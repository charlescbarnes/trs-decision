import java.util.HashMap;
import java.util.Scanner;

/**
 * This class guides users through the TRS Tier Placement Map. For more information, see
 * Resources/TRS/TrsBenefitsHandbook.pdf
 *
 * @author Charlie Barnes
 */
public class TrsTierFinder {
    private static final String memberPriorTo2005 = "Were you ever a member of TRS prior to Sept. 1, 2005?";
    private static final String criteriaPriorTo2005 = """
            While a member of TRS prior to Sept. 1, 2005, did you meet any of the following?
            - At least age 50, or
            - Age and years of service credit totaled at least 70, or
            - At least 25 years of service credit""";

    private static final HashMap<String, String> tierPlacementMap = new HashMap<>() {{
        // for example, read "q1Yq2N" as "answered Yes to question 1, No to question 2"

        put("q1", "Did your current TRS membership begin prior to Sept. 1, 2014?");

        put("q1Y","Did you have at least five years of service credit on Aug. 31, 2014?");

        put("q1Yq2Y","Did your current TRS membership begin prior to Sept. 1, 2007?");
        put("q1Yq2Yq3Y",memberPriorTo2005);

        put("q1Yq2Yq3Yq4Y",criteriaPriorTo2005);
        put("q1Yq2Yq3Yq4Yq5Y","Tier 1");
        put("q1Yq2Yq3Yq4Yq5N","Tier 2");
        put("q1Yq2Yq3Yq4N","Tier 2");

        put("q1Yq2Yq3N",memberPriorTo2005);
        put("q1Yq2Yq3Nq4Y",criteriaPriorTo2005);
        put("q1Yq2Yq3Nq4Yq5Y","Tier 4");
        put("q1Yq2Yq3Nq4Yq5N","Tier 3");
        put("q1Yq2Yq3Nq4N","Tier 3");

        put("q1Yq2N",memberPriorTo2005);
        put("q1Yq2Nq3Y",criteriaPriorTo2005);
        put("q1Yq2Nq3Yq4Y","Tier 6");
        put("q1Yq2Nq3Yq4N","Tier 5");

        put("q1Yq2Nq3N","Tier 5");

        put("q1N",memberPriorTo2005);
        put("q1Nq2Y",criteriaPriorTo2005);
        put("q1Nq2Yq3Y","Tier 6");
        put("q1Nq2Yq3N","Tier 5");
        put("q1Nq2N","Tier 5");
    }};

    /**
     * Class constructor
     */
    public TrsTierFinder() {}

    /**
     * runs the user through the TRS Tier Placement Map
     * @return the user's TRS Tier, as an <code>int</code>
     */
    public int run() {
        int questionNumber = 1;
        String flowchartPath = "q1";

        Scanner scan = new Scanner(System.in);

        // while the value in tierPlacementMap does not contain your tier number...
        while (!tierPlacementMap.get(flowchartPath).contains("Tier ")) {
            System.out.println(tierPlacementMap.get(flowchartPath));
            // if we're not on question one, append the current question number
            if (!flowchartPath.endsWith("1")) {
                flowchartPath += "q" + questionNumber;
            }
            System.out.print("Enter \"Y\" or \"N\": ");

            String answer;
            boolean answerValid = false;
            while (!answerValid) {
                answer = scan.next().trim();
                // check whether the user entered a valid option. If not, repeat prompt.
                if (answer.equals("Y") || answer.equals("N")) {
                    answerValid = true;
                    // append the answer (Y/N)
                    flowchartPath += answer;
                    questionNumber++;
                } else {
                    System.out.print("Enter \"Y\" or \"N\": ");
                }
            }
        }
        String tierString = tierPlacementMap.get(flowchartPath);
        System.out.println("Got it. You're in " + tierString + ".");
        return Integer.parseInt(tierString.substring(tierString.length()-1));
    }
}
