package eapli.aisafe.app.backoffice.console.presentation.flightcontrol;

import eapli.framework.actions.Action;

public class GenerateMonthlyStatisticsReportAction implements Action {

    @Override
    public boolean execute() {
        return new GenerateMonthlyStatisticsReportUI().show();
    }
}
