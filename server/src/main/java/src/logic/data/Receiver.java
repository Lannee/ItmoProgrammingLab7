package src.logic.data;

import module.stored.Dragon;
import src.logic.data.db.DBCollectionLoader;
import src.logic.data.db.DBConnection;
import src.logic.data.db.DBDataManager;
import src.utils.Formatter;
import module.utils.ObjectUtils;

import java.lang.reflect.Field;
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

    ReentrantLock lock = new ReentrantLock();

    public Receiver(DBConnection dbConnection) {

        this.dbConnection = dbConnection;
        CollectionLoader collectionLoader = new DBCollectionLoader(dbConnection);
        collection.addAll(collectionLoader.getCollection());

        db = new DBDataManager(dbConnection.getConnection());
    }

    public void add(Object obj, int userId) {
        lock.lock();
        try {
            if (db.add(getStoredType().cast(obj), userId))
                collection.add(getStoredType().cast(obj));
        } finally {
            lock.unlock();
        }
    }

    public void update(long id, Object newObject, int userId) {
        if (id <= 0)
            throw new NumberFormatException("Incorrect argument value");
        if (!(newObject instanceof Dragon dragon))
            throw new ClassCastException();

        lock.lock();
        try {
            if (db.update(id, dragon, userId)) {
                collection.remove(dragon);
                collection.add(dragon);
            }
        } finally {
            lock.unlock();
        }
    }

    public String update(long id, int userId) {
        try {
            // Long id = (Long) args[0];

            Object obj = this.getElementByFieldValue("id", id);
            if (obj != null) {
                List<Integer> usersIdCreatedDragonList = this.getUsersIdCreatedDragon(((Dragon) obj).getId());
                if (usersIdCreatedDragonList != null) {
                    if (!usersIdCreatedDragonList.contains(userId)) {
                        return "You have not created this Dragon. \nYou can update only objects you had created.";
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
        lock.lock();
        try {
            if (id <= 0)
                throw new NumberFormatException("Incorrect argument value");

            ObjectUtils.setFieldValue(obj, "id", id);
            db.add(getStoredType().cast(obj), userId);

        } catch (NoSuchFieldException | IllegalArgumentException impossible) {
        } finally {
            lock.unlock();
        }
    }

    public boolean removeDragon(long dragonId, int userId) {
        lock.lock();
        try {
            if (db.removeDragon(dragonId)) {
                if (this.removeById(dragonId, userId)) {
                    return true;
                }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    public int clear(int userId) {
        lock.lock();
        int countRemoved = 0;
        try {
            List<Long> removedDragons = db.clear(userId);
            for (long dragonId : removedDragons) {
                if (this.removeById(dragonId, userId)) {
                    countRemoved++;
                }
            }
        } finally {
            lock.unlock();
        }
        return countRemoved;
    }

    public String getInfo(int userId) {
        lock.lock();
        StringBuilder result = new StringBuilder();
        try {
            result.append("Stored type : ").append(getClT().getSimpleName()).append("\n");
            result.append("Amount of elements : ").append(size()).append("\n");
            String userName = db.getUserNameById(userId);
            result.append("User name : ").append(userName != null ? userName : "Unknown").append("\n");
            result.append("Items created by you : ").append(getDragonUserCreated(userId).size());
        } finally {
            lock.unlock();
        }
        return result.toString();
    }

    public String getFormattedCollection(Comparator<Dragon> sorter) {
        lock.lock();
        String result;
        try {
            result = Formatter.format(this.getElements(sorter), this.getClT());
        } finally {
            lock.unlock();
        }
        return result;
    }

    public String getFormattedCollection() {
        lock.lock();
        String result;
        try {
            result = getFormattedCollection(Comparator.reverseOrder());
        } finally {
            lock.unlock();
        }
        return result;
    }

    public <T> Integer countCompareToValueByField(String fieldName, Comparable value,
            Comparator<Comparable<T>> comparator)
            throws NumberFormatException, NoSuchFieldException {
        lock.lock();
        int counter = 0;
        try {
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
        } finally {
            lock.unlock();
        }
        return counter;
    }

    public synchronized void saveCollection() {

    }

    public Dragon getElementByFieldValue(String fieldName, Object value)
            throws NumberFormatException, NoSuchFieldException {
        lock.lock();
        try {
            Field idField;
            idField = this.getClT().getDeclaredField(fieldName);
            idField.setAccessible(true);
            for (Dragon e : this.getElements()) {
                try {
                    if (idField.get(e).equals(value)) {
//                        lock.unlock();
                        return e;
                    }
                } catch (IllegalAccessException ex) {
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public Dragon getElementByIndex(int index) {
        lock.lock();
        Dragon result;
        try {
            result = this.get(index);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public int collectionSize() {
        int result;
        try {
            result = this.size();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public boolean removeFromCollection(Object o, int userId) {
        lock.lock();
        boolean result;
        try {
            result = this.remove(o, userId);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public String removeOn(Predicate<Dragon> filter, boolean showRemoved, int userId) {
        if (this.size() == 0) {
            return "Cannot remove since the collection is empty";
        }
        lock.lock();
        try {
            List<Dragon> removed = new LinkedList<>();
            for (Dragon element : this.getElements()) {
                if (filter.test(element)) {
                    if (db.removeByIndex(element.getId(), userId)) {
                        removed.add(element);
                        removeFromCollection(element, userId);
                    }
                }
            }

            if (showRemoved) {
                String result = Formatter.format(removed, this.getClT());
                lock.unlock();
                return result;
            }
        } finally {
            lock.unlock();
        }
        return "Successfully";
    }

    public String removeByIndex(boolean showRemoved, int userId) {
        if (this.size() == 0) {
            return "Cannot remove since the collection is empty";
        }

        List<Long> dragonsCreatedByUser = this.getDragonUserCreated(userId);

        if (dragonsCreatedByUser.size() == 0) {
            return "You have not created any dragons.\nYou can remove only objects you had created.";
        }

        long dragonId = dragonsCreatedByUser.get(0);

        Object obj = this.getDragonById(dragonId);
        return removeOn(e -> e == obj, showRemoved, userId);
    }

    public Class<Dragon> getStoredType() {
        return this.getClT();
    }

    public Map<Object, Integer> groupByField(String fieldName) throws NoSuchFieldException {
        lock.lock();
        Map<Object, Integer> groups = new HashMap<>();
        try {
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
        } finally {
            lock.unlock();
        }
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

    public synchronized Dragon getDragonById(long id) {
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