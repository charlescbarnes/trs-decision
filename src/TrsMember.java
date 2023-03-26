import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class TrsMember {
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private TrsAccount trsAccount;

    /**
     * Class constructor
     * @param name                  member name, as a <code>String</code>
     * @param email                 member email, as a <code>String</code>
     * @param dateOfBirthMMDDYYYY   member birthdate in the "MM/DD/YYYY" format, as a <code>String</code>
     */
    public TrsMember(String name, String email, String dateOfBirthMMDDYYYY) {
        this.name = name;
        if (email != null && !email.equals("") && !isValidEmailAddress(email)) {
            throw new IllegalArgumentException("invalid email address");
        }
        this.email = email;
        if (!isValidBirthDate(dateOfBirthMMDDYYYY).getKey()) {
            throw new IllegalArgumentException("invalid birth date--must be a valid date in the form MM/DD/YYYY");
        }
        this.dateOfBirth = isValidBirthDate(dateOfBirthMMDDYYYY).getValue();
        int currentAge = Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
        trsAccount = new TrsAccount(currentAge);
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public TrsAccount getTrsAccount() { return trsAccount; }

    public static TrsMember initializeTrsMember() {
        Scanner scan = new Scanner(System.in);

        // get name
        System.out.print("Enter your name: ");
        String name = scan.nextLine().trim();

        // get email address
        System.out.print("Enter your email address (or, if you prefer not to, just hit return): ");
        String email = scan.nextLine().trim();
        while (!email.equals("") && !isValidEmailAddress(email)) {
            System.out.print("Enter a valid email address (or just hit return): ");
            email = scan.nextLine().trim();
        }

        // get birthdate
        System.out.print("Enter your date of birth in the form MM/DD/YYYY: ");
        String birthDate = scan.nextLine().trim();
        while (!isValidBirthDate(birthDate).getKey()) {
            System.out.print("Enter a valid birth date (MM/DD/YYYY): ");
            birthDate = scan.nextLine().trim();
        }
        return new TrsMember(name, email, birthDate);
    }

    /**
     * data validation method for <code>email</code>
     * @param possEmailAddress a <code>String</code>, which might be a valid email address
     * @return  <code>true</code> if <code>possEmailAddress</code> is a valid email address; <code>false</code> otherwise
     */
    public static boolean isValidEmailAddress(String possEmailAddress) {
        boolean result = true;
        try {
            InternetAddress emailAddress = new InternetAddress(possEmailAddress);
            emailAddress.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * @param possBirthDate a <code>String</code>, intended to be in the "MM/DD/YYYY" format,
     *                      which might be a valid email address
     * @return a <code>Map.Entry<Boolean, LocalDate></code>, where:
     *              - the Boolean key is <code>true</code> if and only if <code>possBirthDate</code> was a valid birthdate
     *              - the value is:
     *                  a <code>LocalDate</code> representation of <code>possBirthDate</code> if the key is true
     *                  <code>null</code> otherwise
     */
    public static Map.Entry<Boolean, LocalDate> isValidBirthDate(String possBirthDate) {
        Map.Entry<Boolean, LocalDate> boolDate = new AbstractMap.SimpleEntry(false, null);
        if (possBirthDate.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})")){
            String[] monthDayYear = possBirthDate.split("/");
            int month = Integer.parseInt(monthDayYear[0]);
            if (1 <= month && month <= 12) {
                int year = Integer.parseInt(monthDayYear[2]);
                if (java.time.LocalDate.now().getYear() - year < 100) {
                    int day = Integer.parseInt(monthDayYear[1]);
                    if (day >= 1) {
                        if (Arrays.asList(new int[]{1,3,5,7,8,10,12}).contains(month)) {
                            boolDate = new AbstractMap.SimpleEntry<>(day <= 31,
                                        java.time.LocalDate.of(year, month, day)
                                    );
                        }
                        else if (Arrays.asList(new int[]{4,6,9,11}).contains(month)) {
                            boolDate = new AbstractMap.SimpleEntry<>(day <= 30,
                                    java.time.LocalDate.of(year, month, day)
                            );                        }
                        else { // february
                            if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0)) {
                                boolDate = new AbstractMap.SimpleEntry<>(day <= 29,
                                        java.time.LocalDate.of(year, month, day)
                                );                            }
                            else {
                                boolDate = new AbstractMap.SimpleEntry<>(day <= 28,
                                        java.time.LocalDate.of(year, month, day)
                                );                            }
                        }
                    }
                }
            }
        }
        return boolDate;
    }

    /**
     * @return  a string containing HTML code summarizing the <code>TrsMember</code> (and associated
     *          <code>TrsAccount</code>) data, intended to be printed as part of the body of a
     *          <code>TrsDecisionToolEmail</code>
     */
    public String toHtmlString() {
        String str = "<p>Hi " + name + ",</p>";
        str += "<p>You provided the following data:<ul>";
        str += "<li>DOB: " + dateOfBirth + "</li>";
        str += "<li>TRS Tier: " + getTrsAccount().getTier() + "</li>";
        str += "<li>Years of service: " + getTrsAccount().getYearsOfService() + "</li>";
        str += "<li>Years till resignation: " + getTrsAccount().getYearsTillResignation() + "</li>";
        str += "<li>Highest salaries: " + getTrsAccount().salaryArrayToString(getTrsAccount().getHighestSalaries()) + "</li>";
        if (getTrsAccount().getYearsTillResignation() > 0) {
            str += "<ul><li>" +
                    "Projected highest salaries at resignation " +
                    "(assumes a " + String.format("%.0f%%", 100 * TrsAccount.ANNUAL_SALARY_INCREASE) +
                    " annual increase to previous highest salary): " +
                    getTrsAccount().salaryArrayToString(getTrsAccount().getHighestSalariesAtResignation()) +
                    "</li></ul>";
        }
        str += "<li>TRS Account Balance: " + "$" + String.format("%,d", getTrsAccount().getAccountBalance()) + "</li>";
        if (getTrsAccount().getYearsTillResignation() > 0) {
            str += "<ul><li>" +
                    "Projected balance at resignation " +
                    "(assumes salary increases described above, an " +
                    String.format("%.2f%%", 100 * TrsAccount.TRS_CONTRIBUTION) +
                    " member contribution rate, and a " +
                    String.format("%.1f%%", 100 * TrsAccount.TRS_INTEREST) +
                    " account interest rate): " +
                    "$" + String.format("%,d", getTrsAccount().getAccountBalanceAtResignation()) +
                    "</li></ul>";
        }
        str += "</ul>" + getTrsAccount().normalBenefitToString() + "</p>";
        return str;
    }
}