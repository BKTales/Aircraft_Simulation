package eapli.aisafe.persistence.impl.inmemory;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmanagement.repositories.AircraftRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

public class InMemoryAircraftRepository
        extends InMemoryDomainRepository<Aircraft, AircraftRegistration>
        implements AircraftRepository {
}
