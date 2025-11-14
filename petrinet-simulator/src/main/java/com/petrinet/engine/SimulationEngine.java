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

	private boolean verbose;

	private boolean terminateByTime;
	private boolean terminateByMarking;
	private double terminationTime;
	private ArrayList<int[]> terminationMarking;

	public SimulationEngine(String petrinetFile, String outputFile, boolean verbose) throws PetriNetException {
		this.time = 0.0;
		this.pn = new PetriNet(petrinetFile);
		this.listEvents = new LinkedList<Event>();

		this.terminateByTime = false;
		this.terminateByMarking = false;
		readTerminationCriteria(petrinetFile);

		this.verbose = verbose;

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

	public Output getOutputFile() {
		return this.outputWriter;
	}

	private boolean scheduledTransition(int transition) {
		for (int i = 0; i < listEvents.size(); i++)
			if (listEvents.get(i).getTransition() == transition)
				return true;
		return false;
	}

	public boolean enabledVerbose() {
		return this.verbose;
	}

	private void readTerminationCriteria(String petrinetFile) throws PetriNetException {
		try (BufferedReader br = new BufferedReader(new FileReader(petrinetFile))) {
			
			String line;
			do {
				line = br.readLine();
			} while (!line.equals(SECTION_TERMINATION_TIME) && !line.equals(SECTION_TERMINATION_MARKING));

			do {
				if (line.equals(SECTION_TERMINATION_TIME)) {
					terminateByTime = true;

					do {
						line = br.readLine();
						if (!line.substring(0, 1).equals(COMMENT_PREFIX))
							terminationTime = Double.valueOf(line.trim()).doubleValue();
					} while (line.substring(0, 1).equals(COMMENT_PREFIX));

				} else if (line.equals(SECTION_TERMINATION_MARKING)) {
					terminateByMarking = true;
					terminationMarking = new ArrayList<int[]>();

					line = br.readLine();
					do {
						if (!line.substring(0, 1).equals(COMMENT_PREFIX)) {
							int[] placeFinalMarking = new int[2];
							placeFinalMarking[0] = Integer.valueOf(line.split(FIELD_DELIMITER)[0].trim()).intValue();
							placeFinalMarking[1] = Integer.valueOf(line.split(FIELD_DELIMITER)[1].trim()).intValue();
							terminationMarking.add(placeFinalMarking);
						}
						line = br.readLine();

					} while (line != null && line.length() > 0);
				}
				line = br.readLine();
			} while (line != null);

		} catch (FileNotFoundException fnf) {
			throw new PetriNetFileNotFoundException("Petri Net file not found.");
		} catch (IOException ioe) {
			throw new PetriNetParseException("Simulation Engine: Can't read the specified file.");
		} catch (NullPointerException npe) {
			throw new PetriNetSimulationException(
					"Simulation Engine: Unbounded simulation detected. No termination criteria specified or bad format.");
		}
	}

	private boolean terminateSimulation() {
		return (terminateByTime && time >= terminationTime) || (terminateByMarking && checkFinalMarking());
	}

	private boolean checkFinalMarking() {
		int finalPlaceMarking = 0;
		for (int i = 0; i < terminationMarking.size(); i++) {
			if (pn.getPlaces().get(terminationMarking.get(i)[0]).getTokens() >= terminationMarking.get(i)[1])
				finalPlaceMarking++;
		}

		return finalPlaceMarking == terminationMarking.size();
	}
}
