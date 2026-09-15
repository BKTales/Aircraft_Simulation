package eapli.aisafe.manufacturermanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
public class Manufacturer implements AggregateRoot<ManufacturerId>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private ManufacturerId identity;

    @Version
    private Long version;

    @Embedded
    private ManufacturerName name;

    @Embedded
    private CountryCode code;

    protected Manufacturer() {
        // for ORM
    }

    public Manufacturer(final ManufacturerId id, ManufacturerName name, CountryCode code) {
        this.identity = id;
        this.name = name;
        this.code = code;
    }

    @Override
    public boolean sameAs(Object other) {
        return this.identity.equals(((Manufacturer) other).identity());
    }

    @Override
    public ManufacturerId identity() {
        return this.identity;
    }

    public ManufacturerName name() {
        return name;
    }

    public CountryCode countryCode() {
        return code;
    }
}