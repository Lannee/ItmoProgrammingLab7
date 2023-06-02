package src.logic.data;

import module.logic.exceptions.FileFormatException;
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
import static src.authorization.AuthorizationQueries.*;

public class DBDataManager implements DataManager<Dragon> {

    private static final Logger logger = LoggerFactory.getLogger(DBDataManager.class);

    private final List<Dragon> collection = new LinkedList<>();

    private Connection dbConnection;

    DBDataManager(Connection connection) {
        this.dbConnection = connection;
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public void add(Dragon element, int userId) {
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
            if (killer != null) {
                killer_id = addPersonToDB(killer);
            }

            // creating dragon
            PreparedStatement dragonStatement = dbConnection.prepareStatement(dragonAddStatement);
            dragonStatement.setString(1, element.getName());
            dragonStatement.setInt(2, coordinates_id);

            if (element.getAge() != null)
                dragonStatement.setLong(3, element.getAge());
            else
                dragonStatement.setNull(3, Types.BIGINT);

            if (element.getWingspan() != null)
                dragonStatement.setLong(4, element.getWingspan());
            else
                dragonStatement.setNull(4, Types.INTEGER);

            dragonStatement.setFloat(5, element.getWeight());
            dragonStatement.setString(6, element.getColor().name());

            if (killer_id != null)
                dragonStatement.setInt(7, killer_id);
            else
                dragonStatement.setNull(7, Types.INTEGER);
            Integer dragonId = null;
            try {
                if (dragonStatement.executeUpdate() > 0) {
                    PreparedStatement dragonCurrval = dbConnection.prepareStatement(currvalStatement);
                    dragonCurrval.setString(1, "dragon_id_seq");
                    ResultSet currval = dragonCurrval.executeQuery();
                    currval.next();
                    dragonId = currval.getInt(1);
                    element.setId(dragonId);
                    commitAdd(userId, dragonId);
                    dbConnection.commit();

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

    public long[] getCoorfinatesIDNPersonID(long dragonId) {
        long[] resultList = new long[2];
        // getting coordinates and killer ids
        try (
                PreparedStatement coordinatesIdNPersonIdQuery = dbConnection
                        .prepareStatement(coordinatesIDNPersonIdStatement)) {
            coordinatesIdNPersonIdQuery.setLong(1, dragonId);
            ResultSet ids = coordinatesIdNPersonIdQuery.executeQuery();
            ids.next();
            long coordinatesId = ids.getLong("coordinates");
            long personId = ids.getLong("killer");

            resultList[0] = coordinatesId;
            resultList[1] = personId;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    @Override
    public void update(long dragonId, Dragon newObject, int userId) {
        try {
            dbConnection.setAutoCommit(false);
            Savepoint savepoint = dbConnection.setSavepoint();

            // getting coordinates and killer ids
            long[] coordinateAndPersonIds = getCoorfinatesIDNPersonID(dragonId);
            long coordinatesId = coordinateAndPersonIds[0];
            long personId = coordinateAndPersonIds[1];

            // updating coordinates
            Coordinates newCoordinates = newObject.getCoordinates();
            PreparedStatement coordinatesStatement = dbConnection.prepareStatement(updateCoordinates);
            coordinatesStatement.setLong(1, newCoordinates.getX());
            coordinatesStatement.setInt(2, newCoordinates.getY());
            coordinatesStatement.setLong(3, coordinatesId);
            coordinatesStatement.executeUpdate();

            // updating killer
            Person newKiller = newObject.getKiller();
            if (personId != 0) {
                if (newKiller != null) {
                    PreparedStatement personStatement = dbConnection.prepareStatement(updatePerson);
                    personStatement.setString(1, newKiller.getName());

                    if (newKiller.getBirthday() != null)
                        personStatement.setTimestamp(2, Timestamp.from(newKiller.getBirthday().toInstant()));
                    else
                        personStatement.setNull(2, Types.TIMESTAMP);

                    personStatement.setFloat(3, newKiller.getHeight());
                    personStatement.setString(4, newKiller.getPassportID());

                    if (newKiller.getHairColor() == null) {
                        personStatement.setNull(5, Types.INTEGER);
                    } else {
                        PreparedStatement heirColor = dbConnection.prepareStatement(getColorID);
                        heirColor.setString(1, newKiller.getHairColor().name());
                        ResultSet heirColorID = heirColor.executeQuery();
                        heirColorID.next();
                        personStatement.setInt(5, heirColorID.getInt(1));
                    }

                    personStatement.setLong(6, personId);
                    personStatement.executeUpdate();
                } else {
                    PreparedStatement deletePerson = dbConnection.prepareStatement(deletePersonById);
                    deletePerson.setLong(1, personId);
                    deletePerson.executeUpdate();
                }
            } else {
                if (newKiller != null) {
                    personId = addPersonToDB(newKiller);
                    PreparedStatement setKillerId = dbConnection.prepareStatement(updateKillerId);
                    setKillerId.setLong(1, personId);
                    setKillerId.setLong(2, dragonId);
                    setKillerId.executeUpdate();
                }
            }

            // updating dragon
            PreparedStatement dragonStatement = dbConnection.prepareStatement(updateDragon);
            dragonStatement.setString(1, newObject.getName());

            if (newObject.getAge() != null)
                dragonStatement.setLong(2, newObject.getAge());
            else
                dragonStatement.setNull(2, Types.BIGINT);

            if (newObject.getWingspan() != null)
                dragonStatement.setLong(3, newObject.getWingspan());
            else
                dragonStatement.setNull(3, Types.INTEGER);

            dragonStatement.setFloat(4, newObject.getWeight());
            dragonStatement.setString(5, newObject.getColor().name());
            dragonStatement.setLong(6, dragonId);

            try {
                if (dragonStatement.executeUpdate() > 0) {
                    dbConnection.commit();
                    Dragon oldObject = getDragonById(dragonId);
                    remove(oldObject, userId);
                    newObject.setId(dragonId);
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
    public void addAll(Collection<Dragon> elements, int userId) {
        elements.forEach(e -> add(e, userId));
    }

    @Override
    public int clear(int userId) {
        List<Long> dragonsIdCreatedByUser = this.getDragonUserCreated(userId);
        int countRemoved = 0;

        for (long dragonid : dragonsIdCreatedByUser) {
            // getting coordinates and killer ids
            long[] coordinateAndPersonIds = getCoorfinatesIDNPersonID(dragonid);
            long coordinatesId = coordinateAndPersonIds[0];
            long personId = coordinateAndPersonIds[1];

            // deleting dragon's killer
            try (PreparedStatement deletePerson = dbConnection.prepareStatement(deletePersonById)) {
                deletePerson.setLong(1, personId);
                deletePerson.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // deleting dragon's coordinates
            try (PreparedStatement deleteCoordinates = dbConnection.prepareStatement(deleteCoordinatesById)) {
                deleteCoordinates.setLong(1, coordinatesId);
                deleteCoordinates.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // deleting dragon by its id
            try (PreparedStatement deleteDragon = dbConnection.prepareStatement(deleteDragonById)) {
                deleteDragon.setLong(1, dragonid);
                deleteDragon.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            countRemoved++;
        }
        return countRemoved;
    }

    @Override
    public Dragon get(int id) {
        return collection.get(id);
    }

    @Override
    public boolean remove(Object o, int userId) {
        if (!(o instanceof Dragon dragon))
            throw new ClassCastException();
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
        else
            personStatement.setNull(2, Types.TIMESTAMP);

        personStatement.setFloat(3, person.getHeight());
        personStatement.setString(4, person.getPassportID());

        if (person.getHairColor() != null)
            personStatement.setString(5, person.getHairColor().name());
        else
            personStatement.setNull(5, Types.VARCHAR);

        if (personStatement.executeUpdate() > 0) {
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
        for (Dragon dragon : collection) {
            if (dragon.getId() == id)
                return dragon;
        }
        return null;
    }

    public void addToCollection(Dragon newObject) {
        collection.add(collection.size(), newObject);
    }

    public void sort() {
        Collections.sort(collection);
    }

    public void commitAdd(int userId, int dragonId) {
        try {
            PreparedStatement commitAddStatement = dbConnection.prepareStatement(commitAdd);
            commitAddStatement.setInt(1, userId);
            commitAddStatement.setInt(2, dragonId);
            System.out.println("Изменено строк в базе данных " + commitAddStatement.executeUpdate());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // public boolean dragonUserMatch(int userId, int dragonId) {
    // try {
    // PreparedStatement match = dbConnection.prepareStatement()
    // }
    // }

    public int getUserIdFromUserName(String userName) {
        int userId = 0;
        PreparedStatement match;
        try {
            match = dbConnection.prepareStatement(getUserIdFromUserName);
            match.setString(1, userName);
            ResultSet resultSet = match.executeQuery();
            if (resultSet.next()) {
                userId = resultSet.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    @Override
    public List<Integer> getUsersIdCreatedDragon(long dragonId) {
        List<Integer> resultListDragonId = new ArrayList<Integer>();
        try (PreparedStatement statement = dbConnection.prepareStatement(getUserCreatedDragon)) {
            statement.setInt(1, (int) dragonId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                resultListDragonId.add(rs.getInt("users"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultListDragonId;
    }

    @Override
    public List<Long> getDragonUserCreated(int userId) {
        List<Long> resultListDragonId = new ArrayList<Long>();
        try (PreparedStatement statement = dbConnection.prepareStatement(getDragonsCreatedByUser)) {
            statement.setInt(1, (int) userId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                resultListDragonId.add(rs.getLong("dragon"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultListDragonId;
    }
}
