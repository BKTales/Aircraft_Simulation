package eapli.aisafe.companycollaboratormanagment.domain;

import eapli.aisafe.companycollaboratormanagment.application.InvalidSecurityClearanceDate;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;
@Embeddable
public class SecurityClearance implements ValueObject {

    @Column(name = "CLEARANCE_EXPIRY_DATE")
    private LocalDate expiryDate;

    protected SecurityClearance() {}

    private SecurityClearance(final LocalDate expiryDate) {
        Preconditions.nonNull(expiryDate, "Expiry date cannot be null");
        if (!expiryDate.isAfter(LocalDate.now())) {
            throw new InvalidSecurityClearanceDate("Expiry date must be in the future");
        }
        this.expiryDate = expiryDate;
    }

    public static SecurityClearance valueOf(final LocalDate expiryDate) {
        return new SecurityClearance(expiryDate);
    }

    public boolean isActive() {
        return LocalDate.now().isBefore(this.expiryDate);
    }

    public LocalDate expiryDate() {
        return expiryDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SecurityClearance that = (SecurityClearance) o;
        return Objects.equals(expiryDate, that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiryDate);
    }

    @Override
    public String toString() {
        return "SecurityClearance{expiryDate=" + expiryDate + '}';
    }
}