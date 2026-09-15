package eapli.aisafe.usermanagement.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AISafeUserId implements Serializable, Comparable<AISafeUserId> {

    private static final long serialVersionUID = 1L;

    private String id;

    // JPA needs empty constructor
    protected AISafeUserId() {
    }

    private AISafeUserId(final String value) {
        this.id = value;
    }

    public static AISafeUserId newId() {
        return new AISafeUserId(UUID.randomUUID().toString());
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AISafeUserId)) return false;
        final AISafeUserId that = (AISafeUserId) o;
        return Objects.equals(id, that.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(AISafeUserId aiSafeUserId) {
        return this.id.compareTo(aiSafeUserId.id());
    }
}
