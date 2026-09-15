package eapli.aisafe.flightmanagement.application.reporting;

public final class Us112SummaryFixtures {

    private Us112SummaryFixtures() {
    }

    public static String passSummary(final String generatedAt) {
        return """
                ================================================================================
                 AISafe SIMULATION SUMMARY REPORT
                ================================================================================
                 Area              : AREA-0
                 Interval          : 2026-05-26 00:00 -> 2026-08-26 23:59
                 Generated at      : %s

                 FINAL RESULT      : PASS
                 Total flights     : 5
                 Completed (SUCCESS): 5
                 Failed execution  : 0

                 FLIGHT EXECUTION STATUS
                   789013  SUCCESS

                 SAFETY VIOLATIONS (0)
                 No safety violations recorded.
                ================================================================================
                """.formatted(generatedAt);
    }

    public static String failSummary(final String generatedAt) {
        return """
                ================================================================================
                 AISafe SIMULATION SUMMARY REPORT
                ================================================================================
                 Area              : AREA-0
                 Interval          : 2026-06-01 09:00 -> 2026-06-01 18:00
                 Generated at      : %s

                 FINAL RESULT      : FAIL
                 Total flights     : 4
                 Completed (SUCCESS): 3
                 Failed execution  : 1

                 FLIGHT EXECUTION STATUS
                   789013  SUCCESS
                   456789  COLLISION

                 SAFETY VIOLATIONS (3)
                   [1] COLLISION — step 42
                ================================================================================
                """.formatted(generatedAt);
    }
}
