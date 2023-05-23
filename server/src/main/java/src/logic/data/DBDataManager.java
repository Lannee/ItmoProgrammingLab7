package src.logic.data;

import module.stored.Color;
import module.stored.Coordinates;
import module.stored.Dragon;
import module.stored.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class DBDataManager implements DataManager<Dragon> {

    private static final Logger logger = LoggerFactory.getLogger(DBDataManager.class);

    private final List<Dragon> collection = new LinkedList<>();

    private Connection dbConnection;

    private final String dbURL;

    private static final String initializationQuery = """
            SELECT d.id id, d.name name, cr.id coordinatesId, x, y, creationDate, age, wingspan, weight, cl1.color color, p.id killerId, p.name killerName, birthday, height, passportID, cl2.color heirColor
            FROM Dragon d
                     JOIN Coordinates cr on cr.id = d.coordinates
                     JOIN Color cl1 on cl1.id = d.color
                     LEFT JOIN Person p on p.id = d.killer
                     LEFT JOIN Color cl2 on p.heirColor = cl2.id
            ORDER BY age, d.name;
            """;

    private static final String coordnatesAddStatment = """
            INSERT INTO coordinates (x, y)
                    VALUES (?, ?);
            """;

    private static final String personAddStatment = """
            INSERT INTO person (name, birthday, height, passportID, heirColor)
                        VALUES (?, ?, ?, ?, (SELECT id FROM color WHERE color = ?))
            """;

    private static final String dragonAddStatment = """
            INSERT INTO dragon (name, coordinates, age, wingspan, weight, color, killer)
                    VALUES (
                               ?, ?, ?, ?, ?,
                               (SELECT id FROM color WHERE color = ?), ?
                           );
            """;

    private static final String currvalStatment = """
            SELECT currval(?)
            FROM (values('')) AS t(currval);
            """;


    DBDataManager(String url) {
        dbURL = url;
    }

    @Override
    public void initialize(String path) {
        try {
            Class.forName("org.postgresql.Driver");
            dbConnection = DriverManager.getConnection(dbURL, getUserName(path), getPassword(path));
        } catch (SQLException | ClassNotFoundException | FileNotFoundException e) {
            logger.error(e.getMessage());
            return;
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
                    killer.setBirthday(rs.getObject("birthday", Date.class));
                    killer.setHeight(rs.getFloat("height"));
                    killer.setPassportID(rs.getString("passportId"));
                    String heirColor = rs.getString("heirColor");
                    killer.setHairColor(heirColor != null ? Color.valueOf(heirColor) : null);
                    dragon.setKiller(killer);
                }

                collection.add(dragon);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private String getUserName(String filePath) throws FileNotFoundException {
        return parsePgpass(filePath, 3);
    }

    private String getPassword(String filePath) throws FileNotFoundException {
        return parsePgpass(filePath, 4);
    }

    private String parsePgpass(String filePath, int elementID) throws FileNotFoundException {
        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath)))) {

            return reader.readLine().split(":")[elementID];

        } catch (IOException e) {
            if(e instanceof FileNotFoundException)
                throw new FileNotFoundException(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public void add(Dragon element) {
        try {
            // creating coordinates
            Coordinates coordinates = element.getCoordinates();
            PreparedStatement coordinatesStatement = dbConnection.prepareStatement(coordnatesAddStatment);
            coordinatesStatement.setLong(1, coordinates.getX());
            coordinatesStatement.setInt(2, coordinates.getY());
            coordinatesStatement.executeUpdate();

            PreparedStatement coordinateCurrval = dbConnection.prepareStatement(currvalStatment);
            coordinateCurrval.setString(1, "coordinates_id_seq");
            ResultSet rs = coordinateCurrval.executeQuery();
            rs.next();
            Integer coordinates_id = rs.getInt(1);

            // creating person
            Person killer = element.getKiller();
            Integer killer_id = null;
            if(killer != null) {
                PreparedStatement personStatement = dbConnection.prepareStatement(personAddStatment);
                personStatement.setString(1, killer.getName());

                if(killer.getBirthday() != null) personStatement.setTimestamp(2, Timestamp.from(killer.getBirthday().toInstant()));
                else personStatement.setNull(2, Types.TIMESTAMP);

                personStatement.setFloat(3, killer.getHeight());
                personStatement.setString(4, killer.getPassportID());

                if(killer.getHairColor() != null) personStatement.setString(5, killer.getHairColor().name());
                else personStatement.setNull(5, Types.VARCHAR);

                personStatement.executeUpdate();

                PreparedStatement personCurrval = dbConnection.prepareStatement(currvalStatment);
                personCurrval.setString(1, "person_id_seq");
                ResultSet rs1 = personCurrval.executeQuery();
                rs1.next();
                killer_id = rs1.getInt(1);
            }

            // creating dragon
            PreparedStatement dragonStatement = dbConnection.prepareStatement(dragonAddStatment);
            dragonStatement.setString(1, element.getName());
            dragonStatement.setInt(2, coordinates_id);

            if(element.getAge() != null) dragonStatement.setLong(3, element.getAge());
            else dragonStatement.setNull(3, Types.BIGINT);

            if(element.getWingspan() != null) dragonStatement.setLong(4, element.getWingspan());
            else dragonStatement.setNull(4, Types.INTEGER);

            dragonStatement.setFloat(5, element.getWeight());
            dragonStatement.setString(6, element.getColor().name());

            if(killer_id != null) dragonStatement.setInt(7, killer_id);
            else dragonStatement.setNull(7, Types.INTEGER);

            dragonStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addAll(Collection<Dragon> elements) {
        elements.forEach(this::add);
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
        return collection.size();
    }

    @Override
    public List<Dragon> getElements() {
        return getElements(Comparator.naturalOrder());
    }

    @Override
    public List<Dragon> getElements(Comparator<? super Dragon> sorter) {
        return getElements(sorter, 0, size());
    }

    @Override
    public List<Dragon> getElements(Comparator<? super Dragon> sorter, int startIndex, int endIndex) {
        List<Dragon> copy = new LinkedList<>(collection);
        copy.sort(sorter);
        return copy.subList(startIndex, endIndex);
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
        collection.forEach(action);
    }
}
