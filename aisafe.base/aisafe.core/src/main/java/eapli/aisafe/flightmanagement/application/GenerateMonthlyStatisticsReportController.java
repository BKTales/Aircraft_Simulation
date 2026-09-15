package eapli.aisafe.flightmanagement.application;

import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.flightmanagement.application.reporting.MonthlyReportResult;
import eapli.aisafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.YearMonth;

@UseCaseController
public final class GenerateMonthlyStatisticsReportController {

    private final AuthorizationService authz;
    private final GenerateMonthlyStatisticsReportService service;
    private final FlightControlOperatorUserRepository operators;

    public GenerateMonthlyStatisticsReportController() {
        this(AuthzRegistry.authorizationService(),
                new GenerateMonthlyStatisticsReportService(),
                PersistenceContext.repositories().flightOperators());
    }

    GenerateMonthlyStatisticsReportController(final AuthorizationService authz,
                                              final GenerateMonthlyStatisticsReportService service,
                                              final FlightControlOperatorUserRepository operators) {
        if (authz == null || service == null || operators == null) {
            throw new IllegalArgumentException("Authorization service, report service and operator repository are required.");
        }
        this.authz = authz;
        this.service = service;
        this.operators = operators;
    }

    public String assignedAreaCode() {
        return FlightControlOperatorSession.requireFlightControlOperator(authz, operators)
                .airControlArea()
                .identity()
                .toString();
    }

    public MonthlyReportResult generate(final int year, final int month) {
        final String areaCode = assignedAreaCode();
        return service.generateMonthlyReport(areaCode, YearMonth.of(year, month));
    }
}
