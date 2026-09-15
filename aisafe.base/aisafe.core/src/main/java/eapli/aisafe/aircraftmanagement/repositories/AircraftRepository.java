package eapli.aisafe.aircraftmanagement.repositories;

import eapli.aisafe.aircraftmanagement.domain.Aircraft;
import eapli.aisafe.aircraftmanagement.domain.AircraftRegistration;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModelId;
import eapli.aisafe.airtransportcompanymanagement.domain.IATACode;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface AircraftRepository extends DomainRepository<AircraftRegistration, Aircraft> {

    default boolean existsByRegistration(final AircraftRegistration registration) {
        return ofIdentity(registration).isPresent();
    }

    default Optional<Aircraft> findByRegistration(final AircraftRegistration registration) {
        return ofIdentity(registration);
    }

    /**
     * Active aircraft owned by the given air transport company (IATA).
     */
    default Iterable<Aircraft> findActiveByOwnerCompany(final IATACode ownerIata) {
        Objects.requireNonNull(ownerIata);
        final List<Aircraft> result = new ArrayList<>();
        for (final Aircraft a : findAll()) {
            if (a.ownerCompanyIata().equals(ownerIata) && a.isActive()) {
                result.add(a);
            }
        }
        return result;
    }

    /**
     * All aircraft owned by the company (active and decommissioned).
     */
    default Iterable<Aircraft> findByOwnerCompany(final IATACode ownerIata) {
        Objects.requireNonNull(ownerIata);
        final List<Aircraft> result = new ArrayList<>();
        for (final Aircraft a : findAll()) {
            if (a.ownerCompanyIata().equals(ownerIata)) {
                result.add(a);
            }
        }
        return result;
    }

    default Iterable<Aircraft> findByOwnerCompanyAndModel(final IATACode ownerIata,
                                                          final AircraftModelId modelId) {
        Objects.requireNonNull(ownerIata);
        Objects.requireNonNull(modelId);
        final List<Aircraft> result = new ArrayList<>();
        for (final Aircraft a : findByOwnerCompany(ownerIata)) {
            if (a.aircraftModelId().equals(modelId)) {
                result.add(a);
            }
        }
        return result;
    }

    default Iterable<Aircraft> findByOwnerCompanyAndCapacity(final IATACode ownerIata,
                                                             final int passengerCapacity) {
        Objects.requireNonNull(ownerIata);
        final List<Aircraft> result = new ArrayList<>();
        for (final Aircraft a : findByOwnerCompany(ownerIata)) {
            if (a.cabinConfiguration().totalSeats() == passengerCapacity) {
                result.add(a);
            }
        }
        return result;
    }

    default Iterable<Aircraft> findByOwnerCompanyAndAge(final IATACode ownerIata, final int ageInYears) {
        Objects.requireNonNull(ownerIata);
        final List<Aircraft> result = new ArrayList<>();
        for (final Aircraft a : findByOwnerCompany(ownerIata)) {
            if (a.ageInYears() == ageInYears) {
                result.add(a);
            }
        }
        return result;
    }
}
