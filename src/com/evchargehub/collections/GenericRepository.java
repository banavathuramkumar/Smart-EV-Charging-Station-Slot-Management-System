package com.evchargehub.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generic Interface for an in-memory repository.
 * - Generic Interface.
 */
interface Repository<T> {
    void add(T item);
    T get(int index);
    int size();
    List<T> getAll();
    void clear();
}

/**
 * Base generic class in the hierarchy.
 * - Generic Class Hierarchies.
 */
class BaseGenericRepository<T> implements Repository<T> {
    protected final List<T> storage = new ArrayList<>();

    @Override
    public void add(T item) {
        if (item != null) {
            storage.add(item);
        }
    }

    @Override
    public T get(int index) {
        if (index >= 0 && index < storage.size()) {
            return storage.get(index);
        }
        return null;
    }

    @Override
    public int size() {
        return storage.size();
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public void clear() {
        storage.clear();
    }
}

/**
 * Subclass extending generic base class.
 * Demonstrates:
 * - Generic Class Hierarchies
 * - Generic Methods
 * - Wildcard Arguments (? and ? extends Number)
 * - Iterator usage
 */
public class GenericRepository<T> extends BaseGenericRepository<T> {

    public GenericRepository() {
        super();
    }

    /**
     * Generic Method: Takes an array of any type E and adds to a target list.
     */
    public static <E> void addArrayToList(E[] array, List<E> list) {
        for (E element : array) {
            list.add(element);
        }
    }

    /**
     * Method using Unbounded Wildcard Arguments (?): Can process a list of any type.
     */
    public static void printAnyList(List<?> list, String title) {
        System.out.println("--- " + title + " (Total: " + list.size() + ") ---");
        Iterator<?> iterator = list.iterator();
        while (iterator.hasNext()) {
            System.out.println("  • " + iterator.next());
        }
    }

    /**
     * Method using Upper Bounded Wildcard Arguments (? extends Number):
     * Sums numbers of any subtype of Number (Integer, Double, Float).
     */
    public static double sumNumbers(List<? extends Number> numbers) {
        double total = 0.0;
        for (Number n : numbers) {
            if (n != null) {
                total += n.doubleValue();
            }
        }
        return total;
    }

    /**
     * Retrieve elements using Iterator.
     */
    public void forEachItem(java.util.function.Consumer<T> consumer) {
        Iterator<T> it = storage.iterator();
        while (it.hasNext()) {
            consumer.accept(it.next());
        }
    }
}
