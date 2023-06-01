package src.logic.data;

public final class DBQueries {
    public static final String initializationQuery = """
            SELECT d.id AS id, d.name AS name, cr.id coordinatesId, x, y, creationDate, age, wingspan, weight, cl1.color AS color, p.id killerId, p.name killerName, birthday, height, passportID, cl2.color AS heirColor
            FROM dragon d
                     JOIN coordinates cr on cr.id = d.coordinates
                     JOIN color cl1 on cl1.id = d.color
                     LEFT JOIN person p on p.id = d.killer
                     LEFT JOIN color cl2 on p.heirColor = cl2.id
            ORDER BY age, d.name;
            """;

    public static final String coordinatesAddStatement = """
            INSERT INTO coordinates (x, y)
                    VALUES (?, ?);
            """;

    public static final String personAddStatement = """
            INSERT INTO person (name, birthday, height, passportID, heirColor)
                        VALUES (?, ?, ?, ?, (SELECT id FROM color WHERE color = ?))
            """;

    public static final String dragonAddStatement = """
            INSERT INTO dragon (name, coordinates, age, wingspan, weight, color, killer)
                    VALUES (
                               ?, ?, ?, ?, ?,
                               (SELECT id FROM color WHERE color = ? LIMIT 1), ?
                            );
            """;

    public static final String currvalStatement = """
            SELECT currval(?)
            FROM (values('')) AS t(currval);
            """;

    public static final String updateDragon = """
            UPDATE dragon
            SET name = ?, age = ?,wingspan = ?,
                weight = ?, color = (SELECT id FROM color WHERE Color.color = ?)
            WHERE id = ?;
            """;

    public static final String updateKillerId = """
            UPDATE dragon
                SET killer = ?
                WHERE id = ?;
            """;

    public static final String updateCoordinates = """
            UPDATE coordinates
            SET x = ?, y = ?
            WHERE id = ?;
            """;

    public static final String updatePerson = """
            UPDATE person
            SET name = ?, birthday = ?, height = ?,
            passportID = ?, heirColor = ?
            WHERE id = ?;
            """;

    public static final String coordinatesIDNPersonIdStatement = """
            SELECT coordinates, killer
            FROM dragon
            WHERE id = ?;
            """;

    public static final String deletePersonById = """
            DELETE FROM person
            WHERE id = ?;
            """;
    public static final String getColorID = """
            SELECT id
            FROM color
            WHERE Color.color = ?
            """;

    public static final String commitAdd = """
            INSERT INTO users_dragon (users, dragon)
            VALUES (?, ?);
            """;

    public static final String getUserCreatedDragon = """
            SELECT users FROM users_dragon WHERE dragon = ?;
            """;
}
