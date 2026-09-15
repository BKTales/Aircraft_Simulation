package eapli.aisafe.manufacturermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CountryCode implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "COUNTRY_CODE")
    private String code;

    protected CountryCode() {
        // for ORM
    }

    public CountryCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Country code cannot be empty.");
        }
        this.code = code;
    }

    public String code() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CountryCode that = (CountryCode) o;
        return this.code.equals(that.code());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
