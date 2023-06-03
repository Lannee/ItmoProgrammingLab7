package src.logic.data;

import module.stored.Color;
import module.stored.Coordinates;
import module.stored.Dragon;
import module.stored.Person;
import src.logic.data.db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static src.logic.data.DBQueries.initializationQuery;

public class DBCollectionLoader implements CollectionLoader {

    private final DBConnection connection;

    public DBCollectionLoader(DBConnection connection) {
        this.connection = connection;
    }

    @Override
    public List<Dragon> getCollection() {
        List<Dragon> collection = new LinkedList<>();

        try(Statement statement = connection.getConnection().createStatement()) {
            ResultSet rs = statement.executeQuery(initializationQuery);
            while (rs.next()) {
                Dragon dragon = new Dragon();
                dragon.setId(rs.getLong("id"));
                dragon.setName(rs.getString("name"));
                
                rs.getInt("coordinatesid");
                if (!rs.wasNull()) {
                    Coordinates coordinates = new Coordinates();
                    coordinates.setX(rs.getLong("x"));
                    coordinates.setY(rs.getInt("y"));
                    dragon.setCoordinates(coordinates);
                }

                dragon.setCreationDate(rs.getObject("creationdate", OffsetDateTime.class).toZonedDateTime());
                dragon.setAge(rs.getLong("age"));
                dragon.setWingspan(rs.getLong("wingspan"));
                dragon.setWeight(rs.getFloat("weight"));
                dragon.setColor(Color.valueOf(rs.getString("color")));

                rs.getInt("killerId");
                if (!rs.wasNull()) {
                    Person killer = new Person();
                    killer.setName(rs.getString("killername"));
                    killer.setBirthday(rs.getObject("birthday", Date.class));
                    killer.setHeight(rs.getFloat("height"));
                    killer.setPassportID(rs.getString("passportid"));
                    String heirColor = rs.getString("heircolor");
                    killer.setHairColor(heirColor != null ? Color.valueOf(heirColor) : null);
                    dragon.setKiller(killer);
                }

                collection.add(dragon);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return collection;
    }
}
