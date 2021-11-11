package utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Perform operations on sets.
 * */
public class SetOperations {

    /**
     * Returns the union of two sets.
     * @param a first set
     * @param b second set
     * @return union of the two sets
     * */
    public static <T> Set<T> setUnion(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>();
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    /**
     * Returns the intersection of two sets.
     * @param a first set
     * @param b second set
     * @return intersection of the two sets
     * */
    public static <T> Set<T> setIntersection(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.retainAll(b);
        return result;
    }

    /**
     * Returns the difference of two sets.
     * @param a first set
     * @param b second set
     * @return difference of the two sets
     * */
    public static <T> Set<T> setDifference(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.removeAll(b);
        return result;
    }

    /**
     * Returns the complement of two sets.
     * @param a first set
     * @param b second set
     * @return complement of the two sets
     * */
    public static <T> Set<T> setComplement(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.removeAll(b);
        result.addAll(b);
        result.removeAll(a);
        return result;
    }
}
