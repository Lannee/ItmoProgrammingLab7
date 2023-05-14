package src.logic.data;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Interaction interface between program and data storage
 * @param <T> - Stored type
 */
public interface DataManager<T extends Comparable<? super T>> {
    /**
     * @return information about collection in string representation
     */
    String getInfo();

    /**
     * Adds element to the collection
     * @param element
     */
    void add(T element);

    /**
     * Add all specified elements to the collection
     * @param elements
     */
    void addAll(Collection<T> elements);

    /**
     * Clears collection
     */
    void clear();

    /**
     *
     * @param id
     * @return element with given index
     */
    T get(int id);

    /**
     * Removes specified element from the collection
     * @param o
     * @return true - if element was removed successfully, else - false
     */
    boolean remove(Object o);

    /**
     * @return size of the collection
     */
    int size();

    /**
     * @return collection elements in standard ordering
     */
    List<T> getElements();

    /**
     * @param sorter
     * @return collection elements sorted by given sorter
     */
    List<T> getElements(Comparator<? super T> sorter);

    List<T> getElements(Comparator<? super T> sorter, int startIndex, int endIndex);

    /**
     * Initialize collection from specified source
     * @param filePath
     */
    void initialize(String filePath);

    /**
     * Saves changes in the collection to the storage
     */
    void save();

    /**
     * @return Type of the stored elements
     */
    Class<T> getClT();
    void forEach(Consumer<? super T> action);
}
