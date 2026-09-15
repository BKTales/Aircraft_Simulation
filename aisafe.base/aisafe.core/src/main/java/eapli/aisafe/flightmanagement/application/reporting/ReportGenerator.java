package eapli.aisafe.flightmanagement.application.reporting;

public interface ReportGenerator<RQ, RS> {

    RS generate(RQ request);
}
