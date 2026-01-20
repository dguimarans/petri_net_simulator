package com.petrinet.engine;

import java.util.ArrayList;

public class SimulationControl {

    // Termination criteria configuration
    private boolean terminateByTime = false;
    private boolean terminateByMarking = false;
    private double terminationTime = 0;
    private ArrayList<int[]> terminationMarking;

    /**
     * Verbose output flag (copied from SimulationConfig).
     * Used by SimulationEngine to control detailed logging during simulation.
     */
    private boolean verbose = false;


    public SimulationControl() {
        this.terminationMarking = new ArrayList<>();
    }

    // Termination by time
    public void setTerminationByTime(boolean terminateByTime) {
        this.terminateByTime = terminateByTime;
    }

    public boolean shouldTerminateByTime() {
        return terminateByTime;
    }

    public void setTerminationTime(double terminationTime) {
        this.terminationTime = terminationTime;
    }

    public double getTerminationTime() {
        return terminationTime;
    }

    // Termination by marking
    public void setTerminateByMarking(boolean terminateByMarking) {
        this.terminateByMarking = terminateByMarking;
    }

    public boolean shouldTerminateByMarking() {
        return terminateByMarking;
    }

    public void addTerminationMarking(int placeId, int tokens) {
        terminationMarking.add(new int[]{placeId, tokens});
    }

    public ArrayList<int[]> getTerminationMarking() {
        return terminationMarking;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public boolean isVerbose() {
        return verbose;
    }

}