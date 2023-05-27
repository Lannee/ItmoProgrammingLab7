package src.authorization;

public class AuthorizationQueries {
        public static final String isUserExists = """
                        SELECT id
                        FROM "user"
                        WHERE login = ?;
                        """;

        public static final String passwordConfirmation = """
                        SELECT id
                        FROM "user"
                        WHERE login = ? AND
                              password = ?;
                        """;

        public static final String getSalt = """
                        SELECT salt
                        FROM "user"
                        WHERE login = ?;
                        """;

        public static final String registerUser = """
                        INSERT INTO "user" (login, password, salt)
                        VALUES (?, ?, ?);
                        """;
        
        public static final String getUserIdFromUserName = """
                SELECT
                        "user".id
                FROM
                        "user"
                WHERE
                        "user".login = ?;
                """; 
}
