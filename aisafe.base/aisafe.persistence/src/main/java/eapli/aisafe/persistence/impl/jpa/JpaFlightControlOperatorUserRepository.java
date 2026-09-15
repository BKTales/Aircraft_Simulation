package eapli.aisafe.persistence.impl.jpa;

import eapli.aisafe.Application;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUser;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class JpaFlightControlOperatorUserRepository
        extends JpaAutoTxRepository<FlightControlOperatorUser, AISafeUserId, AISafeUserId>
        implements FlightControlOperatorUserRepository {

    JpaFlightControlOperatorUserRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    JpaFlightControlOperatorUserRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public Iterable<FlightControlOperatorUser> findByAreaAndActive(final AirControlArea airControlArea) {

        final Map<String, Object> params = new HashMap<>();
        params.put("active", true);
        params.put("area", airControlArea);

        return match(
                "e.systemUser.active = :active AND e.airControlArea = :area",
                params
        );
    }

    @Override
    public Optional<FlightControlOperatorUser> findByUsername(final Username username) {
        final Map<String, Object> params = new HashMap<>();
        params.put("u", username);
        return matchOne("e.systemUser.username = :u", params);
    }
}
