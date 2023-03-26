import java.util.Map;
import java.util.TreeMap;

/**
 * This class provides a method of generating a piecewise linear function (from the set of Doubles
 * to the set of Doubles) through a set of ordered pairs (<code>TreeMap</code>) of Doubles.
 *
 * @author Charlie Barnes
 */
public class PiecewiseLinearFunction {
    interface LinearMapping {
        /**
         * the <code>output</code> lambda function (defined later) will return a y-value (double) for:
         * @param x     a particular x-value (any <code>Number</code>)
         * @param x1y1  first of two coordinates (<code>Map.Entry<Double, Double></code>)
         *              to determine a linear function
         * @param x2y2  second of two coordinates (<code>Map.Entry<Double, Double></code>)
         *              to determine a linear function
         * @return      the corresponding y-value (as a <code>double</code>)
         */
        double output(Number x, Map.Entry<Double, Double> x1y1, Map.Entry<Double, Double> x2y2);
    }

    private TreeMap<Double, Double> points;
    private LinearMapping line;

    /**
     * Class constructor
     * @param points a <code>TreeMap</code> of ordered pairs that the function should pass through
     */
    public PiecewiseLinearFunction(TreeMap<Number, Number> points) {
        setPoints(points);

        // define the output lambda function from the LinearMapping interface above
        line = (Number x, Map.Entry<Double, Double> x1y1, Map.Entry<Double, Double> x2y2) -> {
            double x1 = x1y1.getKey();
            double y1 = x1y1.getValue();
            double x2 = x2y2.getKey();
            double y2 = x2y2.getValue();
            double m = (y2-y1) / (x2-x1);
            return y1 + m * (x.doubleValue() - x1);
        };
    }

    /**
     * Accepts a <code>TreeMap</code> of points of any type of <code>Number</code>. The <code>Double</code>
     * value of each coordinate will be added to as entries/values in the <code>points</code> <code>TreeMap</code>.
     * <p>
     * Notice that this method is designed such that our piecewise linear function will be constant both
     * left of <code>this.points.firstKey()</code>, and
     * right of <code>this.points.lastKey()</code>.
     *
     * @param points a <code>TreeMap</code> of ordered pairs that the function should pass through
     */
    public void setPoints(TreeMap<Number, Number> points) {
        this.points = new TreeMap<>();
        // add every pair as a pair of Doubles
        for (Number key : points.keySet()) {
            this.points.put(key.doubleValue(), points.get(key).doubleValue());
        }
        // get the y-values for the left-most and right-most points
        double yLeft = this.points.get(this.points.firstKey());
        double yRight = this.points.get(this.points.lastKey());
        // set these as the outputs for the least and greatest Double inputs accepted, respectively
        this.points.put(-Double.MAX_VALUE, yLeft);
        this.points.put(Double.MAX_VALUE, yRight);
    }

    /**
     * @return the set of points (<code>TreeMap<Double, Double></code>) that defined the piecewise linear function
     */
    public TreeMap<Double, Double> getPoints() { return points; }

    /**
     * @param x     a particular x-value (any <code>Number</code>)
     * @return      the corresponding y-value (as a <code>double</code>)
     */
    public double get(Number x) {
        if (points.containsKey(x.doubleValue())) {
            return points.get(x.doubleValue());
        }
        else {
            return line.output(x, points.lowerEntry(x.doubleValue()), points.higherEntry(x.doubleValue()));
        }
    }

    /**
     * A piecewise linear function representing Vanguard's TDF glide path
     * inputs represent investor age
     * outputs represent a percentage (allocation to stocks)
     * references: see Resources/Vanguard
     */
    public static final PiecewiseLinearFunction VANGUARD_GLIDE_PATH = new PiecewiseLinearFunction(
            new TreeMap<>() {{
                put(20,90); // key: age; value: % allocation to stocks
                put(40,90);
                put(60,60);
                put(65,50);
                put(72,30);
            }}
    );

    /**
     * A piecewise linear function representing historical returns for selected allocations to stocks
     * inputs represent a percentage (allocation to stocks)
     * outputs represent an average annual return on investment (using data from 1926-2021)
     * source: see Resources/HistoricalData/Vanguard.pdf
     */
    public static final PiecewiseLinearFunction HISTORICAL_MEAN_RETURNS = new PiecewiseLinearFunction(
            new TreeMap<>() {{
                put(0,.063); // key: % allocation to stocks; value: average annual return (1926-2021)
                put(20,.075);
                put(30,.081);
                put(40,.087);
                put(50,.093);
                put(60,.099);
                put(70,.105);
                put(80,.111);
                put(100,.123);
            }}
    );

    /**
     * A piecewise linear function representing historical returns for selected allocations to stocks
     * inputs represent a percentage (allocation to stocks)
     * outputs represent an average annual return on investment
     * source: old Vanguard data I pulled for an earlier version of this project (2017, I think), no longer online
     */
    public static final PiecewiseLinearFunction HISTORICAL_MEAN_RETURNS_OLD = new PiecewiseLinearFunction(
            new TreeMap<>() {{
                put(0,.054); // key: % allocation to stocks; value: average annual return (?-?)
                put(20,.067);
                put(30,.072);
                put(40,.078);
                put(50,.083);
                put(60,.087);
                put(70,.091);
                put(80,.095);
                put(100,.101);
            }}
    );

    /**
     * A piecewise linear function representing historical standard deviations for selected allocations to stocks
     * inputs represent a percentage (allocation to stocks)
     * outputs represent a standard deviation of annual returns (using data from 1976-2012)
     * source: see Resources/HistoricalData/QVM.png
     */
    public static final PiecewiseLinearFunction HISTORICAL_SD_RETURNS = new PiecewiseLinearFunction(
            new TreeMap<>() {{
                put(0,.0629); // key: % allocation to stocks; value: average annual volatility (1976-2012)
                put(10,.0626);
                put(20,.0663);
                put(30,.0737);
                put(40,.0840);
                put(50,.0965);
                put(60,.1107);
                put(70,.1264);
                put(80,.1433);
                put(90,.1614);
                put(100,.1807);
            }}
    );

    /**
     * A piecewise linear function representing historical standard deviations for selected allocations to stocks
     * inputs represent a percentage (allocation to stocks)
     * outputs represent a standard deviation of annual returns
     * source: old Raymond James data I pulled for an earlier version of this project (2017, I think), no longer online
     */
    public static final PiecewiseLinearFunction HISTORICAL_SD_RETURNS_RJ = new PiecewiseLinearFunction(
            new TreeMap<>() {{
                put(0,.117); // key: % allocation to stocks; value: average annual volatility
                put(20,.107);
                put(28,.102);
                put(30,.105);
                put(40,.110);
                put(50,.112);
                put(60,.121);
                put(70,.134);
                put(80,.147);
                put(100,.178);
            }}
    );
}
