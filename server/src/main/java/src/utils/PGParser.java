package src.utils;

import module.logic.exceptions.FileFormatException;

import java.io.*;

public class PGParser {

    private String userName;
    private String password;

    public PGParser(String filePath) throws IOException {
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath)))) {

            String[] elements = reader.readLine().split(":");
            if(elements.length < 5) throw new FileFormatException();

            userName = elements[3];
            password = elements[4];
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}