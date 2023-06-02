package src.logic.data.db;

import module.logic.exceptions.FileFormatException;

import java.io.*;
import java.util.Properties;

public class DBConfParser {

    private String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private String password = "";
    private String userName = "postgres";


    public DBConfParser() {
        Properties props = new Properties();
        try {
            props.load(DBConfParser.class.getResourceAsStream("/config.properties"));

            dbURL = props.getProperty("db_url");
            userName = props.getProperty("user_name");
            password = props.getProperty("user_password");
        } catch (IOException e) {
            e.printStackTrace();
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
