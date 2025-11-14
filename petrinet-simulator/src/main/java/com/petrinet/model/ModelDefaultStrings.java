package com.petrinet.model;

public final class ModelDefaultStrings {

    /** File format version */
    public static final String VERSION = "1.0";
    
    // Default values for places
    public static final String DEFAULT_PLACE_NAME_PREFIX = "P";
    
    // Default values for transitions
    public static final String DEFAULT_TRANSITION_NAME_PREFIX = "T";
    public static final String INITIAL_STATE_MARKER = "T0";

    private ModelDefaultStrings() {
        throw new AssertionError("Cannot instantiate model format constants class");
    }
}