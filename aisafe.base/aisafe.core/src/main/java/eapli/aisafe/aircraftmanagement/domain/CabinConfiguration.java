package eapli.aisafe.aircraftmanagement.domain;

import eapli.aisafe.aircraftmodelmanagement.domain.NumberOfSeats;
import eapli.framework.domain.model.DomainEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * Cabin layout (seats per class) for a specific aircraft instance.
 * Persisted as its own entity and referenced by {@link Aircraft}.
 */
@Entity
@Table(name = "CABIN_CONFIGURATION")
public class CabinConfiguration implements DomainEntity<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "seats", column = @Column(name = "SEATS_ECONOMY", nullable = false))
    })
    private NumberOfSeats economySeats;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "seats", column = @Column(name = "SEATS_BUSINESS", nullable = false))
    })
    private NumberOfSeats businessSeats;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "seats", column = @Column(name = "SEATS_FIRST", nullable = false))
    })
    private NumberOfSeats firstClassSeats;

    protected CabinConfiguration() {
        // ORM
    }

    public CabinConfiguration(final NumberOfSeats economySeats,
                              final NumberOfSeats businessSeats,
                              final NumberOfSeats firstClassSeats) {
        if (economySeats == null || businessSeats == null || firstClassSeats == null) {
            throw new IllegalArgumentException("Seat counts for all cabin classes are required.");
        }
        this.economySeats = economySeats;
        this.businessSeats = businessSeats;
        this.firstClassSeats = firstClassSeats;
    }

    /**
     * Builds a cabin layout from raw integers (non-negative), wrapped as {@link NumberOfSeats}.
     */
    public static CabinConfiguration ofEconomyBusinessFirst(final int economy,
                                                            final int business,
                                                            final int firstClass) {
        return new CabinConfiguration(
                NumberOfSeats.valueOf(economy),
                NumberOfSeats.valueOf(business),
                NumberOfSeats.valueOf(firstClass));
    }

    public NumberOfSeats economySeats() {
        return economySeats;
    }

    public NumberOfSeats businessSeats() {
        return businessSeats;
    }

    public NumberOfSeats firstClassSeats() {
        return firstClassSeats;
    }

    public int totalSeats() {
        return economySeats.seats() + businessSeats.seats() + firstClassSeats.seats();
    }

    @Override
    public Long identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {
        return equals(other);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CabinConfiguration)) {
            return false;
        }
        final CabinConfiguration that = (CabinConfiguration) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(economySeats, that.economySeats)
                && Objects.equals(businessSeats, that.businessSeats)
                && Objects.equals(firstClassSeats, that.firstClassSeats);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id)
                : Objects.hash(economySeats, businessSeats, firstClassSeats);
    }
}
