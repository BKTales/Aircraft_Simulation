package eapli.aisafe.aircontrolarea.domain;

import eapli.aisafe.aircontrolarea.application.exceptions.NegativeFuelException;
import jakarta.persistence.Embeddable;

@Embeddable
public class MinFuelRequirement {
    private float minFuelRequirement;

    protected MinFuelRequirement() {}

    public MinFuelRequirement(float minFuelRequirement){
        if(minFuelRequirement < 0)
            throw new NegativeFuelException(minFuelRequirement);
        this.minFuelRequirement = minFuelRequirement;
    }

    public static MinFuelRequirement valueOf(final float minFuelRequirement) {
        return new MinFuelRequirement(minFuelRequirement);
    }
}
