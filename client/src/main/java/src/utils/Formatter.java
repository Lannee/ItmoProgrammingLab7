package src.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Formatter {

    public Formatter() {

    }


    public static <T> String format(Collection<T> collection, Class<T> clT) {
        String[] headers = ObjectUtils.getHeaders(clT, false);
        List<String[]> elements = new ArrayList<>(collection.size());

        collection.forEach(e -> elements.add(ObjectUtils.getFieldsValues(e)));

        // table formatting
        int[] columnsWidth = new int[headers.length];

        for(int i = 0; i < headers.length; i++) {
            int columnWidth = headers[i].length();
            for(String[] line : elements) {
                if(line[i].length() > columnWidth)
                    columnWidth = line[i].length();
            }
            columnsWidth[i] = columnWidth;
        }

        StringBuilder sb = new StringBuilder();

        // top line
        for(int i = 0; i < headers.length; i++) {
            headers[i] = padRight(headers[i], columnsWidth[i], ' ');
            for(String[] line : elements) {
                line[i] = padRight(line[i], columnsWidth[i], ' ');
            }
        }

        sb.append(getHLine(columnsWidth,
                SPSymbs.TABLE_HORIZONTAL_LINE.getSymb(),
                SPSymbs.TABLE_CORNER_TOP_LEFT.getSymb(),
                SPSymbs.TABLE_CONNECTION_DOWN.getSymb(),
                SPSymbs.TABLE_CORNER_TOP_RIGHT.getSymb()) + "\n");

        // headers line
        sb.append(getValuesLine(headers, SPSymbs.TABLE_VERTICAL_LINE.getSymb()) + "\n");

        // values lines
        for(String[] element : elements) {
            sb.append(getHLine(columnsWidth,
                    SPSymbs.TABLE_HORIZONTAL_LINE.getSymb(),
                    SPSymbs.TABLE_CONNECTION_RIGHT.getSymb(),
                    SPSymbs.TABLE_CROSS_CONNECTION.getSymb(),
                    SPSymbs.TABLE_CONNECTION_LEFT.getSymb()) + "\n");

            sb.append(getValuesLine(element, SPSymbs.TABLE_VERTICAL_LINE.getSymb()) + "\n");
        }

        // bottom line
        sb.append(getHLine(columnsWidth,
                SPSymbs.TABLE_HORIZONTAL_LINE.getSymb(),
                SPSymbs.TABLE_CORNER_BOTTOM_LEFT.getSymb(),
                SPSymbs.TABLE_CONNECTION_UP.getSymb(),
                SPSymbs.TABLE_CORNER_BOTTOM_RIGHT.getSymb()));

        return sb.toString();
    }

    private static String padRight(String line, int length, Character symbol) {
        if(line.length() >= length) return line;

        return line + symbol.toString().repeat(length - line.length());
    }

    private static String getHLine(int[] columnsWidth, Character line, Character connectionLeft, Character connectionCenter, Character connectionRight) {
        StringBuilder sb = new StringBuilder();
        sb.append(connectionLeft);
        for(int i = 0; i < columnsWidth.length; i++) {
            sb.append(line.toString().repeat(columnsWidth[i] + 2));
            if(i != columnsWidth.length - 1)
                sb.append(connectionCenter);
        }
        sb.append(connectionRight);

        return sb.toString();
    }

    private static String getValuesLine(String[] values, Character separator) {
        StringBuilder sb = new StringBuilder();
        sb.append(separator + " ");
        sb.append(String.join(" " + separator + " ", values));
        sb.append(" " + separator);
        return sb.toString();
    }
}
