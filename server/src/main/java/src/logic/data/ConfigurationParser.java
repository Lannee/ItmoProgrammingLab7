package src.logic.data;

import src.utils.PGParser;

import java.io.*;
import java.util.Properties;

public class ConfigurationParser {

    private String dbURL = "jdbc:postgresql://localhost:5432/postgres";
    private String password = "password";
    private String userName = "postgres";


    public ConfigurationParser() {
        Properties props = new Properties();
        try(InputStream resources = ConfigurationParser.class.getResourceAsStream("/config.properties")) {

            if(resources != null) {
                props.load(resources);
                dbURL = props.getProperty("db_url");

                String pgpass = props.getProperty("pgpass");
                if(pgpass != null) {
                    try {
                        PGParser pgParser = new PGParser(pgpass);
                        userName = pgParser.getUserName();
                        password = pgParser.getPassword();
                    } catch (Exception ignored) {}
                }

                userName = props.getProperty("user_name");
                password = props.getProperty("user_password");
            }
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
