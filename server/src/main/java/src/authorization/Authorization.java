package src.authorization;

import module.utils.PGParser;

import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

import static src.authorization.AuthorizationQueries.*;

public class Authorization {

    private static final String paper = "]8~h/+$>";

    private static MessageDigest md5;

    private Connection connection;

    public Authorization(String filePath, String dbURL) throws FileNotFoundException, SQLException {
        try {
            PGParser pgParser = new PGParser(filePath);

            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(dbURL, pgParser.getUserName(), pgParser.getPassword());

            md5 = MessageDigest.getInstance("DM5");
        } catch (NoSuchAlgorithmException ignored) {}
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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

    public boolean isLogingSusseccfully(String user, String password) {
        String passwordHash = getPasswordHash(user, password);

        try {
            PreparedStatement loggingStatement = connection.prepareStatement(passwordConfirmation);
            loggingStatement.setString(1, user);
            loggingStatement.setString(2, passwordHash);
            ResultSet isLoginSuccessfully = loggingStatement.executeQuery();
            return isLoginSuccessfully.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSalt(String user) {
        try {
            PreparedStatement saltStatement = connection.prepareStatement(getSalt);
            saltStatement.setString(1, user);
            ResultSet saltRS = saltStatement.executeQuery();
            if(saltRS.next()) {
                return saltRS.getString(1);
            } else {
                throw new SQLException("User does not exist");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPasswordHash(String user, String password) {
        String salt = getSalt(user);
        md5.update((paper + password + salt).getBytes());
        return new String(md5.digest());
    }

}
