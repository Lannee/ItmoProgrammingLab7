package module.utils;

import java.io.*;

public class PGParser {

    private String userName;
    private String password;

    public PGParser(String filePath) throws FileNotFoundException {
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath)))) {

            String[] elements = reader.readLine().split(":");
            userName = elements[3];
            password = elements[4];

        } catch (IOException e) {
            if(e instanceof FileNotFoundException)
                throw new FileNotFoundException(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
