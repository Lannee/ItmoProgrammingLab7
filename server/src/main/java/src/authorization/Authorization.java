package src.authorization;

import module.connection.responseModule.LoginStatus;
import module.connection.responseModule.RegistrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Random;

import static src.authorization.AuthorizationQueries.*;

public class Authorization {

    private static final String pepper = "]8~h/+$>";
    private static final Logger logger = LoggerFactory.getLogger(Authorization.class);

    private static MessageDigest md5;

    private Connection connection;

    public Authorization(Connection dbConnection) throws SQLException {
        try {
            this.connection = dbConnection;
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
        }
    }

    public boolean isUserExists(String user) {
        try {
            PreparedStatement userStatement = connection.prepareStatement(isUserExists);
            userStatement.setString(1, user);
            ResultSet result = userStatement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public LoginStatus loginUser(String user, String password) {
        try {
            byte[] passwordHash = getPasswordHash(getSalt(user), password);

            PreparedStatement loggingStatement = connection.prepareStatement(passwordConfirmation);
            loggingStatement.setString(1, user);
            loggingStatement.setBytes(2, passwordHash);
            ResultSet isLoginSuccessfully = loggingStatement.executeQuery();
            System.out.println("login user done");
            return isLoginSuccessfully.next() ? LoginStatus.SUCCESSFUL : LoginStatus.INVALID_PASSWORD;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return LoginStatus.FAILED;
        }
    }

    private byte[] getSalt(String user) throws SQLException {
        PreparedStatement saltStatement = connection.prepareStatement(getSalt);
        saltStatement.setString(1, user);
        ResultSet saltRS = saltStatement.executeQuery();
        if (saltRS.next()) {
            return saltRS.getBytes(1);
        } else {
            throw new SQLException("User does not exist");
        }
    }

    private byte[] getPasswordHash(byte[] salt, String password) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(pepper.getBytes());
            baos.write(password.getBytes());
            baos.write(salt);
        } catch (IOException ignored) {}

        return md5.digest(baos.toByteArray());
    }

    public RegistrationStatus registerUser(String user, String password) {
        if (isUserExists(user))
            return RegistrationStatus.USER_ALREADY_EXISTS;
        if (password.length() < 5)
            return RegistrationStatus.SHORT_PASSWORD;

        byte[] salt = generateSalt();
        byte[] passwordHash = getPasswordHash(salt, password);

        try {
            PreparedStatement registrationStatement = connection.prepareStatement(registerUser);
            registrationStatement.setString(1, user);
            registrationStatement.setBytes(2, passwordHash);
            registrationStatement.setBytes(3, salt);
            if (registrationStatement.executeUpdate() != 0) {
                return RegistrationStatus.SUCCESSFUL;
            } else {
                return RegistrationStatus.FAILED;
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
            return RegistrationStatus.FAILED;
        }
    }

    private static byte[] generateSalt() {
        byte[] saltBytes = new byte[4];

        new Random().nextBytes(saltBytes);

        return saltBytes;
    }
}