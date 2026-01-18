package com.petrinet.io;

public class PetriNetException extends Exception {

    public PetriNetException(String errorMessage) {
        super(errorMessage);
    }

    public PetriNetException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}