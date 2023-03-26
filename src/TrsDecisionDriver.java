public class TrsDecisionDriver {
    public static void main(String[] args) {
        TrsMember member;

        // either collect TRS member info this way...
//        member = new TrsMember("Jane Doe", "janesusername@gmail.com", "01/01/1990");
//        member.getTrsAccount().setTier(5);
//        member.getTrsAccount().setYearsOfService(10);
//        member.getTrsAccount().setYearsTillResignation(0);
//        member.getTrsAccount().setHighestSalaries(new int[]{69083, 66384, 66384, 63770, 57502});
//        member.getTrsAccount().setAccountBalance(42373);

        // or this way...
        member = TrsMember.initializeTrsMember();
        member.getTrsAccount().initializeTrsAccount();

        // print TRS normal retirement info to the console
        System.out.println(member.getTrsAccount().normalBenefitToString());
        System.out.println();

        // run the Monte Carlo simulation
        MonteCarloSimulation simForMember = new MonteCarloSimulation(100000, member);

        // print results of the Monte Carlo simulation to the console
        System.out.println(simForMember);
        System.out.println();

        // draw and save a histogram of Monte Carlo simulation trials
        TrsHistogram hist = new TrsHistogram(simForMember.getWithdrawalAt3pt3pct(),
                member.getTrsAccount().getNormalBenefit());
        hist.draw();
        StdDraw.save(TrsHistogram.HISTOGRAM_FILE_PATH);

        // email the end user the results
        TrsDecisionToolEmail.runSender(member, simForMember);
    }
}
