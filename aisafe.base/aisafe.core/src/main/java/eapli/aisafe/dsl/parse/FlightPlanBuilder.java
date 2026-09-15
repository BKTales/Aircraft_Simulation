package eapli.aisafe.dsl.parse;

import eapli.aisafe.dsl.FlightPlanBaseVisitor;
import eapli.aisafe.dsl.FlightPlanParser;
import eapli.aisafe.dsl.model.AltitudeSlotDescriptor;
import eapli.aisafe.dsl.model.FlightPlanDescriptor;
import eapli.aisafe.dsl.model.LegDescriptor;
import eapli.aisafe.dsl.model.RouteDescriptor;
import eapli.aisafe.dsl.model.SegmentDescriptor;

import java.util.ArrayList;
import java.util.List;

public class FlightPlanBuilder extends FlightPlanBaseVisitor<Object> {

    private final FlightPlanSourceIndex sourceIndex = new FlightPlanSourceIndex();

    public FlightPlanSourceIndex sourceIndex() {
        return sourceIndex;
    }

    @Override
    public FlightPlanDescriptor visitFlightPlan(FlightPlanParser.FlightPlanContext ctx) {
        return (FlightPlanDescriptor) visit(ctx.flight());
    }

    @Override
    public FlightPlanDescriptor visitFlight(FlightPlanParser.FlightContext ctx) {
        String id = ctx.ID().getText();
        String type = (String) visit(ctx.flightType());
        sourceIndex.setFlight(
                FlightPlanSourceIndex.span(ctx.ID().getSymbol()),
                FlightPlanSourceIndex.span(ctx.flightType()),
                FlightPlanSourceIndex.span(ctx.load()));

        var loadCtx = ctx.load();
        int passengersCount = Integer.parseInt(loadCtx.passengers().INTEGER().getText());
        double paxWeightKg = Double.parseDouble(loadCtx.paxWeight().DECIMAL().getText());
        double cargoWeightKg = Double.parseDouble(loadCtx.cargoWeight().DECIMAL().getText());

        List<LegDescriptor> legs = new ArrayList<>();
        for (var legCtx : ctx.legs().leg()) {
            legs.add((LegDescriptor) visit(legCtx));
        }
        return new FlightPlanDescriptor(id, type, passengersCount, paxWeightKg, cargoWeightKg, legs);
    }

    @Override
    public String visitFlightType(FlightPlanParser.FlightTypeContext ctx) {
        return ctx.getChild(1).getText().toUpperCase();
    }

    @Override
    public LegDescriptor visitLeg(FlightPlanParser.LegContext ctx) {
        FlightPlanSourceIndex.LegSource legSource = new FlightPlanSourceIndex.LegSource();
        legSource.departure = FlightPlanSourceIndex.span(ctx.departure());
        legSource.arrival = FlightPlanSourceIndex.span(ctx.arrival());
        legSource.fuel = FlightPlanSourceIndex.span(ctx.fuel());

        String depAirport = ctx.departure().airportCode().AIRPORT_CODE().getText();
        String depTime = ctx.departure().DATETIME().getText();
        String arrAirport = ctx.arrival().airportCode().AIRPORT_CODE().getText();
        String arrTime = ctx.arrival().DATETIME().getText();

        RouteDescriptor route = (RouteDescriptor) visit(ctx.route());
        for (var segCtx : ctx.route().segment()) {
            FlightPlanSourceIndex.SegmentSource segmentSource = new FlightPlanSourceIndex.SegmentSource();
            segmentSource.segment = FlightPlanSourceIndex.span(segCtx);
            segmentSource.wind = FlightPlanSourceIndex.span(segCtx.INTEGER().getSymbol());
            legSource.segments.add(segmentSource);
        }

        double fuelVal = Double.parseDouble(ctx.fuel().DECIMAL().getText());
        String fuelUni = ctx.fuel().UNIT_MASS().getText();

        sourceIndex.addLeg(legSource);

        return new LegDescriptor(depAirport, depTime, arrAirport, arrTime,
                route, fuelVal, fuelUni);
    }

    @Override
    public RouteDescriptor visitRoute(FlightPlanParser.RouteContext ctx) {
        List<SegmentDescriptor> segments = new ArrayList<>();
        for (var segCtx : ctx.segment()) {
            segments.add((SegmentDescriptor) visit(segCtx));
        }
        return new RouteDescriptor(segments);
    }

    @Override
    public SegmentDescriptor visitSegment(FlightPlanParser.SegmentContext ctx) {
        double[] start = coordToLatLon(ctx.coord(0));
        double[] end = coordToLatLon(ctx.coord(1));

        List<AltitudeSlotDescriptor> slots = new ArrayList<>();
        for (var slotCtx : ctx.altSlot()) {
            slots.add((AltitudeSlotDescriptor) visit(slotCtx));
        }

        int windDir = Integer.parseInt(ctx.INTEGER().getText());
        double windSpeed = Double.parseDouble(ctx.DECIMAL().getText());

        return new SegmentDescriptor(
                start[0], start[1],
                end[0], end[1],
                slots,
                windDir, windSpeed
        );
    }

    @Override
    public AltitudeSlotDescriptor visitAltSlot(FlightPlanParser.AltSlotContext ctx) {
        int alt = Integer.parseInt(ctx.INTEGER(0).getText());
        int width = Integer.parseInt(ctx.INTEGER(1).getText());
        return new AltitudeSlotDescriptor(alt, width);
    }

    private static double[] coordToLatLon(FlightPlanParser.CoordContext ctx) {
        String text = ctx.getText();
        int comma = text.indexOf(',');
        double lat = Double.parseDouble(text.substring(0, comma));
        double lon = Double.parseDouble(text.substring(comma + 1));
        return new double[]{lat, lon};
    }
}
