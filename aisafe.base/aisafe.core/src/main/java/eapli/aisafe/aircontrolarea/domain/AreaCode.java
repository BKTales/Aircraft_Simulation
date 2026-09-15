package eapli.aisafe.aircontrolarea.domain;

import eapli.framework.domain.model.ValueObject;
import java.util.Objects;

public class AreaCode implements ValueObject, Comparable<AreaCode> {

    private static final String PREFIX = "AREA-";
    private static int counter = 0;

    private final String code;

    public AreaCode() {
        this.code = PREFIX + counter++;
    }

    public AreaCode(final String code) {
        this.code = code;
    }

    public static AreaCode valueOf(final String code) {
        return new AreaCode(code);
    }

    public String getCode() {
        return code;
    }

    @Override
    public int compareTo(final AreaCode other) {
        return this.code.compareTo(other.code);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AreaCode)) return false;
        final AreaCode that = (AreaCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
