package eapli.aisafe.persistence.impl.inmemory;
import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.flightcontroloperatormanagement.domain.FlightControlOperatorUser;
import eapli.aisafe.flightcontroloperatormanagement.repositories.FlightControlOperatorUserRepository;
import eapli.aisafe.usermanagement.domain.AISafeUserId;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryFlightControlOperatorUserRepository
        extends InMemoryDomainRepository<FlightControlOperatorUser, AISafeUserId>
        implements FlightControlOperatorUserRepository {

    @Override
    public Iterable<FlightControlOperatorUser> findByAreaAndActive(final AirControlArea airControlArea) {
        return data().values().stream()
                .filter(c -> c.airControlArea().equals(airControlArea) && c.systemUser().isActive())
                .toList();
    }

    @Override
    public Optional<FlightControlOperatorUser> findByUsername(final Username username) {
        return data().values().stream()
                .filter(c -> c.systemUser().username().equals(username))
                .findFirst();
    }
}
