package src.logic.data;

import module.stored.Dragon;
import src.logic.data.db.ConfigurationParser;
import src.logic.data.db.DBConnection;
import src.utils.Formatter;
import module.utils.ObjectUtils;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Responsible for performing various actions on the collection
 */
public class Receiver {
    // private final DataManager<Dragon> collection = new
    // CSVFileDataManager<>(Dragon.class);
    private final DataManager<Dragon> db;

    private final List<Dragon> collection = new LinkedList<>();

    private final DBConnection dbConnection;

    ReentrantLock reentrantLockOnWrite = new ReentrantLock();
    ReentrantLock reentrantLockOnRead = new ReentrantLock();

    public Receiver(DBConnection dbConnection) throws FileNotFoundException {

        this.dbConnection = dbConnection;
        CollectionLoader collectionLoader = new DBCollectionLoader(dbConnection);
        collection.addAll(collectionLoader.getCollection());

        System.out.println(collection.size());

        db = new DBDataManager(dbConnection.getConnection());
    }

    public void add(Object obj, int userId) {
        reentrantLockOnWrite.lock();
        if (db.add(getStoredType().cast(obj), userId))
            collection.add(getStoredType().cast(obj));
        reentrantLockOnWrite.unlock();
    }

    public void update(long id, Object newObject, int userId) {
        if (id <= 0)
            throw new NumberFormatException("Incorrect argument value");
        if (!(newObject instanceof Dragon dragon))
            throw new ClassCastException();

        reentrantLockOnWrite.lock();
        // db.update(id, dragon, userId);
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

    public List<Long> getDragonUserCreated(int userId) {
        List<Long> resultList = db.getDragonUserCreated(userId);
        return resultList;
    }

    public void add(Object obj, long id, int userId) {
        reentrantLockOnWrite.lock();
        try {
            if (id <= 0)
                throw new NumberFormatException("Incorrect argument value");

            ObjectUtils.setFieldValue(obj, "id", id);
            db.add(getStoredType().cast(obj), userId);

        } catch (NoSuchFieldException | IllegalArgumentException impossible) {
        }
        reentrantLockOnWrite.unlock();
    }

    public boolean removeDragon(long dragonId, int userId) {
        reentrantLockOnWrite.lock();
        if (db.removeDragon(dragonId)){
            if (this.removeById(dragonId, userId)) {
                return true;
            }
        }
        reentrantLockOnWrite.unlock();
        return false;
    }

    public int clear(int userId) {
        reentrantLockOnWrite.lock();
        int countRemoved = 0;
        List<Long> removedDragons = db.clear(userId);
        for (long dragonId : removedDragons) {
            if (this.removeById(dragonId, userId)) {
                countRemoved++;
            }
        }
        reentrantLockOnWrite.unlock();
        return countRemoved;
    }

    public String getInfo() {
        reentrantLockOnRead.lock();
        String result = db.getInfo();
        reentrantLockOnRead.unlock();
        return result;
    }

    public String getFormattedCollection(Comparator<Dragon> sorter) {
        reentrantLockOnRead.lock();
        String result = Formatter.format(this.getElements(sorter), this.getClT());
        reentrantLockOnRead.unlock();
        return result;
    }

    public String getFormattedCollection() {
        reentrantLockOnRead.lock();
        String result = getFormattedCollection(Comparator.reverseOrder());
        reentrantLockOnRead.unlock();
        return result;
    }

    public <T> Integer countCompareToValueByField(String fieldName, Comparable value,
            Comparator<Comparable<T>> comparator)
            throws NumberFormatException, NoSuchFieldException {
        reentrantLockOnRead.lock();
        int counter = 0;
        Field field = this.getClT().getDeclaredField(fieldName);
        field.setAccessible(true);
        // Comparable givenValue = (Comparable)
        // StringConverter.methodForType.get(field.getType()).apply(value);
        if (!ObjectUtils.checkValueForRestrictions(field, value)) {
            throw new NumberFormatException();
        }
        for (Object element : this.getElements()) {
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

    }

    public synchronized Dragon getElementByFieldValue(String fieldName, Object value)
            throws NumberFormatException, NoSuchFieldException {
        reentrantLockOnRead.lock();
        Field idField;
        idField = this.getClT().getDeclaredField(fieldName);
        idField.setAccessible(true);
        for (Dragon e : this.getElements()) {
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
        Dragon result = this.get(index);
        reentrantLockOnRead.unlock();
        return result;
    }

    public int collectionSize() {
        reentrantLockOnRead.unlock();
        int result = this.size();
        return result;
    }

    public synchronized boolean removeFromCollection(Object o, int userId) {
        reentrantLockOnWrite.lock();
        boolean result = this.remove(o, userId);
        reentrantLockOnWrite.unlock();
        return result;
    }

    public synchronized String removeOn(Predicate<Dragon> filter, boolean showRemoved, int userId) {
        if (this.size() == 0) {
            return "Cannot remove since the collection is empty";
        }
        reentrantLockOnWrite.lock();
        List<Dragon> removed = new LinkedList<>();
        for (Dragon element : this.getElements()) {
            if (filter.test(element)) {
                removed.add(element);
                removeFromCollection(element, userId);
            }
        }

        if (showRemoved) {
            String result = Formatter.format(removed, this.getClT());
            reentrantLockOnWrite.unlock();
            return result;
        }

        return "";
    }

    public String removeByIndex(int index, boolean showRemoved, int userId) {
        if (this.size() == 0) {
            return "Cannot remove since the collection is empty";
        }

        if (index >= this.size()) {
            return "Cannot remove from collection: index is out of bound";
        }

        Object obj = getElementByIndex(index);
        return removeOn(e -> e == obj, showRemoved, userId);
    }

    public Class<Dragon> getStoredType() {
        return this.getClT();
    }

    public Map<Object, Integer> groupByField(String fieldName) throws NoSuchFieldException {
        reentrantLockOnRead.lock();
        Map<Object, Integer> groups = new HashMap<>();
        Field field = this.getClT().getDeclaredField(fieldName);
        field.setAccessible(true);
        for (Object element : this.getElements()) {
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

    public synchronized int getUserIdFromUserName(String userName) {
        return db.getUserIdFromUserName(userName);
    }

    public synchronized Dragon get(int id) {
        return collection.get(id);
    }

    public synchronized boolean removeById(long dragonId, int userId) {
        return this.remove(getDragonById(dragonId), userId);
    }

    public synchronized boolean remove(Object o, int userId) {
        if (!(o instanceof Dragon dragon))
            throw new ClassCastException();
        return collection.remove(o);
    }

    public int size() {
        return collection.size();
    }

    public synchronized List<Dragon> getElements() {
        return getElements(Comparator.naturalOrder());
    }

    public synchronized List<Dragon> getElements(Comparator<? super Dragon> sorter) {
        return getElements(sorter, 0, size());
    }

    public synchronized List<Dragon> getElements(Comparator<? super Dragon> sorter, int startIndex, int endIndex) {
        List<Dragon> copy = new LinkedList<>(collection);
        copy.sort(sorter);
        return copy.subList(startIndex, endIndex);
    }

    public Class<Dragon> getClT() {
        return Dragon.class;
    }

    public synchronized void forEach(Consumer<? super Dragon> action) {
        collection.forEach(action);
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
}