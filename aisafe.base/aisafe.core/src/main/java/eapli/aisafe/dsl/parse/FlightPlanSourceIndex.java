package eapli.aisafe.dsl.parse;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Token positions collected while building a {@link eapli.aisafe.dsl.model.FlightPlanDescriptor}.
 */
public final class FlightPlanSourceIndex {

    private SourceSpan flightId;
    private SourceSpan flightType;
    private SourceSpan load;
    private final List<LegSource> legs = new ArrayList<>();

    public SourceSpan flightId() {
        return flightId;
    }

    public SourceSpan flightType() {
        return flightType;
    }

    public SourceSpan load() {
        return load;
    }

    public List<LegSource> legs() {
        return Collections.unmodifiableList(legs);
    }

    void setFlight(final SourceSpan id, final SourceSpan type, final SourceSpan load) {
        this.flightId = id;
        this.flightType = type;
        this.load = load;
    }

    void addLeg(final LegSource leg) {
        legs.add(leg);
    }

    public static SourceSpan span(final ParserRuleContext ctx) {
        if (ctx == null || ctx.getStart() == null) {
            return null;
        }
        return span(ctx.getStart());
    }

    public static SourceSpan span(final Token token) {
        if (token == null) {
            return null;
        }
        return new SourceSpan(token.getLine(), token.getCharPositionInLine());
    }

    public static final class LegSource {
        public SourceSpan departure;
        public SourceSpan arrival;
        public SourceSpan fuel;
        public final List<SegmentSource> segments = new ArrayList<>();
    }

    public static final class SegmentSource {
        public SourceSpan segment;
        public SourceSpan wind;
    }
}
