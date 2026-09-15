package eapli.aisafe.airtransportcompanymanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class CompanyName implements ValueObject {

    @Column(name = "COMPANY_NAME")
    private String name;

    protected CompanyName() {
        // for ORM
    }

    private CompanyName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty.");
        }
        this.name = name.trim();
    }

    public static CompanyName valueOf(final String name) {
        return new CompanyName(name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CompanyName that = (CompanyName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
