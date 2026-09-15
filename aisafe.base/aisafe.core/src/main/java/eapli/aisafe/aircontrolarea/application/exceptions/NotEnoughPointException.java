package eapli.aisafe.aircontrolarea.application.exceptions;

public class NotEnoughPointException extends IllegalArgumentException{
    public NotEnoughPointException(int qtyOfPoints){
        super("Invalid of points (should be < 3): " + qtyOfPoints);
    }

}

