package src.logic.data.db;

import module.logic.exceptions.FileFormatException;

import java.io.*;

public class DBConfParser {

    private String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private String password = "";
    private String userName = "postgres";


    public DBConfParser(String filePath) throws FileNotFoundException {
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if(line.equals("")) continue;
                String[] splitted = line.split(":", 1);
                if(splitted.length < 2) continue;
                switch (splitted[0]) {
                    case "db_url" -> this.dbURL = splitted[1];
                    case "user_name" -> this.userName = splitted[1];
                    case "user_password" -> this.password = splitted[1];
                }
            }

        } catch (IOException e) {
            if(e instanceof FileNotFoundException)
                throw new FileNotFoundException(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String getDbURL() {
        return dbURL;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }
}
