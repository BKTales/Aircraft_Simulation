package eapli.aisafe.usermanagement.domain;

import eapli.aisafe.companycollaboratormanagment.application.InvalidPhoneFormat;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Phone implements ValueObject, Serializable, Comparable<Phone> {

    private static final long serialVersionUID = 1L;

    @Column(name = "phoneNumber")
    private String number;

    protected Phone() {

    }

    public Phone(final String phoneNumber) {
        if (!validatePhoneNumber(phoneNumber)) {
            throw new InvalidPhoneFormat("Invalid phone number format.");
        }
        this.number = phoneNumber;
    }

    public static Phone valueOf(final String phoneNumber) {
        return new Phone(phoneNumber);
    }

    private boolean validatePhoneNumber(final String phoneNumber) {
        Preconditions.nonNull(phoneNumber, "Phone number cannot be null");
        Preconditions.nonEmpty(phoneNumber, "Phone number cannot be empty");
        final String regex = "^[+]351\\d{9}$";

        return phoneNumber.matches(regex);
    }

    @Override
    public String toString() {
        return this.number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return Objects.equals(number, phone.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public int compareTo(Phone o) {
        return this.number.compareTo(o.number);
    }
}