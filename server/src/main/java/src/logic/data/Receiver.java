package src.logic.data;

import module.stored.Dragon;
import src.utils.Formatter;
import module.utils.ObjectUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * Responsible for performing various actions on the collection
 */
public class Receiver {
    //    private final DataManager<Dragon> collection = new CSVFileDataManager<>(Dragon.class);
    private final DataManager<Dragon> collection = new DBDataManager("jdbc:postgresql://localhost:5432/studs");
    ReentrantLock reentrantLockOnWrite = new ReentrantLock();
    ReentrantLock reentrantLockOnRead = new ReentrantLock();

    public Receiver(String filePath) {
        reentrantLockOnWrite.lock();
        collection.initialize(filePath);
        reentrantLockOnWrite.unlock();
    }

    public void add(Object obj, int userId) {
        reentrantLockOnWrite.lock();
        collection.add(getStoredType().cast(obj), userId);
        reentrantLockOnWrite.unlock();
    }

    public void update(long id, Object newObject) {
        if (id <= 0) throw new NumberFormatException("Incorrect argument value");
        if(!(newObject instanceof Dragon dragon)) throw new ClassCastException();

        reentrantLockOnWrite.lock();
        collection.update(id, dragon);
        reentrantLockOnWrite.unlock();

    }

    public void add(Object obj, long id) {
        reentrantLockOnWrite.lock();
        try {
            if (id <= 0)
                throw new NumberFormatException("Incorrect argument value");

            ObjectUtils.setFieldValue(obj, "id", id);
            collection.add(getStoredType().cast(obj));

        } catch (NoSuchFieldException | IllegalArgumentException impossible) {}
        reentrantLockOnWrite.unlock();
    }

    public void clear(int userId) {
        reentrantLockOnWrite.lock();
        collection.clear();
        reentrantLockOnWrite.unlock();
    }

    public String getInfo() {
        reentrantLockOnRead.lock();
        String result = collection.getInfo();
        reentrantLockOnRead.unlock();
        return result;
    }

    public String getFormattedCollection(Comparator<Dragon> sorter) {
        reentrantLockOnRead.lock();
        String result = Formatter.format(collection.getElements(sorter), collection.getClT());
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
        Field field = collection.getClT().getDeclaredField(fieldName);
        field.setAccessible(true);
//        Comparable givenValue = (Comparable) StringConverter.methodForType.get(field.getType()).apply(value);
        if (!ObjectUtils.checkValueForRestrictions(field, value)) {
            throw new NumberFormatException();
        }
        for (Object element : collection.getElements()) {
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
        collection.save();
    }

    public Dragon getElementByFieldValue(String fieldName, Object value)
            throws NumberFormatException, NoSuchFieldException {
        reentrantLockOnRead.lock();
        Field idField;
        idField = collection.getClT().getDeclaredField(fieldName);
        idField.setAccessible(true);
        for (Dragon e : collection.getElements()) {
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
        Dragon result = collection.get(index);
        reentrantLockOnRead.unlock();
        return result;
    }

    public int collectionSize() {
        reentrantLockOnRead.unlock();
        int result = collection.size();
        return result;
    }

    public boolean removeFromCollection(Object o) {
        reentrantLockOnWrite.lock();
        boolean result = collection.remove(o);
        reentrantLockOnWrite.unlock();
        return result;
    }

    public String removeOn(Predicate<Dragon> filter, boolean showRemoved) {
        if (collection.size() == 0) {
            return "Cannot remove since the collection is empty";
        }
        reentrantLockOnWrite.lock();
        List<Dragon> removed = new LinkedList<>();
        for (Dragon element : collection.getElements()) {
            if (filter.test(element)) {
                removed.add(element);
                removeFromCollection(element);
            }
        }

        if (showRemoved) {
            String result = Formatter.format(removed, collection.getClT());
            reentrantLockOnWrite.unlock();
            return result;
        }

        return "";
    }

    public String removeByIndex(int index, boolean showRemoved) {
        if (collection.size() == 0) {
            return "Cannot remove since the collection is empty";
        }

        if (index >= collection.size()) {
            return "Cannot remove from collection: index is out of bound";
        }

        Object obj = getElementByIndex(index);
        return removeOn(e -> e == obj, showRemoved);
    }

    public Class<Dragon> getStoredType() {
        return collection.getClT();
    }

    public Map<Object, Integer> groupByField(String fieldName) throws NoSuchFieldException {
        reentrantLockOnRead.lock();
        Map<Object, Integer> groups = new HashMap<>();
        Field field = collection.getClT().getDeclaredField(fieldName);
        field.setAccessible(true);
        for (Object element : collection.getElements()) {
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
}