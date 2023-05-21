package src.logic.data;

import module.stored.Color;
import module.stored.Coordinates;
import module.stored.Dragon;
import module.stored.Person;

import java.io.*;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class DBDataManager implements DataManager<Dragon> {

    private final List<Dragon> collection = new LinkedList<>();

    private Connection dbConnection;

    private final String dbURL;

    private static final String initializationQuery = """
            SELECT d.id id, d.name name, cr.id coordinatesId, x, y, creationDate, age, wingspan, weight, cl1.color color, p.id killerId, p.name killerName, birthday, height, passportID, cl2.color heirColor
            FROM Dragon d
                JOIN Coordinates cr on cr.id = d.coordinates
                JOIN Color cl1 on cl1.id = d.color
                JOIN Person p on p.id = d.killer
                JOIN Color cl2 on p.heirColor = cl2.id
            ORDER BY age, d.name;
            """;

    DBDataManager(String url) {
        dbURL = url;
    }

    @Override
    public void initialize(String path) {
        try {
            Class.forName("org.postgresql.Driver");
            dbConnection = DriverManager.getConnection(dbURL, getUserName(path), getPassword(path));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            Statement statement = dbConnection.createStatement();
            ResultSet rs = statement.executeQuery(initializationQuery);
            while(rs.next()) {
                Dragon dragon = new Dragon();
                dragon.setId(rs.getLong("id"));
                dragon.setName(rs.getString("name"));

                rs.getInt("coordinatesId");
                if(!rs.wasNull()) {
                    Coordinates coordinates = new Coordinates();
                    coordinates.setX(rs.getLong("x"));
                    coordinates.setY(rs.getInt("y"));
                    dragon.setCoordinates(coordinates);
                }

                dragon.setCreationDate(rs.getObject("creationDate", OffsetDateTime.class).toZonedDateTime());
                dragon.setAge(rs.getLong("age"));
                dragon.setWingspan(rs.getLong("wingspan"));
                dragon.setWeight(rs.getFloat("weight"));
                dragon.setColor(Color.valueOf(rs.getString("color")));

                rs.getInt("killerId");
                if(!rs.wasNull()) {
                    Person killer = new Person();
                    killer.setName(rs.getString("killerName"));
                    killer.setBirthday(rs.getDate("birthday"));
                    killer.setHeight(rs.getFloat("height"));
                    killer.setPassportID(rs.getString("passportId"));
                    killer.setHairColor(Color.valueOf(rs.getString("heirColor")));
                    dragon.setKiller(killer);
                }

                collection.add(dragon);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private String getUserName(String filePath) {
        return parsePgpass(filePath, 3);
    }

    private String getPassword(String filePath) {
        return parsePgpass(filePath, 4);
    }

    private String parsePgpass(String filePath, int elementID) {
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath)))) {

            return reader.readLine().split(":")[elementID];

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public void add(Dragon element) {

    }

    @Override
    public void addAll(Collection<Dragon> elements) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Dragon get(int id) {
        return null;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public List<Dragon> getElements() {
        return null;
    }

    @Override
    public List<Dragon> getElements(Comparator<? super Dragon> sorter) {
        return null;
    }

    @Override
    public List<Dragon> getElements(Comparator<? super Dragon> sorter, int startIndex, int endIndex) {
        return null;
    }

    @Override
    public void save() {

    }

    @Override
    public Class<Dragon> getClT() {
        return Dragon.class;
    }

    @Override
    public void forEach(Consumer<? super Dragon> action) {

    }
}
