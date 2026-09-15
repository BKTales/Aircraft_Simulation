package eapli.aisafe.aircontrolarea.application.exceptions;

public class NegativeFuelException extends IllegalArgumentException{
    public NegativeFuelException(final float minFuelRequirement) {
        super("Invalid fuel limit (should be > 0): " + minFuelRequirement);
    }
}

