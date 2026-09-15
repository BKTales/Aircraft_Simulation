package eapli.aisafe.aircontrolarea.repositories;

import eapli.aisafe.Application;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

public class JpaAirControlAreaRepository
        extends JpaAutoTxRepository<AirControlArea, AreaCode, AreaCode>
        implements AirControlAreaRepository {

    public JpaAirControlAreaRepository(final TransactionalContext autoTx) {
        super(autoTx, "areaCode");
    }

    public JpaAirControlAreaRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "areaCode");
    }

}