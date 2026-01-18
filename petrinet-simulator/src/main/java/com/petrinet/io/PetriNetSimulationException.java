package com.petrinet.io;

public class PetriNetSimulationException extends PetriNetException {

    public PetriNetSimulationException(String errorMessage) {
        super(errorMessage);
    }

    public PetriNetSimulationException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
