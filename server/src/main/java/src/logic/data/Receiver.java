package src.logic.data;

import module.stored.Dragon;
import module.utils.PGParser;
import src.logic.data.db.DBCollectionLoader;
import src.logic.data.db.DBConfParser;
import src.logic.data.db.DBConnection;
import src.utils.Formatter;
import module.utils.ObjectUtils;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * Responsible for performing various actions on the collection
 */
public class Receiver {
    //    private final DataManager<Dragon> collection = new CSVFileDataManager<>(Dragon.class);
    private final DataManager<Dragon> db;

    private final List<Dragon> collection = new LinkedList<>();

    private final DBConnection dbConnection;

    ReentrantLock reentrantLockOnWrite = new ReentrantLock();
    ReentrantLock reentrantLockOnRead = new ReentrantLock();

    public Receiver(String filePath) throws FileNotFoundException {

        DBConfParser conf = new DBConfParser(filePath);
        try {
            dbConnection = new DBConnection(conf.getDbURL(), conf.getUserName(), conf.getPassword());

            CollectionLoader collectionLoader = new DBCollectionLoader(dbConnection);
            collection.addAll(collectionLoader.getCollection());

            db = new DBDataManager(dbConnection.getConnection());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(Object obj, int userId) {
        reentrantLockOnWrite.lock();
        db.add(getStoredType().cast(obj), userId);
        reentrantLockOnWrite.unlock();
    }

    public void update(long id, Object newObject, int userId) {
        if (id <= 0) throw new NumberFormatException("Incorrect argument value");
        if(!(newObject instanceof Dragon dragon)) throw new ClassCastException();

        reentrantLockOnWrite.lock();
//        db.update(id, dragon, userId);
        reentrantLockOnWrite.unlock();
    }

    public String update(long id, int userId) {
        try {
            // Long id = (Long) args[0];
            
            Object obj = this.getElementByFieldValue("id", id);
            if (obj != null) {
                List<Integer> usersIdCreatedDragonList = this.getUsersIdCreatedDragon(((Dragon) obj).getId());
                if (usersIdCreatedDragonList != null) {
                    if (!usersIdCreatedDragonList.contains(userId)) {
                        return "You have not created this Dragon. \nYou can update only objects that you had created.";
                    }
                }
                return "WAITING";
            } else {
                return "Element with this id does not exist";
            }
        } catch (NoSuchFieldException e) {
            return "Stored type does not support this command";
        } catch (NumberFormatException nfe) {
            return "Incorrect argument value";
        }
    }

    public List<Integer> getUsersIdCreatedDragon(long dragonId) {
        List<Integer> resultList = db.getUsersIdCreatedDragon(dragonId);
        return resultList;
    }

    public void add(Object obj, long id, int userId) {
        reentrantLockOnWrite.lock();
        try {
            if (id <= 0)
                throw new NumberFormatException("Incorrect argument value");

            ObjectUtils.setFieldValue(obj, "id", id);
            db.add(getStoredType().cast(obj), userId);

        } catch (NoSuchFieldException | IllegalArgumentException impossible) {}
        reentrantLockOnWrite.unlock();
    }

    public void clear(int userId) {
        reentrantLockOnWrite.lock();
        db.clear(userId);
        reentrantLockOnWrite.unlock();
    }

    public String getInfo() {
        reentrantLockOnRead.lock();
        String result = db.getInfo();
        reentrantLockOnRead.unlock();
        return result;
    }

    public String getFormattedCollection(Comparator<Dragon> sorter) {
        reentrantLockOnRead.lock();
        String result = Formatter.format(db.getElements(sorter), db.getClT());
        reentrantLockOnRead.unlock();
        return result;
    }

    public String getFormattedCollection() {
        reentrantLockOnRead.lock();
        String result = getFormattedCollection(Comparator.reverseOrder());
        reentrantLockOnRead.unlock();
        return result;
    }

    public <T> Integer countCompareToValueByField(String fieldName, Comparable value, Comparator<Comparable<T>> comparator)
            throws NumberFormatException, NoSuchFieldException {
        reentrantLockOnRead.lock();
        int counter = 0;
        Field field = db.getClT().getDeclaredField(fieldName);
        field.setAccessible(true);
//        Comparable givenValue = (Comparable) StringConverter.methodForType.get(field.getType()).apply(value);
        if (!ObjectUtils.checkValueForRestrictions(field, value)) {
            throw new NumberFormatException();
        }
        for (Object element : db.getElements()) {
            try {
                if (comparator.compare(value, (Comparable) field.get(element)) > 0)
                    counter++;
            } catch (IllegalAccessException impossible) {
            }
        }
        reentrantLockOnRead.unlock();
        return counter;
    }

    public synchronized void saveCollection() {
        db.save();
    }

    public Dragon getElementByFieldValue(String fieldName, Object value)
            throws NumberFormatException, NoSuchFieldException {
        reentrantLockOnRead.lock();
        Field idField;
        idField = db.getClT().getDeclaredField(fieldName);
        idField.setAccessible(true);
        for (Dragon e : db.getElements()) {
            try {
                if (idField.get(e).equals(value)) {
                    reentrantLockOnRead.unlock();
                    return e;
                }
            } catch (IllegalAccessException ex) {
            }
        }
        reentrantLockOnRead.unlock();
        return null;
    }

    public Dragon getElementByIndex(int index) {
        reentrantLockOnRead.lock();
        Dragon result = db.get(index);
        reentrantLockOnRead.unlock();
        return result;
    }

    public int collectionSize() {
        reentrantLockOnRead.unlock();
        int result = db.size();
        return result;
    }

    public boolean removeFromCollection(Object o, int userId) {
        reentrantLockOnWrite.lock();
//        boolean result = db.remove(o, userId);
        reentrantLockOnWrite.unlock();
//        return result;
        return true;
    }

    public String removeOn(Predicate<Dragon> filter, boolean showRemoved, int userId) {
        if (db.size() == 0) {
            return "Cannot remove since the collection is empty";
        }
        reentrantLockOnWrite.lock();
        List<Dragon> removed = new LinkedList<>();
        for (Dragon element : db.getElements()) {
            if (filter.test(element)) {
                removed.add(element);
                removeFromCollection(element, userId);
            }
        }

        if (showRemoved) {
            String result = Formatter.format(removed, db.getClT());
            reentrantLockOnWrite.unlock();
            return result;
        }

        return "";
    }

    public String removeByIndex(int index, boolean showRemoved, int userId) {
        if (db.size() == 0) {
            return "Cannot remove since the collection is empty";
        }

        if (index >= db.size()) {
            return "Cannot remove from collection: index is out of bound";
        }

        Object obj = getElementByIndex(index);
        return removeOn(e -> e == obj, showRemoved, userId);
    }

    public Class<Dragon> getStoredType() {
        return db.getClT();
    }

    public Map<Object, Integer> groupByField(String fieldName) throws NoSuchFieldException {
        reentrantLockOnRead.lock();
        Map<Object, Integer> groups = new HashMap<>();
        Field field = db.getClT().getDeclaredField(fieldName);
        field.setAccessible(true);
        for (Object element : db.getElements()) {
            try {
                Object key = field.get(element);
                if (groups.containsKey(key)) {
                    Integer value = groups.get(key);
                    groups.put(key, ++value);
                } else {
                    groups.put(key, 1);
                }
            } catch (IllegalAccessException impossible) {
            }
        }
        reentrantLockOnRead.unlock();
        return groups;
    }

    public int getUserIdFromUserName(String userName) {
        return db.getUserIdFromUserName(userName);
    }
}