package com.petrinet.engine;

public final class SimulationControl {

    public static int SIMULATION_RUNS = 1;

    private SimulationControl() {
        throw new AssertionError("Cannot instantiate simulation control configuration class");
    }

    public static void setSimulationRuns (int simulationRuns) {
        SIMULATION_RUNS = simulationRuns;
    }
}