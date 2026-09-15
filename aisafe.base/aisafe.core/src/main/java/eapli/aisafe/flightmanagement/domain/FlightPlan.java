package eapli.aisafe.flightmanagement.domain;

import eapli.framework.domain.model.DomainEntities;
import eapli.framework.domain.model.DomainEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.io.Serializable;

/**
 * Flight plan entity (part of the {@link Flight} aggregate). Persisted in {@code FLIGHT_PLAN}.
 */
@Entity
@Table(name = "FLIGHT_PLAN")
public class FlightPlan implements DomainEntity<FlightPlanId>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private FlightPlanId id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private FlightPlanStatus status;

    @Embedded
    private FuelLoad fuelLoad;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DSL_CONTENT")
    private String dslContent;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "JSON_CONTENT", nullable = false)
    private String jsonContent;

    protected FlightPlan() {
        // ORM
    }

    private FlightPlan(final FlightPlanId id,
                       final FlightPlanStatus status,
                       final FuelLoad fuelLoad,
                       final String dslContent,
                       final String jsonContent) {
        if (id == null) {
            throw new IllegalArgumentException("Flight plan id is required.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required.");
        }
        if (fuelLoad == null) {
            throw new IllegalArgumentException("Fuel load is required.");
        }
        if (jsonContent == null || jsonContent.isBlank()) {
            throw new IllegalArgumentException("JSON content is required.");
        }
        this.id = id;
        this.status = status;
        this.fuelLoad = fuelLoad;
        this.dslContent = dslContent;
        this.jsonContent = jsonContent;
    }

    public static FlightPlan forFlight(final FlightDesignator flight,
                                       final FlightPlanStatus status,
                                       final FuelLoad fuelLoad,
                                       final String jsonContent) {
        return forFlight(flight, status, fuelLoad, null, jsonContent);
    }

    public static FlightPlan forFlight(final FlightDesignator flight,
                                       final FlightPlanStatus status,
                                       final FuelLoad fuelLoad,
                                       final String dslContent,
                                       final String jsonContent) {
        return new FlightPlan(FlightPlanId.of(flight), status, fuelLoad, dslContent, jsonContent);
    }

    @Override
    public FlightPlanId identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    public FlightPlanStatus status() {
        return status;
    }

    public FuelLoad fuelLoad() {
        return fuelLoad;
    }

    public String dslContent() {
        return dslContent;
    }

    public String jsonContent() {
        return jsonContent;
    }

    public boolean hasJsonContent() {
        return jsonContent != null && !jsonContent.isBlank();
    }

    public boolean hasDslContent() {
        return dslContent != null && !dslContent.isBlank();
    }

    /**
     * Updates lifecycle status in place (same aggregate version row).
     */
    public void changeStatus(final FlightPlanStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status is required.");
        }
        this.status = newStatus;
    }

    /**
     * Resets the test result to DRAFT when weather is attached after a simulation (AC8 / US082).
     * No-op if the plan is already DRAFT or SUBMITTED_FOR_SIMULATION.
     */
    public void resetTestOnWeatherChange() {
        if (status == FlightPlanStatus.SIM_APPROVED || status == FlightPlanStatus.SIM_REJECTED) {
            this.status = FlightPlanStatus.DRAFT;
        }
    }

    /**
     * Replaces plan content in place (US080). Keeps the same JPA entity row and version.
     */
    public void replaceDraftContent(final FuelLoad fuelLoad,
                                    final String dslContent,
                                    final String jsonContent) {
        if (fuelLoad == null) {
            throw new IllegalArgumentException("Fuel load is required.");
        }
        if (jsonContent == null || jsonContent.isBlank()) {
            throw new IllegalArgumentException("JSON content is required.");
        }
        this.status = FlightPlanStatus.DRAFT;
        this.fuelLoad = fuelLoad;
        this.dslContent = dslContent;
        this.jsonContent = jsonContent;
    }

}
