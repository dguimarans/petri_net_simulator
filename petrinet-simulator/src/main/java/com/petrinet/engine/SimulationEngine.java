package com.petrinet.engine;

import com.petrinet.model.PetriNet;

import lombok.extern.slf4j.Slf4j;

import com.petrinet.io.*;
import static com.petrinet.io.PetriNetFileFormat.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

@Slf4j
public class SimulationEngine {

	private double time;
	private LinkedList<Event> listEvents;

	private PetriNet pn;

	private final String outputFile;
	private Output outputWriter;

	private SimulationControl simulationControl;

	public SimulationEngine(PetriNet petriNet, String outputFile, SimulationControl simulationControl) throws PetriNetException {
		this.time = 0.0;
		this.pn = petriNet;
		this.listEvents = new LinkedList<>();
		this.simulationControl = simulationControl;

		this.outputFile = outputFile;
	}

	public void run() throws PetriNetException {

		try(Output outputWriter = openOutputFiles(outputFile)) {
			this.outputWriter = outputWriter;

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
		}
	}

	private Output openOutputFiles(String outputFile) throws PetriNetException {
		Output output = new Output(outputFile);
		output.writeHeaders(pn);
		output.writeInitialState(getSimulationTime(), pn);

		return output;
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

	public Output getOutputFile() {
		return this.outputWriter;
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
