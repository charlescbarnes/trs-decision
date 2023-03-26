import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Collections;
import java.util.List;

/**
 * This class consists entirely of static methods for computing statistics for sample (<code>Integer</code>)
 * data contained in a <code>List<Integer></code>.
 * <p>
 * I'm sure this exists elsewhere.
 *
 * @author Charlie Barnes
 */
public class Statistics {
    public static double mean(List<Integer> list) {
        return list.stream().mapToDouble(x -> x).average().getAsDouble();
    }

    /**
     * <code>percentile(list, n)</code> returns the <em>100*n</em>th percentile entry of the data
     * contained in <code>list</code>
     * @param list          a <code>List</code> of type <code>Integer</code>
     * @param percentile    a <code>Double</code> value in the interval <em>[0,1]</em>
     * @throws IllegalArgumentException    if percentile is less than 0 or greater than 1
     * @return              the <em>100*n</em>th percentile entry of the data, as a <code>double</code>
     */
    public static double percentile(List<Integer> list, double percentile) {
        if (percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException("percentile must be between 0 and 1, inclusive");
        }
        Collections.sort(list);
        double doubleIndex = percentile * (list.size() - 1);
        int indexBelow = (int) Math.floor(doubleIndex);
        int indexAbove = (int) Math.ceil(doubleIndex);
        return ((double) list.get(indexBelow) + list.get(indexAbove)) / 2;
    }

    public static double median(List<Integer> list) {
        return percentile(list, 0.5);
    }

    /**
     * @param list  a <code>List</code> of type <code>Integer</code> containing sample data
     * @return the unbiased sample variance of the data (as a <code>Double</code>)
     */
    public static double variance(List<Integer> list) {
        double mean = mean(list);
        return list.stream().mapToDouble(x -> Math.pow((x - mean), 2)).sum() / (list.size() - 1);
    }

    /**
     * @param list  a <code>List</code> of type <code>Integer</code> containing sample data
     * @return the corrected sample standard deviation of the data (as a <code>Double</code>)
     */
    public static double sd(List<Integer> list) {
        return Math.sqrt(variance(list));
    }

    /**
     * @param list          a <code>List</code> of type <code>Integer</code>
     * @param threshold     a <code>double</code>
     * @return              the (<code>double</code>) proportion of entries in <code>list</code> that are strictly
     *                      less than <code>threshold</code>
     */
    public static double percentBelow(List<Integer> list, double threshold) {
        Collections.sort(list);
        return (double) list.stream().takeWhile(x -> x < threshold).count() / list.size();
    }

    /**
     * @param confidenceLevel a <code>double</code> in the interval <em>[0,1)</em>
     * @throws IllegalArgumentException if <code>confidenceLevel</code> is less than 0 or at least 1
     * @return the z-score (or quantile) associated with <code>confidenceLevel</code>, as a <code>double</code>
     */
    public static double getZ(double confidenceLevel) {
        if (confidenceLevel < 0 || confidenceLevel >= 1) {
            throw new IllegalArgumentException("confidenceLevel must be between 0 (inclusive) and 1 (exclusive)");
        }
        NormalDistribution standard = new NormalDistribution();
        double leftTail = (1 - confidenceLevel) / 2;
        return standard.inverseCumulativeProbability(leftTail + confidenceLevel);
    }

    /**
     * @param list              a <code>List</code> of type <code>Integer</code> containing sample data
     * @param confidenceLevel   a <code>double</code> in the interval <em>[0,1)</em>
     * @throws IllegalArgumentException if <code>confidenceLevel</code> is less than 0 or at least 1
     * @return the margin of error at <code>confidenceLevel</code>, as a <code>double</code>
     */
    public static double getMarginOfError(List<Integer> list, double confidenceLevel) {
        return getZ(confidenceLevel) * sd(list) / Math.sqrt(list.size());
    }
}
