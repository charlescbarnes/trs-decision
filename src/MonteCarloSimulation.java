import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonteCarloSimulation {
    private int trials;
    private TrsMember member;

    // these lists will contain one entry per trial, representing that trial's initial
    // withdrawal amount at retirement at the given withdrawal rate (3.3%, 4%, or 5%)
    // see documents in Resources/WithdrawalRates for more information
    private List<Integer> withdrawalAt3pt3pct;
    private List<Integer> withdrawalAt4pct;
    private List<Integer> withdrawalAt5pct;

    /**
     * Class constructor
     * @param trials    the (positive) number of trials in the simulation
     * @param member    a <code>TrsMember</code> instance
     */
    public MonteCarloSimulation(int trials, TrsMember member) {
        this.trials = trials;
        this.member = member;

        // these lists need to be concurrent safe
        withdrawalAt3pt3pct = Collections.synchronizedList(new ArrayList<>());
        withdrawalAt4pct = Collections.synchronizedList(new ArrayList<>());
        withdrawalAt5pct = Collections.synchronizedList(new ArrayList<>());
        int principal = member.getTrsAccount().getAccountBalanceAtResignation();

        // each trial begins with in initial investment of principal
        Collections.nCopies(trials, principal)
                .stream()
                .parallel()
                .forEach(x -> { // for each trial in the simulation...
                    double TdfBalance = principal;

                    // TrsMember age at which TDF investment registers first year's return
                    int firstYear = member.getTrsAccount().getCurrentAge() +
                            member.getTrsAccount().getYearsTillResignation() + 1;
                    int lastYear = Math.max(member.getTrsAccount().getRetirementAge(),
                            member.getTrsAccount().getCurrentAge() + member.getTrsAccount().getYearsTillResignation());

                    // loop through years from time of principal investment (plus 1) till retirement
                    for (int age = firstYear; age <= lastYear; age++) {
                        double stockAllocation = PiecewiseLinearFunction.VANGUARD_GLIDE_PATH.get(age);
                        double mean = PiecewiseLinearFunction.HISTORICAL_MEAN_RETURNS.get(stockAllocation);
                        double sd = PiecewiseLinearFunction.HISTORICAL_SD_RETURNS.get(stockAllocation);
                        NormalDistribution normalPDF = new NormalDistribution(mean, sd);
                        // Apply a randomly selected (according to normalPDF) annual return to the current balance
                        // (As of 3/17/23, all Vanguard TDFs have a 0.08% expense ratio.)
                        TdfBalance *= (1 + normalPDF.sample() - .0008);
                        // Freeze any trials that end up with a non-positive TDF value at 0.
                        if (TdfBalance <= 0) {
                            TdfBalance = 0;
                            break;
                        }
                    }

                    int TdfBalanceAtRetirement = (int) Math.round(TdfBalance);
                    // add the initial withdrawal amount to its corresponding list
                    withdrawalAt3pt3pct.add((int) Math.round(.033 * TdfBalanceAtRetirement));
                    withdrawalAt4pct.add((int) Math.round(.04 * TdfBalanceAtRetirement));
                    withdrawalAt5pct.add((int) Math.round(.05 * TdfBalanceAtRetirement));
                });
    }

    /**
     * @return  the list (of size <code>trials</code>) of initial withdrawal amounts from the TDF
     *          assuming a 3.3% withdrawal rate
     */
    public List<Integer> getWithdrawalAt3pt3pct() { return withdrawalAt3pt3pct; }

    /**
     * @return  the list (of size <code>trials</code>) of initial withdrawal amounts from the TDF
     *          assuming a 4% withdrawal rate
     */
    public List<Integer> getWithdrawalAt4pct() { return withdrawalAt4pct; }

    /**
     * @return  the list (of size <code>trials</code>) of initial withdrawal amounts from the TDF
     *          assuming a 5% withdrawal rate
     */
    public List<Integer> getWithdrawalAt5pct() { return withdrawalAt5pct; }

    /**
     * @return  a string summarizing the results of the simulation,
     *          intended to be printed to the console
     */
    public String toString() {
        String str = "As one alternative, you could roll your TRS account balance into an IRA upon your resignation\n" +
                "and invest that money in a Vanguard Target Date Retirement Fund (TDF). I performed a Monte Carlo\n" +
                "simulation for you with " + String.format("%,d", trials) + " trials to model those outcomes.\n\n";

        // column headers
        int width = 22;
        String sf = "%" + width + "s";
        str += String.format(sf, "withdrawal rate") + String.format("%" + (width/2 + 3) + "s", "3.3%") +
                String.format(sf, "4%") + String.format(sf, "5%") + "\n";

        // percentiles
        for (int percent : new int[]{1, 5, 25, 50}) {
            String rowHeader = String.format("%2s", percent);
            if (percent % 10 == 1 && percent != 11) {
                rowHeader += "st";
            }
            else if (percent % 10 == 2 && percent != 12) {
                rowHeader += "nd";
            }
            else if (percent % 10 == 3 && percent != 13) {
                rowHeader += "rd";
            }
            else {
                rowHeader += "th";
            }
            rowHeader += " percentile";
            str += String.format(sf, rowHeader);
            str += String.format(sf, String.format("%,d", (int) Math.round(Statistics.percentile(withdrawalAt3pt3pct, (double) percent/100)))) +
                    String.format(sf, String.format("%,d", (int) Math.round(Statistics.percentile(withdrawalAt4pct, (double) percent/100)))) +
                    String.format(sf, String.format("%,d", (int) Math.round(Statistics.percentile(withdrawalAt5pct, (double) percent/100)))) + "\n";
        }

        // means
        int confidence = 99;
        str += String.format(sf, "mean (w/ " + confidence + "% C.I.)");
        str += String.format(sf, String.format("%,d", (int) Math.round(Statistics.mean(withdrawalAt3pt3pct))) + " +/- " +
                String.format("%,d", (int) Math.round(Statistics.getMarginOfError(withdrawalAt3pt3pct, (double) confidence/100))));
        str += String.format(sf, String.format("%,d", (int) Math.round(Statistics.mean(withdrawalAt4pct))) + " +/- " +
                String.format("%,d", (int) Math.round(Statistics.getMarginOfError(withdrawalAt4pct, (double) confidence/100))));
        str += String.format(sf, String.format("%,d", (int) Math.round(Statistics.mean(withdrawalAt5pct))) + " +/- " +
                String.format("%,d", (int) Math.round(Statistics.getMarginOfError(withdrawalAt5pct, (double) confidence/100)))) +
                "\n";

        // probabilities
        String ff = "%" + (width-1) + ".1f%%";
        str += String.format(sf, "P(earning < TRS)") +
                String.format(ff, 100 * Statistics.percentBelow(withdrawalAt3pt3pct, member.getTrsAccount().getNormalBenefit())) +
                String.format(ff, 100 * Statistics.percentBelow(withdrawalAt4pct, member.getTrsAccount().getNormalBenefit())) +
                String.format(ff, 100 * Statistics.percentBelow(withdrawalAt5pct, member.getTrsAccount().getNormalBenefit()));
        return str;
    }

    /**
     * @return  a string containing HTML code summarizing the results of the simulation,
     *          intended to be printed as part of the body of a <code>TrsDecisionToolEmail</code>
     */
    public String toHtmlString() {
        String p1 = "<p>As one alternative, you could roll your TRS account balance into an IRA upon your " +
                "resignation and invest that money in a Vanguard Target Date Retirement Fund (TDF). I performed a " +
                "Monte Carlo simulation for you with " +
                String.format("%,d", trials) + " trials to model those outcomes.</p>";

        // table: column headers
        String t1 = "<center><table style=\"width:45em\">"
                    + "<tr>"
                    + "<td>withdrawal rate</td>"
                    + "<td style=\"text-align: center\"><strong>3.3%</strong></td>"
                    + "<td style=\"text-align: center\"><strong>4%</strong></td>"
                    + "<td style=\"text-align: center\"><strong>5%</strong></td>"
                    + "</tr>";
        // table: percentiles
        for (int percent : new int[]{1, 5, 25, 50}) {
            String rowHeader = "<tr><td>" + percent;
            if (percent % 10 == 1 && percent != 11) {
                rowHeader += "st";
            }
            else if (percent % 10 == 2 && percent != 12) {
                rowHeader += "nd";
            }
            else if (percent % 10 == 3 && percent != 13) {
                rowHeader += "rd";
            }
            else {
                rowHeader += "th";
            }
            rowHeader += " percentile</td>";
            t1 += rowHeader;
            t1 += "<td style=\"text-align: right\">" + "$" + String.format("%,d", (int) Math.round(Statistics.percentile(withdrawalAt3pt3pct, (double) percent/100))) + "</td>" +
                    "<td style=\"text-align: right\">" + "$" + String.format("%,d", (int) Math.round(Statistics.percentile(withdrawalAt4pct, (double) percent/100))) + "</td>" +
                    "<td style=\"text-align: right\">" + "$" + String.format("%,d", (int) Math.round(Statistics.percentile(withdrawalAt5pct, (double) percent/100))) + "</td>" +
                    "</tr>";
        }

        // table: means
        int confidence = 99;
        t1 += "<tr><td>" + "mean (w/ " + confidence + "% C.I.)" + "</td>";
        t1 += "<td style=\"text-align: right\">" + "$" + String.format("%,d", (int) Math.round(Statistics.mean(withdrawalAt3pt3pct))) + " +/- " +
                "$" + String.format("%,d", (int) Math.round(Statistics.getMarginOfError(withdrawalAt3pt3pct, (double) confidence/100)))
                + "</td>";
        t1 += "<td style=\"text-align: right\">" + "$" + String.format("%,d", (int) Math.round(Statistics.mean(withdrawalAt4pct))) + " +/- " +
                "$" + String.format("%,d", (int) Math.round(Statistics.getMarginOfError(withdrawalAt4pct, (double) confidence/100)))
                + "</td>";
        t1 += "<td style=\"text-align: right\">" + "$" + String.format("%,d", (int) Math.round(Statistics.mean(withdrawalAt5pct))) + " +/- " +
                "$" + String.format("%,d", (int) Math.round(Statistics.getMarginOfError(withdrawalAt5pct, (double) confidence/100)))
                + "</td></tr>";

        // table: probabilities
        String ff = "%.1f%%";
        t1 += "<tr><td>" + "P(earning < TRS)" + "</td>" +
                "<td style=\"text-align: right\">" + String.format(ff, 100 * Statistics.percentBelow(withdrawalAt3pt3pct, member.getTrsAccount().getNormalBenefit())) + "</td>" +
                "<td style=\"text-align: right\">" + String.format(ff, 100 * Statistics.percentBelow(withdrawalAt4pct, member.getTrsAccount().getNormalBenefit())) + "</td>" +
                "<td style=\"text-align: right\">" + String.format(ff, 100 * Statistics.percentBelow(withdrawalAt5pct, member.getTrsAccount().getNormalBenefit()))
                + "</td></tr>"
                + "</center></table>";

        // histogram image
        String i1 = "<center><img src=\"cid:image\" alt=\"histogram of Monte Carlo trial outcomes\" width=\"850\"></center>";

        String p2 = "<p>You can see the inner workings of this tool on " +
                "<a href=\"https://www.github.com/charlescbarnes/trs-decision\" target=\"_blank\" rel=\"noopener noreferrer\">GitHub</a>" +
                ", as well as browse helpful materials in the " +
                "<a href=\"https://www.github.com/charlescbarnes/trs-decision/tree/master/Resources\" target=\"_blank\" rel=\"noopener noreferrer\">Resources folder</a>" +
                ".</p>" +
                "<p>Best of luck with your decision!</p>";

        return p1 + t1 + i1 + p2;
    }
}