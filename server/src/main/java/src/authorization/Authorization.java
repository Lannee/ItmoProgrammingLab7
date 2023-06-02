package src.authorization;

import module.connection.responseModule.LoginStatus;
import module.connection.responseModule.RegistrationStatus;
import src.logic.data.db.ConfigurationParser;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Random;

import static src.authorization.AuthorizationQueries.*;

public class Authorization {

    private static final String pepper = "]8~h/+$>";

    private static MessageDigest md5;

    private Connection connection;

    public Authorization(Connection dbConnection) throws FileNotFoundException, SQLException {
        try {
            this.connection = dbConnection;
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {
            throw new RuntimeException();
        }
    }

    public boolean isUserExists(String user) {
        try {
            PreparedStatement userStatement = connection.prepareStatement(isUserExists);
            userStatement.setString(1, user);
            ResultSet result = userStatement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public LoginStatus loginUser(String user, String password) {
        String passwordHash = getPasswordHash(getSalt(user), password);

        try {
            PreparedStatement loggingStatement = connection.prepareStatement(passwordConfirmation);
            loggingStatement.setString(1, user);
            loggingStatement.setString(2, passwordHash);
            ResultSet isLoginSuccessfully = loggingStatement.executeQuery();
            System.out.println("login user done");
            return isLoginSuccessfully.next() ? LoginStatus.SUCCESSFUL : LoginStatus.INVALID_PASSWORD;
        } catch (SQLException e) {
            e.printStackTrace();
            return LoginStatus.FAILED;
        }
    }

    private String getSalt(String user) {
        try {
            PreparedStatement saltStatement = connection.prepareStatement(getSalt);
            saltStatement.setString(1, user);
            ResultSet saltRS = saltStatement.executeQuery();
            if (saltRS.next()) {
                return saltRS.getString(1);
            } else {
                throw new SQLException("User does not exist");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPasswordHash(String salt, String password) {
        return new String(md5.digest((pepper + password + salt).getBytes()), StandardCharsets.UTF_8);
    }

    public RegistrationStatus registerUser(String user, String password) {
        if (isUserExists(user))
            return RegistrationStatus.USER_ALREADY_EXISTS;
        if (password.length() < 5)
            return RegistrationStatus.SHORT_PASSWORD;

        String salt = generateSalt();
        String passwordHash = getPasswordHash(salt, password);

        try {
            PreparedStatement registrationStatement = connection.prepareStatement(registerUser);
            registrationStatement.setString(1, user);
            registrationStatement.setString(2, passwordHash);
            registrationStatement.setString(3, salt);
            if (registrationStatement.executeUpdate() != 0) {
                return RegistrationStatus.SUCCESSFUL;
            } else {
                return RegistrationStatus.FAILED;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return RegistrationStatus.FAILED;
        }
    }

    private static String generateSalt() {
        byte[] saltBytes = new byte[4];

        new Random().nextBytes(saltBytes);

        return new String(saltBytes, StandardCharsets.UTF_8);
    }
}