package eapli.aisafe.dsl.parse;

import eapli.aisafe.dsl.FlightPlanBaseListener;
import eapli.aisafe.dsl.FlightPlanParser;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;

public final class FlightPlanTreeShapeListener extends FlightPlanBaseListener {

    private int legs;
    private int segmentsInCurrentLeg;
    private final List<Integer> segmentCountPerLeg = new ArrayList<>();

    @Override
    public void enterLeg(final FlightPlanParser.LegContext ctx) {
        legs++;
        segmentsInCurrentLeg = 0;
    }

    @Override
    public void enterSegment(final FlightPlanParser.SegmentContext ctx) {
        segmentsInCurrentLeg++;
    }

    @Override
    public void exitLeg(final FlightPlanParser.LegContext ctx) {
        segmentCountPerLeg.add(segmentsInCurrentLeg);
    }

    public static FlightPlanTreeShapeListener inspect(final ParseTree tree) {
        FlightPlanTreeShapeListener listener = new FlightPlanTreeShapeListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        return listener;
    }

    public boolean matchesDescriptor(final FlightPlanDescriptor descriptor) {
        if (descriptor == null) return false;
        if (descriptor.getLegs().size() != legs) return false;
        if (descriptor.getLegs().size() != segmentCountPerLeg.size()) return false;

        for (int i = 0; i < legs; i++) {
            LegDescriptor leg = descriptor.getLegs().get(i);
            int expected = leg.getRoute().segments().size();
            if (expected != segmentCountPerLeg.get(i)) return false;
        }
        return true;
    }
}

