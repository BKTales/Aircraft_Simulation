package eapli.aisafe.aircontrolarea.application.exceptions;

public class ZeroSizeAreaException extends IllegalArgumentException{
    public ZeroSizeAreaException(){
        super("Area equals to zero.");
    }
}
