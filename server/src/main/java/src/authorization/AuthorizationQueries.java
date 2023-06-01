package src.authorization;

public class AuthorizationQueries {
        public static final String isUserExists = """
                        SELECT id
                        FROM users
                        WHERE login = ?;
                        """;

        public static final String passwordConfirmation = """
                        SELECT id
                        FROM users
                        WHERE login = ? AND
                              password = ?;
                        """;

        public static final String getSalt = """
                        SELECT salt
                        FROM users
                        WHERE login = ?;
                        """;

        public static final String registerUser = """
                        INSERT INTO users (login, password, salt)
                        VALUES (?, ?, ?);
                        """;
        
        public static final String getUserIdFromUserName = """
                SELECT
                        users.id
                FROM
                        users
                WHERE
                        users.login = ?;
                """; 
}
