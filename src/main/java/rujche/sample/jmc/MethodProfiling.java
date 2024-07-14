package rujche.sample.jmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MethodProfiling {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodProfiling.class);

    public static void main(String[] args) {
        LOGGER.info("main started.");
        Set<Integer> set1 = createIntegerSet(10_000_000);
        Set<Integer> set2 = createIntegerSet(10_000_000);
        Set<Integer> set3 = createIntegerSet(10_000_000);
        Set<Integer> common12 = createCommonSet(set1, set2);
        Set<Integer> common123 = createCommonSet(common12, set3);
        LOGGER.info("common123.size() = {}.", common123.size());
        LOGGER.info("main ended.");
    }

    private static Set<Integer> createIntegerSet(int size) {
        return createIntegerSet(0, size * 10, size);
    }

    private static Set<Integer> createIntegerSet(int x1, int x2, int size) {
        Set<Integer> result = new HashSet<>();
        while (result.size() < size) {
            double f = Math.random() / Math.nextDown(1.0);
            double x = x1 * (1.0 - f) + x2 * f;
            result.add((int) x);
        }
        return result;
    }

    private static Set<Integer> createCommonSet(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> result = new HashSet<>();
        for (Integer i : set1) {
            if (set2.contains(i)) {
                result.add(i);
            }
        }
        return result;
    }
}
