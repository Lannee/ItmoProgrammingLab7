package src.logic.data;

import module.stored.Color;
import module.stored.Coordinates;
import module.stored.Dragon;
import module.stored.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.Date;
import java.util.function.Consumer;

import static src.logic.data.DBQueries.*;

public class DBDataManager implements DataManager<Dragon> {

    private static final Logger logger = LoggerFactory.getLogger(DBDataManager.class);

    private final List<Dragon> collection = new LinkedList<>();

    private Connection dbConnection;

    private final String dbURL;

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

                rs.getInt("coordinatesid");
                if(!rs.wasNull()) {
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
                if(!rs.wasNull()) {
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
        System.out.println("-------------------------Didn't fall--------------------------0");

        try {
            dbConnection.setAutoCommit(false);
            Savepoint savepoint = dbConnection.setSavepoint();

            // creating coordinates
            Coordinates coordinates = element.getCoordinates();
            PreparedStatement coordinatesStatement = dbConnection.prepareStatement(coordinatesAddStatement);
            coordinatesStatement.setLong(1, coordinates.getX());
            coordinatesStatement.setInt(2, coordinates.getY());
            coordinatesStatement.executeUpdate();

            PreparedStatement coordinateCurrval = dbConnection.prepareStatement(currvalStatement);
            coordinateCurrval.setString(1, "coordinates_id_seq");
            ResultSet rs = coordinateCurrval.executeQuery();
            rs.next();
            int coordinates_id = rs.getInt(1);

            // creating person
            Person killer = element.getKiller();
            Integer killer_id = null;
            if(killer != null) {
                killer_id = addPersonToDB(killer);
            }
            System.out.println("-------------------------Didn't fall--------------------------1");

            // creating dragon
            PreparedStatement dragonStatement = dbConnection.prepareStatement(dragonAddStatement);
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
            System.out.println("-------------------------Didn't fall--------------------------2");

            try {
                if (dragonStatement.executeUpdate() > 0) {
                    System.out.println("-------------------------Didn't fall--------------------------3 ");
                    dbConnection.commit();
                    PreparedStatement dragonCurrval = dbConnection.prepareStatement(currvalStatement);
                    dragonCurrval.setString(1, "dragon_id_seq");
                    ResultSet currval = dragonCurrval.executeQuery();
                    currval.next();
                    element.setId(currval.getInt(1));

                    collection.add(element);
                    sort();
                } else {
                    dbConnection.rollback(savepoint);
                }
            } catch (SQLException e) {
                dbConnection.rollback(savepoint);
                throw new SQLException(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(long id, Dragon newObject) {
        System.out.println("here2");
        try {
            dbConnection.setAutoCommit(false);
            Savepoint savepoint = dbConnection.setSavepoint();

            // getting coordinates and killer ids
            PreparedStatement coordinatesIdNPersonIdQuery = dbConnection.prepareStatement(coordinatesIDNPersonIdStatement);
            coordinatesIdNPersonIdQuery.setLong(1, id);
            ResultSet ids = coordinatesIdNPersonIdQuery.executeQuery();
            ids.next();
            long coordinatesId = ids.getLong("coordinates");
            long personId = ids.getLong("killer");

            // updating coordinates
            Coordinates newCoordinates = newObject.getCoordinates();
            PreparedStatement coordinatesStatement = dbConnection.prepareStatement(updateCoordinates);
            coordinatesStatement.setLong(1, newCoordinates.getX());
            coordinatesStatement.setInt(2, newCoordinates.getY());
            coordinatesStatement.setLong(3, coordinatesId);
            coordinatesStatement.executeUpdate();

            // updating killer
            Person newKiller = newObject.getKiller();
            if(personId != 0) {
                if (newKiller != null) {
                    PreparedStatement personStatement = dbConnection.prepareStatement(updatePerson);
                    personStatement.setString(1, newKiller.getName());
                    personStatement.setTimestamp(2, Timestamp.from(newKiller.getBirthday().toInstant()));
                    personStatement.setFloat(3, newKiller.getHeight());
                    personStatement.setString(4, newKiller.getPassportID());
                    personStatement.setString(5, newKiller.getHairColor().name());
                    personStatement.setLong(6, personId);
                    personStatement.executeUpdate();
                } else {
                    PreparedStatement deletePerson = dbConnection.prepareStatement(deletePersonById);
                    deletePerson.setLong(1, personId);
                    deletePerson.executeUpdate();
                }
            } else {
                if(newKiller != null) {
                    personId = addPersonToDB(newKiller);
                    PreparedStatement setKillerId = dbConnection.prepareStatement(updateKillerId);
                    setKillerId.setLong(1, personId);
                    setKillerId.setLong(2, id);
                    setKillerId.executeUpdate();
                }
            }

            // updating dragon
            PreparedStatement dragonStatement = dbConnection.prepareStatement(updateDragon);
            dragonStatement.setString(1, newObject.getName());
            dragonStatement.setLong(2, newObject.getAge());
            dragonStatement.setLong(3, newObject.getWingspan());
            dragonStatement.setFloat(4, newObject.getWeight());
            dragonStatement.setString(5, newObject.getColor().name());
            dragonStatement.setLong(6, id);

            try {
                if (dragonStatement.executeUpdate() > 0) {
                    dbConnection.commit();
                    Dragon oldObject = getDragonById(id);
                    remove(oldObject);
                    newObject.setId(id);
                    addToCollection(newObject);
                    sort();
                } else {
                    dbConnection.rollback(savepoint);
                }
            } catch (SQLException e) {
                dbConnection.rollback(savepoint);
                throw new SQLException(e);
            }

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
        return collection.get(id);
    }

    @Override
    public boolean remove(Object o) {
        if(!(o instanceof Dragon dragon)) throw new ClassCastException();
        return collection.remove(o);
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

    private int addPersonToDB(Person person) throws SQLException {
        PreparedStatement personStatement = dbConnection.prepareStatement(personAddStatement);
        personStatement.setString(1, person.getName());

        if (person.getBirthday() != null)
            personStatement.setTimestamp(2, Timestamp.from(person.getBirthday().toInstant()));
        else personStatement.setNull(2, Types.TIMESTAMP);

        personStatement.setFloat(3, person.getHeight());
        personStatement.setString(4, person.getPassportID());

        if (person.getHairColor() != null) personStatement.setString(5, person.getHairColor().name());
        else personStatement.setNull(5, Types.VARCHAR);

        if(personStatement.executeUpdate() > 0) {
            PreparedStatement personCurrval = dbConnection.prepareStatement(currvalStatement);
            personCurrval.setString(1, "person_id_seq");
            ResultSet rs1 = personCurrval.executeQuery();
            rs1.next();
            return rs1.getInt(1);
        } else {
            return 0;
        }
    }

    private Dragon getDragonById(long id) {
        for(Dragon dragon : collection) {
            if(dragon.getId() == id) return dragon;
        }
        return null;
    }

    public void addToCollection(Dragon newObject) {
        collection.add(collection.size(), newObject);
    }

    public void sort() {
        Collections.sort(collection);
    }
}
