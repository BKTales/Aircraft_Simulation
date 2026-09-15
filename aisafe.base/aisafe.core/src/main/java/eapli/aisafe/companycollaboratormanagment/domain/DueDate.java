package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class DueDate implements ValueObject {

    private LocalDate startDate;
    private LocalDate endDate;

    protected DueDate() {
        // for ORM
    }

    private DueDate(final LocalDate startDate, final LocalDate endDate) {
        Preconditions.nonNull(startDate, "Start date is required");
        Preconditions.nonNull(endDate, "End date is required");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static DueDate valueOf(final LocalDate startDate, final LocalDate endDate) {
        return new DueDate(startDate, endDate);
    }

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DueDate dueDate = (DueDate) o;
        return Objects.equals(startDate, dueDate.startDate)
                && Objects.equals(endDate, dueDate.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }
}
