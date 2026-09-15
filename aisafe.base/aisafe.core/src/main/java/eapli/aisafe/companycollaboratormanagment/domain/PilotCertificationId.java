package eapli.aisafe.companycollaboratormanagment.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PilotCertificationId implements Serializable, Comparable<PilotCertificationId> {

    private static final long serialVersionUID = 1L;

    private String id;

    protected PilotCertificationId() {
        // for ORM
    }

    private PilotCertificationId(final String id) {
        this.id = id;
    }

    public static PilotCertificationId newId() {
        return new PilotCertificationId(UUID.randomUUID().toString());
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PilotCertificationId)) {
            return false;
        }
        final PilotCertificationId that = (PilotCertificationId) o;
        return Objects.equals(id, that.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(final PilotCertificationId other) {
        return this.id.compareTo(other.id());
    }
}
