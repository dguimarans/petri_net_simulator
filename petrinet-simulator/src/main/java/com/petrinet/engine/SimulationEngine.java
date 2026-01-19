package com.petrinet.engine;

import com.petrinet.config.SimulationConfig;
import com.petrinet.model.PetriNet;

import lombok.extern.slf4j.Slf4j;

import com.petrinet.io.*;

import java.util.Collections;
import java.util.LinkedList;

@Slf4j
public class SimulationEngine {

	private double time;
	private LinkedList<Event> listEvents;

	private PetriNet pn;

	private final String outputFile;
	private Output compactOutputWriter;
	private Output fullStateOutputWriter;

	private SimulationControl simulationControl;
	private SimulationConfig simulationConfig;

	/**
	 * Legacy constructor for backwards compatibility.
	 * Uses full-state output mode by default.
	 */
	public SimulationEngine(PetriNet petriNet, String outputFile, SimulationControl simulationControl) throws PetriNetException {
		this(petriNet, outputFile, simulationControl, null);
	}

	/**
	 * New constructor with configuration support.
	 */
	public SimulationEngine(PetriNet petriNet, String outputFile, SimulationControl simulationControl, SimulationConfig config) throws PetriNetException {
		this.time = 0.0;
		this.pn = petriNet;
		this.listEvents = new LinkedList<>();
		this.simulationControl = simulationControl;
		this.simulationConfig = config;

		this.outputFile = outputFile;
	}

	public void run() throws PetriNetException {

		try {
			openOutputFiles();

			while (!terminateSimulation()) {

				if (!listEvents.isEmpty()) {
					Collections.sort(listEvents);
					Event firingEvent = listEvents.remove(0);

					time = firingEvent.getTime();
					pn.fireTransition(firingEvent.getTransition(), this);
				} else {
					// This block only applies at the beginning of the simulation
					// or a potential deadlock state has been reached
					// (i.e. no live transitions and no events scheduled)
					boolean deadlock = true;
					int i = 1;
					do {
						if (pn.enabledTransition(pn.getTransitions().get(i).getId())) {
							// This first case only applies for Arrival/Source transitions
							if (pn.getTransitions().get(i).isTimed()
									&& !scheduledTransition(pn.getTransitions().get(i).getId()))
								listEvents.add(new Event(pn.getTransitions().get(i).getId(),
										time + pn.getTransitions().get(i).call()));
							else if (!pn.getTransitions().get(i).isTimed())
								pn.fireTransition(pn.getTransitions().get(i).getId(), this);
							else
								i++;
							deadlock = false;
						} else {
							i++;
						}
					} while (i <= pn.getTransitions().size());

					if (deadlock) {
						log.warn("Simulation reached a deadlock state.");
						break;
					}
				}

			}
		} finally {
			closeOutputFiles();
		}
	}

	private void openOutputFiles() throws PetriNetException {
		if (shouldWriteCompact()) {
			compactOutputWriter = new Output(outputFile);
			compactOutputWriter.writeCompactHeaders();
		}

		if (shouldWriteFullState()) {
			String fullStateFile = (simulationConfig != null)
				? simulationConfig.getFullStateOutputFile(outputFile)
				: outputFile;

			// If only full-state mode, use the base output file name
			if (!shouldWriteCompact()) {
				fullStateFile = outputFile;
			}

			fullStateOutputWriter = new Output(fullStateFile);
			fullStateOutputWriter.writeHeaders(pn);
			fullStateOutputWriter.writeInitialState(getSimulationTime(), pn);
		}
	}

	private void closeOutputFiles() throws PetriNetException {
		if (compactOutputWriter != null) {
			compactOutputWriter.close();
		}
		if (fullStateOutputWriter != null) {
			fullStateOutputWriter.close();
		}
	}

	public double getSimulationTime() {
		return this.time;
	}

	public LinkedList<Event> getListEvents() {
		return this.listEvents;
	}

	public boolean isVerbose() {
		return simulationControl.isVerbose();
	}

	public boolean shouldWriteCompact() {
		return simulationConfig != null && simulationConfig.shouldWriteCompact();
	}

	public boolean shouldWriteFullState() {
		// Default to full-state for legacy/null config
		return simulationConfig == null || simulationConfig.shouldWriteFullState();
	}

	/**
	 * Writes a single place change to the compact output file.
	 * Format: transition;time;placeId;placeName;newTokens
	 */
	public void writeCompactChange(int transition, int placeId, String placeName, int newTokens) {
		if (compactOutputWriter != null) {
			compactOutputWriter.writeCompactLine(transition, time, placeId, placeName, newTokens);
		}
	}

	/**
	 * Writes the full system state to the full-state output file.
	 */
	public void writeFullState(int transition, String stateString) {
		if (fullStateOutputWriter != null) {
			fullStateOutputWriter.writeFullStateLine(transition, time, stateString);
		}
	}

	private boolean scheduledTransition(int transition) {
		for (int i = 0; i < listEvents.size(); i++)
			if (listEvents.get(i).getTransition() == transition)
				return true;
		return false;
	}

	private boolean terminateSimulation() {
		return (simulationControl.shouldTerminateByTime() && time >= simulationControl.getTerminationTime())
			|| (simulationControl.shouldTerminateByMarking() && checkFinalMarking());
	}

	private boolean checkFinalMarking() {
		for (int[] placeGoalState : simulationControl.getTerminationMarking()) {
			if (pn.getPlaces().get(placeGoalState[0]).getTokens() < placeGoalState[1]){
				return false;
			}
		}

		return true;
	}
}
