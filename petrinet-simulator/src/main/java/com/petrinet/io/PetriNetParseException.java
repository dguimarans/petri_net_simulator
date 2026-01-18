package com.petrinet.io;

public class PetriNetParseException extends PetriNetException {

    public PetriNetParseException(String errorMessage) {
        super(errorMessage);
    }

    public PetriNetParseException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}