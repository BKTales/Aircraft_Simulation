package eapli.aisafe.aircontrolarea.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class AirControlAreaName {
    private String name;

    protected AirControlAreaName() {}

    public AirControlAreaName(String name){
        this.name = name;
    }

    public static AirControlAreaName valueOf(final String name) {
        return new AirControlAreaName(name);
    }

    public String getName() { return name; }
}
