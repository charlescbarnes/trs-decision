import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class TrsAccount {
    private int currentAge;
    private int tier;
    private int yearsOfService;
    private int yearsTillResignation;
    private int retirementAge;
    private int numHighestSalaries;
    private int[] highestSalaries;
    public static final double ANNUAL_SALARY_INCREASE = 0.02;
    public static final double TRS_CONTRIBUTION = 0.0825;
    public static final double TRS_INTEREST = 0.02;
    private static final int AISD_SUPT_SALARY = 350000;
    private int[] futureSalariesTillResignation;
    private int[] highestSalariesAtResignation;
    private int accountBalance;
    private int accountBalanceAtResignation;

    /**
     * Class constructor
     * @param currentAge    the <code>TrsMember</code>'s age in years, as an <code>int</code>
     */
    public TrsAccount(int currentAge) {
        this.currentAge = currentAge;
    }

    public int getCurrentAge() { return currentAge; }
    public int getTier() { return tier; }

    /**
     * Sets the Tier number associated with this <code>TrsAccount</code>.
     * This method must be called before many other <code>TrsAccount</code> setters.
     * @param tier the TRS tier number, as an <code>int</code>
     * @throws IllegalArgumentException if <code>tier</code> is not a valid tier number (1, 2, 3, 4, 5, or 6)
     */
    public void setTier(int tier) {
        if (tier < 1 || tier > 6) throw new IllegalArgumentException("tier must be 1, 2, 3, 4, 5, or 6");
        this.tier = tier;
        setNumHighestSalaries();
    }

    public int getYearsOfService() { return yearsOfService; }

    public void setYearsOfService(int yearsOfService) {
        if (!isValidYearsOfService(String.valueOf(yearsOfService))) {
            throw new IllegalArgumentException("years of service must be non-negative and " +
                    "less than currentAge minus 20");
        }
        this.yearsOfService = yearsOfService; }

    public int getYearsTillResignation() { return yearsTillResignation; }

    public void setYearsTillResignation(int yearsTillResignation) {
        if (!isValidYearsTillResignation(String.valueOf(yearsTillResignation))) {
            throw new IllegalArgumentException("years till resignation must be non-negative and " +
                    "less than 100 minus currentAge");
        }
        this.yearsTillResignation = yearsTillResignation;
        setRetirementAge();
    }

    public int getRetirementAge() { return retirementAge; }

    public void setRetirementAge() {
        retirementAge = -1;
        int ageRuleOf80Met = 80;
        if (currentAge + yearsOfService >= 80) {
            ageRuleOf80Met = currentAge;
        }
        else {
            ageRuleOf80Met -= (yearsOfService + yearsTillResignation);
        }
        if (yearsOfService + yearsTillResignation >= 5) {
            if (tier == 1 || tier == 2) {
                retirementAge = Math.min(65, ageRuleOf80Met);
            }
            else if (tier == 3 || tier == 4) {
                retirementAge = Math.min(65, Math.max(60, ageRuleOf80Met));
            }
            else {
                retirementAge = Math.min(65, Math.max(62, ageRuleOf80Met));
            }
        }
    }

    public void setNumHighestSalaries() {
        if (tier == 1 || tier == 4 || tier == 6) {
            numHighestSalaries = 3;
        }
        else if (tier == 2 || tier == 3 || tier == 5) {
            numHighestSalaries = 5;
        }
    }

    /**
     * @return an <code>int</code> array of highest salaries, listed in descending order
     */
    public int[] getHighestSalaries() {
        return Arrays.stream(highestSalaries)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .mapToInt(i -> i)
                .toArray();
    }

    public void setHighestSalaries(int[] highestSalaries) {
        if (highestSalaries.length != numHighestSalaries) throw new IllegalArgumentException("highestSalaries array " +
                "must be length " + numHighestSalaries + " for tier " + tier + " TRS members");
        for (int salary : highestSalaries) {
            if (salary < 0 || salary > AISD_SUPT_SALARY) {
                throw new IllegalArgumentException("salaries must be non-negative and no more than the Austin ISD " +
                        "superintendent's salary");
            }
        }
        this.highestSalaries = highestSalaries;
    }

    public void setFutureSalariesTillResignation() {
        int[] currSalaries = getHighestSalaries();
        futureSalariesTillResignation = new int[yearsTillResignation];
        for (int i = 1; i <= yearsTillResignation; i++) {
            futureSalariesTillResignation[yearsTillResignation - i] = (int) Math.round(currSalaries[0] * Math.pow(1 + ANNUAL_SALARY_INCREASE, i));
        }
    }

    public int[] getFutureSalariesTillResignation() {
        setFutureSalariesTillResignation();
        return Arrays.stream(futureSalariesTillResignation)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .mapToInt(i -> i)
                .toArray();
    }

    public void setHighestSalariesAtResignation() {
        highestSalariesAtResignation = new int[numHighestSalaries];
        int numFutureSalaries = getFutureSalariesTillResignation().length;
        for (int i = 0; i < numHighestSalaries; i++) {
            // as many (of the highest) future salaries that you can...
            if (i < numFutureSalaries) {
                highestSalariesAtResignation[i] = futureSalariesTillResignation[i];
            }
            // before including previous highest salaries
            else {
                highestSalariesAtResignation[i] = highestSalaries[i - numFutureSalaries];
            }
        }
    }

    public int[] getHighestSalariesAtResignation() {
        setHighestSalariesAtResignation();
        return Arrays.stream(highestSalariesAtResignation)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .mapToInt(i -> i)
                .toArray();
    }

    public String salaryArrayToString(int[] salaryArray) {
        String salaries = "";
        for (int i = 0; i < salaryArray.length; i++) {
            salaries += "$" + String.format("%,d", salaryArray[i]);
            if (i < salaryArray.length - 1) {
                salaries += ", ";
            }
        }
        return salaries;
    }
    public int getAccountBalance() { return accountBalance; }
    public void setAccountBalance(int accountBalance) {
        if (accountBalance < 0) throw new IllegalArgumentException("accountBalance must be non-negative");
        this.accountBalance = accountBalance;
    }

    /**
     * Predicts the TRS account balance at the time of resignation and sets
     * <code>accountBalanceAtResignation</code> accordingly
     */
    public void setAccountBalanceAtResignation() {
        // start with the current account balance
        accountBalanceAtResignation = accountBalance;
        // for every year prior to resignation...
        for (int i = 0; i < yearsTillResignation; i++) {
            // add contribution withheld from salary
            accountBalanceAtResignation += futureSalariesTillResignation[i] * TRS_CONTRIBUTION;
            // and add interest on the account balance
            accountBalanceAtResignation *= (1 + TRS_INTEREST);
        }
    }

    public int getAccountBalanceAtResignation() {
        setAccountBalanceAtResignation();
        return accountBalanceAtResignation;
    }

    public void initializeTrsAccount() {
        Scanner scan = new Scanner(System.in);

        // tier
        System.out.print("Enter your tier number (1, 2, 3, 4, 5, or 6), " +
                "or 0 if you don't know your tier: ");
        String tierSelectionString;
        boolean tierSelectionValid = false;
        int tierSelection;
        while (!tierSelectionValid) {
            tierSelectionString = scan.next().trim();
            // check whether the user entered a valid option. If not, repeat prompt.
            try {
                tierSelection = Integer.parseInt(tierSelectionString);
                if (tierSelection >= 1 && tierSelection <= 6) {
                    tierSelectionValid = true;
                    setTier(tierSelection);
                }
                else if (tierSelection == 0) {
                    tierSelectionValid = true;
                    TrsTierFinder tierFinder = new TrsTierFinder();
                    setTier(tierFinder.run());
                }
                else {
                    System.out.print("Enter a valid tier number (1, 2, 3, 4, 5, or 6), " +
                            "or 0 if you don't know your tier: ");
                }
            }
            catch (NumberFormatException e) {
                System.out.print("Enter a valid tier number (1, 2, 3, 4, 5, or 6), " +
                        "or 0 if you don't know your tier: ");
            }
        }

        // years of service
        String yearsOfServiceString = "";
        while (!isValidYearsOfService(yearsOfServiceString)) {
            System.out.print("Enter your current number of years of service: ");
            yearsOfServiceString = scan.next().trim();
        }

        // years till resignation
        String yearsTillResignationString = "";
        while (!isValidYearsTillResignation(yearsTillResignationString)) {
            System.out.print("Enter a number of years until your resignation: ");
            yearsTillResignationString = scan.next().trim();
        }

        // highest salaries
        highestSalaries = new int[numHighestSalaries];
        System.out.println("Enter your highest " + numHighestSalaries + " salaries (in any order):");
        for (int i = 0; i < numHighestSalaries; i++) {
            String highSalaryString = "";
            while (!isValidSalary(highSalaryString, i)) {
                System.out.print("Salary " + (i+1) + ": ");
                highSalaryString = scan.next().trim();
            }
        }

        // account balance
        String accountBalanceString = "";
        while (!isValidAccountBalance(accountBalanceString)) {
            System.out.print("Enter your current TRS account balance: ");
            accountBalanceString = scan.next().trim();
        }
    }

    /**
     * data validation method for <code>yearsOfService</code>
     * @param yearsOfServiceString a string, which potentially parses to <code>yearsOfService</code>
     * @return  <code>true</code> if <code>yearsOfServiceString</code> parses to a sensible <code>yearsOfService</code>
     *          <code>int</code> (and sets <code>yearsOfService</code>); <code>false</code> otherwise
     */
    public boolean isValidYearsOfService(String yearsOfServiceString) {
        try {
            int possYearsOfService = Integer.parseInt(yearsOfServiceString);
            if (0 <= possYearsOfService && possYearsOfService < currentAge - 20) {
                yearsOfService = possYearsOfService;
                return true;
            }
            else {
                return false;
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * data validation method for <code>yearsTillResignation</code>
     * @param yearsTillResignationString a string, which potentially parses to <code>yearsTillResignation</code>
     * @return  <code>true</code> if <code>yearsTillResignationString</code> parses to a sensible
     *          <code>yearsTillResignation</code> <code>int</code> (and sets <code>yearsTillResignation</code>);
     *          <code>false</code> otherwise
     */
    public boolean isValidYearsTillResignation(String yearsTillResignationString) {
        try {
            int possYearsTillResignation = Integer.parseInt(yearsTillResignationString);
            if (0 <= possYearsTillResignation && currentAge + possYearsTillResignation < 100) {
                yearsTillResignation = possYearsTillResignation;
                setRetirementAge();
                return true;
            }
            else {
                return false;
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * data validation method for salaries
     * @param highSalaryString  a string, which potentially parses to a salary <code>int</code>
     * @param i                 salary index in <code>highestSalaries</code> array
     * @return  <code>true</code> if <code>highSalaryString</code> parses to a sensible salary
     *          <code>int</code> (and sets <code>highestSalaries[i]</code>); <code>false</code> otherwise
     */
    public boolean isValidSalary(String highSalaryString, int i) {
        try {
            int possHighSalary = Integer.parseInt(highSalaryString
                    .replaceAll("[$]", "")      // strip all dollar signs
                    .replaceAll(",", "")        // strip any commas
                    .replaceAll("[.].*", "")    // strip any number of cents
            );
            if (0 <= possHighSalary && possHighSalary <= AISD_SUPT_SALARY) { // accepts AISD supt. salary
                highestSalaries[i] = possHighSalary;
                return true;
            }
            else {
                return false;
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * data validation method for <code>accountBalance</code>
     * @param accountBalanceString a string, which potentially parses to <code>accountBalance</code>
     * @return  <code>true</code> if <code>accountBalanceString</code> parses to a sensible <code>accountBalance</code>
     *          <code>int</code> (and sets <code>accountBalance</code>); <code>false</code> otherwise
     */
    public boolean isValidAccountBalance(String accountBalanceString) {
        try {
            int possAccountBalance = Integer.parseInt(accountBalanceString
                    .replaceAll("[$]", "")      // strip all dollar signs
                    .replaceAll(",", "")        // strip any commas
                    .replaceAll("[.].*", "")    // strip any number of cents
            );
            if (0 <= possAccountBalance) {
                accountBalance = possAccountBalance;
                return true;
            }
            else {
                return false;
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * @return the annual TRS annuity for this <code>TrsAccount</code>, as an <code>int</code>
     */
    public int getNormalBenefit() {
        double averageSalary = Arrays.stream(getHighestSalariesAtResignation()).average().getAsDouble();
        double totalPercent = (yearsOfService + yearsTillResignation) * .023;
        return (int) Math.round(averageSalary * totalPercent);
    }

    public String normalBenefitToString() {
        String annualAnnuityString = "$" + String.format("%,d", getNormalBenefit());
        return "You will be eligible for normal-age retirement at age " + retirementAge + ". " +
                "Your annual TRS annuity will be: " + annualAnnuityString + ".";
    }
}