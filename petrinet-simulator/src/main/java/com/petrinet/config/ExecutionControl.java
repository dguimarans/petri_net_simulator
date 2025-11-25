package com.petrinet.config;

/**
 * Controls how multiple simulation runs are executed and observed.
 */


public class ExecutionControl {

    /** Number of simulation runs to execute */
    public static int simulationRuns = 1;

    /** Verbose output for execution monitoring and debugging */
    public static boolean verbose = false;

    public ExecutionControl() {}

    public void setSimulationRuns(int simulationRuns) {
        this.simulationRuns = simulationRuns;
    }

    public int getSimulationRuns() {
        return this.simulationRuns;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return this.verbose;
    }
}