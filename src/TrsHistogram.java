import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * Uses Standard Draw class functionality to draw a histogram specifically formatted
 * for the TRS Decision problem.
 *
 * @author Charlie Barnes
 */
public class TrsHistogram {
    private List<Integer> trials;
    private int trsAnnuity;
    private int max;
    private int[] freqPerBin;
    private int numBins;
    private double binWidth;
    protected static final String HISTOGRAM_FILE_PATH = "hist.png";

    /**
     * Class constructor
     * @param trials        a <code>List<Integer></code> of trial outcomes from the Monte Carlo Simulation
     * @param trsAnnuity    the TRS annual annuity amount, as an <code>int</code>
     */
    public TrsHistogram(List<Integer> trials, int trsAnnuity) {
        this.trials = trials;
        this.trsAnnuity = trsAnnuity;
        initializeHistogram(trials);
    }

    public void initializeHistogram(List<Integer> trials) {
        int record = Collections.max(trials);
        int recordToTrsRatio = (int) Math.ceil((double) record / trsAnnuity);
        // set the top of the highest bin
        int topTopBin = trsAnnuity * recordToTrsRatio;
        // Note 1: this ensures that topTopBin is an integer multiple (recordToTrsRatio) of trsAnnuity

        // determine the number of bins
        int minNumBins = 40;
        int i = 3;

        while (i * recordToTrsRatio < minNumBins) {
            i++;
        }
        numBins = i * recordToTrsRatio;

        // determine the bin width
        binWidth = (double) topTopBin / numBins;
        // Note 1, cont'd:
            // = topTopBin / (i * recordToTrsRatio)
            // = topTopBin / (i * topTopBin / trsAnnuity)
            // = trsAnnuity / i
        // Note 1, cont'd: Thus, importantly, trsAnnuity will fall at the top of the i^th bin
        // (where i is an integer greater than or equal to 3)
        // Thus coloring the Histogram bars later will be safe.

        // this int array will determine the histogram data (index: bin number; value: bin frequency)
        freqPerBin = new int[numBins];
        for (int trial : trials) {
            int binDex = (int) Math.floor((double) trial / binWidth);
            if (binDex == numBins) {
                binDex--;
            }
            freqPerBin[binDex]++;
            // keep track of the maximum frequency
            if (freqPerBin[binDex] > max) {
                max = freqPerBin[binDex];
            }
        }
    }

    public static final Color VANGUARD_RED = new Color(115,0,0);
    public static final Color VANGUARD_DARK_RED = new Color(80,0,0);
    public static final Color TRS_BLUE = new Color(39,97,142);

    /**
     * Plots histogram bars
     */
    public void plotBars() {
        StdDraw.setXscale(-1, numBins);
        for (int i = 0; i < numBins; i++) {
            if (i * binWidth < trsAnnuity) {
                StdDraw.setPenColor(TRS_BLUE);
            }
            else {
                StdDraw.setPenColor(VANGUARD_RED);
            }
            StdDraw.filledRectangle(i + 0.5, (double) freqPerBin[i] / 2,
                    0.5, (double) freqPerBin[i] / 2);
        }
    }

    public void draw() {
        StdDraw.setCanvasSize(1024, 256);
        StdDraw.setYscale(-0.1 * max, 1.1 * max);  // to leave a little border
        plotBars();
        StdDraw.setPenRadius(.004);
        StdDraw.setFont(new Font("SansSerif", Font.PLAIN, 10));

        // special coordinates
        double xMax = numBins;
        double y0 = 3 * (-1 + StdDraw.getYmin()) / 4;
        double y1 = 3 * (-1 + StdDraw.getYmin()) / 8;
        double y2 = (-1 + StdDraw.getYmin()) / 8;
        double y3 = max + 1 + (StdDraw.getYmax() - (max + 1)) / 4;
        double y4 = (max + 1 + StdDraw.getYmax()) / 2;

        // draw and label Vanguard average line
        StdDraw.setPenColor(VANGUARD_DARK_RED);
        double vanguardAverage = Statistics.mean(trials);
        double vanguardAverageX = vanguardAverage / binWidth;
        StdDraw.line(vanguardAverageX, y2,vanguardAverageX, y3);
        StdDraw.text(vanguardAverageX, y4, "TDF avg. = $" +
                String.format("%,d", (int) Math.round(vanguardAverage)));

        // draw and label TRS annuity line
        StdDraw.setPenColor(StdDraw.BLACK);
        double TrsX = (double) trsAnnuity / binWidth;
        StdDraw.line(TrsX, y2, (double) trsAnnuity / binWidth, y3);
        StdDraw.text(TrsX, y1, "TRS = $" + String.format("%,d", trsAnnuity));

        // draw and label axes
        StdDraw.setFont();
        StdDraw.setPenRadius(.002);
        StdDraw.line(0, 0, xMax, 0);                            // x-axis
        StdDraw.text(numBins / 2, y0,
                "3.3% withdrawal from TDF at retirement (bin width = $" +
                        String.format("%,d", (int) Math.round(binWidth)) + ")"
        );                                                                 // x-axis label
        StdDraw.line(0, 0, 0, y3);                              // y-axis
        StdDraw.text(-1, y3 / 2, "Frequency", 90);         // y-axis label
        StdDraw.setFont(new Font("SansSerif", Font.PLAIN, 10)); // reduce font size
        StdDraw.line(xMax, y2, xMax, -y2);                                 // highest x-axis tick mark
        StdDraw.text(xMax, y1, "$" + String.format("%,.0f", numBins * binWidth)); // highest x-axis label
        StdDraw.line(-0.25, max, 0.25, max);                        // highest bin frequency tick mark
        StdDraw.textRight(-0.5, max, ""+max);                        // highest bin frequency label

        // legend
        double x0 = 7 * xMax / 8;
        StdDraw.setPenColor(TRS_BLUE);
        StdDraw.filledRectangle(x0, 1.1 * y3 / 2, xMax/150, y3/35);
        double pctTrsWins = 100 * Statistics.percentBelow(trials, trsAnnuity);
        StdDraw.textLeft(x0 + 1.5*xMax/150, 1.1 * y3 / 2, "TRS better: " + String.format("%.1f%%", pctTrsWins));
        StdDraw.setPenColor(VANGUARD_RED);
        StdDraw.filledRectangle(x0, 0.9 * y3 / 2, xMax/150, y3/35);
        StdDraw.textLeft(x0 + 1.5*xMax/150, 0.9 * y3 / 2, "TDF better: "  + String.format("%.1f%%", 100-pctTrsWins));
    }
}