package eapli.aisafe.aircontrolarea.repositories;

import eapli.aisafe.aircontrolarea.domain.AirControlArea;
import eapli.aisafe.aircontrolarea.domain.AreaCode;
import eapli.aisafe.aircontrolarea.repositories.AirControlAreaRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

public class InMemoryAirControlAreaRepository
        extends InMemoryDomainRepository<AirControlArea, AreaCode>
        implements AirControlAreaRepository {

}