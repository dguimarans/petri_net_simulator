package com.petrinet.cli;

public final class CommandLineConstants {

    public static final String VERBOSE_FLAG = "-verbose";
    public static final String NUMERIC_PATTERN = "\\d+";

    public static final String USAGE_MESSAGE =
        "Syntax: java petrinet.Simulate inputFileName outputFileName [simulationRuns] [" + VERBOSE_FLAG + "]";

    private CommandLineConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }
}