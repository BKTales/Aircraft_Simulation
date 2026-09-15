package eapli.aisafe.dsl.api;

import eapli.aisafe.aircraftmodelmanagement.domain.AircraftModel;
import eapli.aisafe.aircraftmodelmanagement.domain.AircraftType;
import eapli.aisafe.enginemodelmanagement.domain.EngineModel;

import java.util.Locale;

public final class SimulatorAircraftJsonMapper {

    private static final double G = 9.80665;
    private static final double LITRES_TO_KG = 0.804;
    private static final double LAPSE_RATE_FACTOR = 0.7;
    private static final double OSWALD_E = 0.87;

    private static final double[] CD_MACH = {0.0, 0.75, 0.80, 0.85, 0.90, 0.95, 1.0};
    private static final double[] CD_SCALE = {1.0, 1.0, 1.05, 1.21, 1.4, 1.9, 3.0};

    private SimulatorAircraftJsonMapper() {}

    public static String toJsonObject(final AircraftModel model, final EngineModel engine) {
        if (model == null || engine == null) {
            throw new IllegalArgumentException("Aircraft model and engine are required.");
        }

        final double cruiseAlt = model.performanceSpec().serviceCeiling();
        final double cruiseSpeedMs = model.performanceSpec().cruiseSpeed();
        final double cruiseMach = msToMach(cruiseSpeedMs, cruiseAlt);
        final double maxSpeed = Math.min(1.0, cruiseMach + 0.1);
        final double mmo = Math.min(0.89, cruiseMach + 0.05);
        final double vmo = cruiseSpeedMs * 1.15;
        final double cd0 = model.aerodynamics().cd0();
        final double maxPayload = model.weights().mzfw() - model.weights().emptyWeight();
        final double fuelKg = model.performanceSpec().fuelCapacity() * LITRES_TO_KG;
        // Domain stores TSFC in N/(N·h); C physics expects kg/(N·s): divide by G and by 3600
        final double tsfcKgNs = engine.tsfc().value() / G / 3600.0;

        final StringBuilder sb = new StringBuilder(1024);
        sb.append("{\n");
        appendField(sb, "ModelId", model.identity().toString(), true);
        appendField(sb, "Description", model.name().toString(), true);
        appendField(sb, "Maker", model.manufacturer().identity().toString(), true);
        appendField(sb, "Type", aircraftType(model.aircraftType()), true);
        appendNumber(sb, "NumberMotors", model.numberOfEngines().number());
        appendField(sb, "Motor", engine.name().toString(), true);
        appendField(sb, "MotorType", engine.motorization().name().toLowerCase(Locale.ROOT), true);
        appendNumber(sb, "CruiseAltitude", cruiseAlt);
        appendNumber(sb, "CruiseSpeed", cruiseMach);
        appendNumber(sb, "TSFC", tsfcKgNs);
        appendNumber(sb, "LapseRateFactor", LAPSE_RATE_FACTOR);
        appendNumber(sb, "Thrust0", engine.thrustProfile().thrustAtStatic() * 1000.0);
        appendNumber(sb, "ThrustMaxSpeed", engine.thrustProfile().thrustAtCruise() * 1000.0);
        appendNumber(sb, "MaxSpeed", maxSpeed);
        appendNumber(sb, "EWeight", model.weights().emptyWeight());
        appendNumber(sb, "MTOW", model.weights().mtow());
        appendNumber(sb, "MaxPayload", maxPayload);
        appendNumber(sb, "FuelCapacity", fuelKg);
        appendNumber(sb, "VMO", vmo);
        appendNumber(sb, "MMO", mmo);
        appendNumber(sb, "WingArea", model.wingGeometry().wingArea());
        appendNumber(sb, "WingSpan", model.wingGeometry().wingSpan());
        appendNumber(sb, "AspectRatio", model.wingGeometry().aspectRatio());
        appendNumber(sb, "E", OSWALD_E);
        sb.append("    \"CdragFunction\": [\n");
        for (int i = 0; i < CD_MACH.length; i++) {
            if (i > 0) {
                sb.append(",\n");
            }
            sb.append("      { \"Speed\": ").append(trim(CD_MACH[i]))
                    .append(", \"Cdrag0\": ").append(trim(cd0 * CD_SCALE[i])).append(" }");
        }
        sb.append("\n    ]\n  }");
        return sb.toString();
    }

    public static double msToMach(final double speedMs, final double altitudeM) {
        final double tempK = 288.15 - 0.0065 * Math.max(0.0, Math.min(altitudeM, 11000.0));
        final double soundSpeed = 20.05 * Math.sqrt(Math.max(200.0, tempK));
        return speedMs / soundSpeed;
    }

    private static String aircraftType(final AircraftType type) {
        return switch (type) {
            case PASSENGER -> "passenger";
            case CARGO -> "cargo";
            case MIXED -> "mixed";
        };
    }

    private static void appendField(final StringBuilder sb, final String key, final String value, final boolean comma) {
        sb.append("    \"").append(key).append("\": \"").append(escape(value)).append("\"");
        if (comma) {
            sb.append(",\n");
        } else {
            sb.append('\n');
        }
    }

    private static void appendNumber(final StringBuilder sb, final String key, final double value) {
        sb.append("    \"").append(key).append("\": ").append(trim(value)).append(",\n");
    }

    private static String escape(final String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static String trim(final double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "0";
        }
        final long asLong = (long) v;
        return (v == asLong) ? Long.toString(asLong) : Double.toString(v);
    }
}
