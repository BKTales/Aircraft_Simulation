package eapli.aisafe.aircontrolarea.application.exceptions;

import eapli.aisafe.aircontrolarea.domain.GeographicBoundary.GeographicCoords;

import java.util.ArrayList;

public class OverlapBoundaryException extends IllegalArgumentException{

    public OverlapBoundaryException(ArrayList<GeographicCoords> shape1, ArrayList<GeographicCoords> shape2){
        super("Overlap area detected at " + shape1 + " with " + shape2);
    }

}
