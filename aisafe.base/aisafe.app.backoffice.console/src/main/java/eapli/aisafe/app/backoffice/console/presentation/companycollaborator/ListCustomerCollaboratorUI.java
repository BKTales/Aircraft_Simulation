package eapli.aisafe.app.backoffice.console.presentation.companycollaborator;


import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.SelectWidget;

import java.util.List;


public class ListCustomerCollaboratorUI extends AbstractUI {

    private static final String[] CUSTOMER_TYPES = {
            "Air Transport Company",
            "Air Control Area"
    };

    @Override
    protected boolean doShow() {
        final SelectWidget<String> selector =
                new SelectWidget<>("Customer type:", List.of(CUSTOMER_TYPES));

        selector.show();
        final String selectedType = selector.selectedElement();

        if (selectedType == null) {
            return false;
        }

        if (selectedType.equals(CUSTOMER_TYPES[0])) {
            new ListCompanyCollaboratorUsersUI().show();
        } else {
            new ListFlightControlOperatorUsersUI().show();
        }

        return false;
    }


    @Override
    public String headline() {
        return "";
    }
}
