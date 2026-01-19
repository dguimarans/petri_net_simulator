package com.petrinet.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.petrinet.engine.*;

import static com.petrinet.io.OutputFormat.CSV_DELIMITER;
import static com.petrinet.model.ModelDefaultStrings.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PetriNet {

	private HashMap<Integer, Place> places;
	private HashMap<Integer, Transition> transitions;
	private HashMap<Integer, ArrayList<Integer>> outputTransitions;

	public PetriNet(int nPlaces, int nTransitions) {
		this.places = new HashMap<Integer, Place>(nPlaces);
		this.transitions = new HashMap<Integer, Transition>(nTransitions);

		this.outputTransitions = new HashMap<Integer, ArrayList<Integer>>();
	}

	public void addPlace(Place place) {
		this.places.put(place.getId(), place);
	}

	public void addTransition(Transition transition) {
		this.transitions.put(transition.getId(), transition);

		// Populate lists of transitions enabled by each place
		for (int i = 0; i < transition.getPlacesIn().length; i++) {
			if (outputTransitions.containsKey(transition.getPlacesIn()[i]))
				outputTransitions.get(transition.getPlacesIn()[i]).add(transition.getId());
			else {
				outputTransitions.put(transition.getPlacesIn()[i], new ArrayList<Integer>());
				outputTransitions.get(transition.getPlacesIn()[i]).add(transition.getId());
			}
		}
	}

	public HashMap<Integer, Place> getPlaces() {
		return this.places;
	}

	public HashMap<Integer, Transition> getTransitions() {
		return this.transitions;
	}

	public HashMap<Integer, ArrayList<Integer>> getOutputTransitions() {
		return this.outputTransitions;
	}

	public int[] getCurrentStatus() {
		int[] status = new int[places.size()];

		for (int i = 0; i < places.size(); i++)
			status[i] = places.get(i + 1).getTokens();

		return status;
	}

	public void resetMarking() {
		for (int i = 1; i <= places.size(); i++)
			places.get(i).resetTokens();
	}

	public boolean enabledTransition(int transition) {
		int[] placesIn = transitions.get(transition).getPlacesIn();
		int[] weightsIn = transitions.get(transition).getWeightsIn();

		// Inhibitors will have an arc weight of 0
		for (int i = 0; i < placesIn.length; i++)
			if ((weightsIn[i] != 0 && places.get(placesIn[i]).getTokens() < weightsIn[i]) ||
					(weightsIn[i] == 0 && places.get(placesIn[i]).getTokens() > 0))
				return false;

		return true;
	}

	public void fireTransition(int transition, SimulationEngine simEng) {
		if (simEng.isVerbose())
			log.info("{}{} @ {}", DEFAULT_TRANSITION_NAME_PREFIX, transition, simEng.getSimulationTime());

		Transition t = transitions.get(transition);
		int[] placesIn = t.getPlacesIn();
		int[] weightsIn = t.getWeightsIn();
		int[] placesOut = t.getPlacesOut();
		int[] weightsOut = t.getWeightsOut();

		// Update input places (consume tokens)
		for (int i = 0; i < placesIn.length; i++) {
			Place place = places.get(placesIn[i]);
			int newTokens = place.getTokens() - weightsIn[i];
			place.setTokens(newTokens);

			if (simEng.shouldWriteCompact()) {
				simEng.writeCompactChange(transition, placesIn[i], place.getName(),newTokens);
			}
		}

		// Update output places (produce tokens)
		for (int i = 0; i < placesOut.length; i++) {
			Place place = places.get(placesOut[i]);
			int newTokens = place.getTokens() + weightsOut[i];
			place.setTokens(newTokens);

			if (simEng.shouldWriteCompact()) {
				simEng.writeCompactChange(transition, placesOut[i], place.getName(), newTokens);
			}
		}

		// Write full-state output if enabled
		if (simEng.shouldWriteFullState()) {
			simEng.writeFullState(transition, stateToString());
		}

		transitions.get(transition).countFirings();

		// Schedule newly enabled transitions
		for (int i = 0; i < placesOut.length; i++) {
			if (simEng.isVerbose())
				log.info("{} -> {}", placesOut[i], getOutputTransitions().get(placesOut[i]));
			if (getOutputTransitions().containsKey(placesOut[i]))
				for (int j = 0; j < getOutputTransitions().get(placesOut[i]).size(); j++)
					if (enabledTransition(getOutputTransitions().get(placesOut[i]).get(j))) {
						if (simEng.isVerbose())
							log.info("Enabled transition: {}{}", DEFAULT_TRANSITION_NAME_PREFIX,
									getOutputTransitions().get(placesOut[i]).get(j));
						if (transitions.get(getOutputTransitions().get(placesOut[i]).get(j)).isTimed()) {
							double nextFireTime = simEng.getSimulationTime()
									+ transitions.get(getOutputTransitions().get(placesOut[i]).get(j)).call();
							simEng.getListEvents()
									.add(new Event(getOutputTransitions().get(placesOut[i]).get(j), nextFireTime));
							if (simEng.isVerbose())
								log.info("Adding {}{} with time: {}", DEFAULT_TRANSITION_NAME_PREFIX,
										getOutputTransitions().get(placesOut[i]).get(j), nextFireTime);
						} else {
							fireTransition(getOutputTransitions().get(placesOut[i]).get(j), simEng);
						}
					}
		}
	}

	public String stateToString() {
		StringBuilder state = new StringBuilder(places.size() * 4);

		for (int i = 1; i < places.size(); i++) {
			state.append(places.get(i).getTokens()).append(CSV_DELIMITER);
		}
		state.append(places.get(places.size()).getTokens());

		return state.toString();
	}

}
