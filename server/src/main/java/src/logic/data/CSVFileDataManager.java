package src.logic.data;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import module.annotations.Complex;
import module.annotations.Nullable;
import module.annotations.Unique;
import module.logic.exceptions.FileFormatException;
import module.logic.exceptions.FileReadModeException;
import module.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import src.Server;
import src.utils.StringConverter;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.*;
import java.util.*;

/**
 * Specific implementation of FileDataManager as a CSV file storage
 * @param <T> - Stored type
 */
public class CSVFileDataManager<T extends Comparable<? super T>> extends FileDataManager<T> {
    private static final Logger logger = LoggerFactory.getLogger(CSVFileDataManager.class);

    private final Class<T> clT;

    public CSVFileDataManager(Class<T> clT){
        super(clT);
        this.clT = clT;
    }

    @Override
    public void initialize(String filePath) {
        File csvFile = new File(filePath);

        List<String[]> csvContent;
        String[] headers;
        try(Reader isr = new InputStreamReader(
                new FileInputStream(csvFile));
            CSVReader reader = new CSVReader(isr)) {

            if(!csvFile.exists() || csvFile.isDirectory()) throw new FileNotFoundException();
            if(!csvFile.canRead() && !csvFile.canWrite()) throw new FileReadModeException();

            super.file = csvFile;
            super.attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            super.modification = LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault());

            if(csvFile.canRead()) {
                csvContent = reader.readAll();
                if (!(csvContent.size() < 2)) {
                    headers = csvContent.get(0);
                    csvContent = csvContent.subList(1, csvContent.size());

                } else {
                    throw new FileFormatException("File is empty");
                }

                for (String[] values : csvContent) {
                    try {
                        add(getClT()
                                .cast(createObject(getClT(), headers, values, getElements())));

                    } catch (ReflectiveOperationException e) {
                        Server.out.print("Unable to create an object\n");
                    }
                }
            } else {
                Server.out.print("Collection was not initialized since file cannot be read\n");
            }

        } catch (FileFormatException e) {
            if(!ObjectUtils.agreement(Server.in, Server.out, e.getMessage() + ". Do you want to rewrite this file (y/n) : ", false)) {
                System.exit(0);
            }
        } catch(FileReadModeException frme) {
            logger.error("Cannot read and write the file\n");
            Server.out.print("Cannot read and write the file\n");
            System.exit(3);
        } catch (FileNotFoundException fnfe) {
            logger.error("File does not exist or it is a directory\n");
            Server.out.print("File does not exist or it is a directory\n");
            System.exit(2);
        } catch (IOException e) {
            logger.error("Unable to initialize collection\n");
            Server.out.print("Unable to initialize collection\n");
            System.exit(1);
        }
    }

    @Override
    public void save() {
        List<String[]> toCSV = new ArrayList<>(collection.size() + 1);

        toCSV.add(ObjectUtils.getHeaders(getClT(), true));
        forEach(e -> toCSV.add(ObjectUtils.getFieldsValues(e)));

        try(CSVWriter writer = new CSVWriter(new FileWriter(super.file))) {
            writer.writeAll(toCSV);
        } catch (IOException e) {
//            Client.out.print("Unable to save collection into the file.\n");
        }

    }

    private static <T> T createObject(Class<T> cl, String[] headers, String[] values, List<?> collection) throws FileFormatException, ReflectiveOperationException {
        T obj = cl.getConstructor().newInstance();

        List<String[]> headersElements = Arrays.stream(headers)
                .map(e -> e.split("\\."))
                .toList();

        for(int i = 0; i < headersElements.size(); i++) {
            String[] header = headersElements.get(i);
            if(!header[0].equals(cl.getSimpleName())) throw new FileFormatException("Invalid file headers");

            Field field = cl.getDeclaredField(header[1]);
            Class<?> fieldType = field.getType();
            field.setAccessible(true);
            if(field.isAnnotationPresent(Complex.class)) {
                String reducePrefix = cl.getSimpleName() + "." + field.getName() + ".";
                String prefix = reducePrefix + fieldType.getSimpleName() + ".";

                String exHeader;
                int exLevelStart = 0;
                int exLevelEnd = 0;
                for(int j = 0; j < headers.length; j++) {
                    exHeader = headers[j];
                    if(exHeader.startsWith(prefix)) {
                        exLevelStart = j;
                        break;
                    }
                }

                for(int j = exLevelStart; j < headers.length; j++) {
                    exHeader = headers[j];
                    if(!exHeader.startsWith(prefix)) {
                        break;
                    }
                    exLevelEnd = j;
                }

                i = exLevelEnd;

                String[] exHeaders = Arrays.copyOfRange(headers, exLevelStart, exLevelEnd + 1);
                String[] exValues = Arrays.copyOfRange(values, exLevelStart, exLevelEnd + 1);

                exHeaders = Arrays.stream(exHeaders).map(e -> e.substring(reducePrefix.length())).toArray(String[]::new);
                field.set(obj, createObject(fieldType, exHeaders, exValues, collection));
            } else {
                if(fieldType.isEnum()) {
                    Object enumValue;
                    try {
                        if(values[i].equals(ObjectUtils.nullValue))
                            if(field.isAnnotationPresent(Nullable.class))
                                enumValue = null;
                            else
                                throw new ReflectiveOperationException();
                        else
                            enumValue = Enum.valueOf((Class<Enum>) fieldType, values[i]);
                    } catch (IllegalArgumentException iae) {
                        throw new ReflectiveOperationException();
                    }
                    field.set(obj, enumValue);
                } else {
                    String value = values[i];
                    if(value.equals(ObjectUtils.nullValue)) {
                        if(!field.isAnnotationPresent(Nullable.class)) return null;
                        field.set(obj, null);
                    } else {
                        if(!StringConverter.methodForType.containsKey(fieldType)) throw new FileFormatException("Unsupported field type");
                        try {
                            Object valueConverted =  StringConverter.methodForType
                                    .get(field.getType())
                                    .apply(value);
                            if(field.isAnnotationPresent(Unique.class)) {
                                for (Object element : collection) {
                                    if(valueConverted.equals(field.get(element)))
                                        throw new ReflectiveOperationException("Unique field value cannot be repeated");
                                }
                            }
                            field.set(obj, valueConverted);
                        } catch (NumberFormatException e) {
                            throw new FileFormatException("Invalid data");
                        }
                    }
                }
            }
        }

        return obj;
    }

//    @Override
//    public void save() {
//        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException("Unimplemented method 'save'");
//    }
}
