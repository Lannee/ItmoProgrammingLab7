package src.logic.data;

import module.logic.exceptions.CannotCreateObjectException;
import module.stored.Dragon;
import src.utils.Formatter;
import module.utils.ObjectUtils;
import src.utils.StringConverter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

/**
 * Responsible for performing various actions on the collection
 */
public class Receiver {
//    private final DataManager<Dragon> collection = new CSVFileDataManager<>(Dragon.class);
    private final DataManager<Dragon> collection = new DBDataManager("jdbc:postgresql://localhost:5432/studs");

    public Receiver(String filePath) {
        collection.initialize(filePath);
    }

    public synchronized void add(Object obj) {
        collection.add(getStoredType().cast(obj));
    }

    public synchronized void add(Object obj, long id) {
        try {
            if (id <= 0)
                throw new NumberFormatException("Incorrect argument value");

            ObjectUtils.setFieldValue(obj, "id", id);
            collection.add(getStoredType().cast(obj));

        } catch (NoSuchFieldException | IllegalArgumentException impossible) {}
    }

    public synchronized void clear() {
        collection.clear();
    }

    public synchronized String getInfo() {
        return collection.getInfo();
    }

    public synchronized String getFormattedCollection(Comparator<Dragon> sorter) {
        return Formatter.format(collection.getElements(sorter), collection.getClT());
    }

    public synchronized String getFormattedCollection() {
        return getFormattedCollection(Comparator.reverseOrder());
    }

    public synchronized <T> Integer countCompareToValueByField(String fieldName, Comparable value, Comparator<Comparable<T>> comparator)
            throws NumberFormatException, NoSuchFieldException {
        int counter = 0;
        Field field = collection.getClT().getDeclaredField(fieldName);
        field.setAccessible(true);
//        Comparable givenValue = (Comparable) StringConverter.methodForType.get(field.getType()).apply(value);
        if (!ObjectUtils.checkValueForRestrictions(field, value)) {
            throw new NumberFormatException();
        }
        for (Object element : collection.getElements()) {
            try {
                if (comparator.compare(value, (Comparable) field.get(element)) > 0)
                    counter++;
            } catch (IllegalAccessException impossible) {
            }
        }
        return counter;
    }

    public synchronized void saveCollection() {
        collection.save();
    }

    public synchronized Dragon getElementByFieldValue(String fieldName, Object value)
            throws NumberFormatException, NoSuchFieldException {

        Field idField;
        idField = collection.getClT().getDeclaredField(fieldName);
        idField.setAccessible(true);
        for (Dragon e : collection.getElements()) {
            try {
                if (idField.get(e).equals(value)) {
                    System.out.println(e);
                    return e;
                }
            } catch (IllegalAccessException ex) {
            }
        }
        return null;
    }

    public synchronized Dragon getElementByIndex(int index) {
        return collection.get(index);
    }

    public synchronized int collectionSize() {
        return collection.size();
    }

    public synchronized boolean removeFromCollection(Object o) {
        return collection.remove(o);
    }

    // Needed to be fixed.
    public synchronized String removeOn(Predicate<Dragon> filter, boolean showRemoved) {
        if (collection.size() == 0) {
            return "Cannot remove since the collection is empty";
        }

        List<Dragon> removed = new LinkedList<>();
        for (Dragon element : collection.getElements()) {
            if (filter.test(element)) {
                removed.add(element);
                removeFromCollection(element);
            }
        }

        if (showRemoved) {
            return Formatter.format(removed, collection.getClT());
        }

        return "";
    }

    public synchronized String removeByIndex(int index, boolean showRemoved) {
        if (collection.size() == 0) {
            return "Cannot remove since the collection is empty";
        }

        if (index >= collection.size()) {
            return "Cannot remove from collection: index is out of bound";
        }

        Object obj = getElementByIndex(index);
        return removeOn(e -> e == obj, showRemoved);
    }

    public Class<Dragon> getStoredType() {
        return collection.getClT();
    }

    public Map<Object, Integer> groupByField(String fieldName) throws NoSuchFieldException {
        Map<Object, Integer> groups = new HashMap<>();
        Field field = collection.getClT().getDeclaredField(fieldName);
        field.setAccessible(true);
        for (Object element : collection.getElements()) {
            try {
                Object key = field.get(element);
                if (groups.containsKey(key)) {
                    Integer value = groups.get(key);
                    groups.put(key, ++value);
                } else {
                    groups.put(key, 1);
                }
            } catch (IllegalAccessException impossible) {
            }
        }
        return groups;
    }
}
