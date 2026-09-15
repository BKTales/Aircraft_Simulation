package eapli.aisafe.rcomp.tcpclient.presentation.atcc.routes;

import eapli.framework.visitor.Visitor;

@SuppressWarnings("squid:S106")
public final class RemoteAirportPrinter implements Visitor<RemoteAirportEntry> {

    public static void printHeader() {
        System.out.printf("%-6s %-6s area=%s%n", "IATA", "ICAO", "AREA");
    }

    @Override
    public void visit(final RemoteAirportEntry airport) {
        System.out.printf("%-6s %-6s area=%s%n", airport.iata(), airport.icao(), airport.area());
    }
}
