package petrinet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.ArrayList;

public class PetriNet {

	private Hashtable<Integer, Place> places;
	private Hashtable<Integer, Transition> transitions;
	private Hashtable<Integer, ArrayList<Integer>> outputTransitions;

	public PetriNet(int nPlaces, int nTransitions) {
		this.places = new Hashtable<Integer, Place>(nPlaces);
		for (int i = 0; i < nPlaces;)
			places.put(++i, new Place());

		this.transitions = new Hashtable<Integer, Transition>(nTransitions);
		for (int i = 0; i < nTransitions;)
			transitions.put(++i, new Transition());

		this.outputTransitions = new Hashtable<Integer, ArrayList<Integer>>();
	}

	public PetriNet(int[] initialMarking, int nTransitions) {
		this.places = new Hashtable<Integer, Place>(initialMarking.length);
		this.transitions = new Hashtable<Integer, Transition>(nTransitions);
		this.outputTransitions = new Hashtable<Integer, ArrayList<Integer>>();
		setPlaces(initialMarking);
	}

	public PetriNet(int[] initialMarking, int nTransitions, String fileName) {
		this.places = new Hashtable<Integer, Place>(initialMarking.length);
		this.transitions = new Hashtable<Integer, Transition>(nTransitions);
		this.outputTransitions = new Hashtable<Integer, ArrayList<Integer>>();
		setPlaces(initialMarking);
		setTransitionsFromFile(fileName);
	}

	public PetriNet(String fileName) {
		this.places = new Hashtable<Integer, Place>();
		this.transitions = new Hashtable<Integer, Transition>();
		this.outputTransitions = new Hashtable<Integer, ArrayList<Integer>>();
		readPetriNetFile(fileName);
	}

	private void setPlaces(int[] initialStatus) {
		for (int i = 0; i < initialStatus.length; i++)
			places.put(i + 1, new Place(initialStatus[i]));
	}

	private void readPetriNetFile(String fileName) {
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);

			while (!br.readLine().equals("@Places"));

			int place = 1;
			String line = br.readLine();
			do {
				if (!line.substring(0, 1).equals("#"))
					textToPlace(line, place++);
				line = br.readLine();
			} while (line.trim().length() > 0 && !line.equals("@Transitions"));

			if (!line.equals("@Transitions"))
				while (!br.readLine().equals("@Transitions")) {
				}

			int transition = 1;
			line = br.readLine();
			do {
				if (!line.substring(0, 1).equals("#"))
					textToTransition(line, transition++);
				line = br.readLine();
			} while (line.trim().length() > 0 && !line.equals("@TerminationTime")
					&& !line.equals("@TerminationMarking"));

			br.close();
			fr.close();

		} catch (FileNotFoundException fnf) {
			System.err.println("Petri Net file not found.");
			System.exit(0);
		} catch (IOException ioe) {
			System.err.println("Petri Net: Can't read the specified file.");
			System.exit(0);
		} catch (NullPointerException npe) {
			System.err.println("Petri Net: Unbounded simulation detected.");
			System.exit(0);
		}
	}

	private void setTransitionsFromFile(String fileName) {
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);

			String line = br.readLine();
			int transition = 1;
			do {
				textToTransition(line, transition++);
//				transition++;
				line = br.readLine();
			} while (line != null);

			br.close();
			fr.close();

		} catch (FileNotFoundException fnf) {
			System.err.println("Transitions file not found.");
		} catch (IOException ioe) {
			System.err.println("Wrong file format.");
		}
	}

	public Hashtable<Integer, Place> getPlaces() {
		return this.places;
	}

	public Hashtable<Integer, Transition> getTransitions() {
		return this.transitions;
	}

	public Hashtable<Integer, ArrayList<Integer>> getOutputTransitions() {
		return this.outputTransitions;
	}

	public int[] getCurrentStatus() {
		int[] status = new int[places.size()];

		for (int i = 0; i < places.size(); i++)
			status[i] = places.get(i + 1).getTokens();

		return status;
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
		if (simEng.enabledVerbose())
			System.out.println("T" + transition + " @ " + simEng.getSimulationTime());

		for (int i = 0; i < transitions.get(transition).getPlacesIn().length; i++)
			places.get(transitions.get(transition).getPlacesIn()[i])
					.setTokens(places.get(transitions.get(transition).getPlacesIn()[i]).getTokens()
							- transitions.get(transition).getWeightsIn()[i]);

		for (int i = 0; i < transitions.get(transition).getPlacesOut().length; i++)
			places.get(transitions.get(transition).getPlacesOut()[i])
					.setTokens(places.get(transitions.get(transition).getPlacesOut()[i]).getTokens()
							+ transitions.get(transition).getWeightsOut()[i]);

		simEng.getOutputFile()
				.writeOutput("T" + transition + "," + simEng.getSimulationTime() + "," + stateToString() + "\n");
		transitions.get(transition).countFirings();

		int[] placesOut = transitions.get(transition).getPlacesOut();

		for (int i = 0; i < placesOut.length; i++) {
			if (simEng.enabledVerbose())
				System.out.println(placesOut[i] + " -> " + getOutputTransitions().get(placesOut[i]));
			if (getOutputTransitions().containsKey(placesOut[i]))
				for (int j = 0; j < getOutputTransitions().get(placesOut[i]).size(); j++)
					if (enabledTransition(getOutputTransitions().get(placesOut[i]).get(j))) {
						if (simEng.enabledVerbose())
							System.out
									.println("Enabled transition: T" + getOutputTransitions().get(placesOut[i]).get(j));
						if (transitions.get(getOutputTransitions().get(placesOut[i]).get(j)).isTimed()) {
							double nextFireTime = simEng.getSimulationTime()
									+ transitions.get(getOutputTransitions().get(placesOut[i]).get(j)).call();
							simEng.getListEvents()
									.add(new Event(getOutputTransitions().get(placesOut[i]).get(j), nextFireTime));
							if (simEng.enabledVerbose())
								System.out.println("Adding T" + getOutputTransitions().get(placesOut[i]).get(j)
										+ " with time: " + nextFireTime);
						} else {
							fireTransition(getOutputTransitions().get(placesOut[i]).get(j), simEng);
						}
					}
		}
	}

	private void textToPlace(String line, int place) {
		if(line.split(";").length == 1)
			places.put(place, new Place(place, Integer.valueOf(line).intValue()));
		else if(line.split(";").length == 2)
			places.put(place, new Place(place, Integer.valueOf(line.split(";")[0]).intValue(), line.split(";")[1]));
	}

	private void textToTransition(String line, int transition) {
		String[] splitLine = line.split(";");

		String[] pIn = splitLine[0].split(",");
		String[] pOut = splitLine[1].split(",");
		String[] wIn = splitLine[2].split(",");
		String[] wOut = splitLine[3].split(",");

		int[] placeIn = new int[pIn.length];
		int[] placeOut = new int[pOut.length];
		int[] weightIn = new int[wIn.length];
		int[] weightOut = new int[wOut.length];

		for (int i = 0; i < placeOut.length; i++)
			placeOut[i] = Integer.valueOf(pOut[i]).intValue();
		for (int i = 0; i < weightIn.length; i++)
			weightIn[i] = Integer.valueOf(wIn[i]).intValue();
		for (int i = 0; i < weightOut.length; i++)
			weightOut[i] = Integer.valueOf(wOut[i]).intValue();

		for (int i = 0; i < placeIn.length; i++) {
			placeIn[i] = Integer.valueOf(pIn[i]).intValue();

//			Populate lists of transitions enabled by each place
			if (outputTransitions.containsKey(placeIn[i]))
				outputTransitions.get(placeIn[i]).add(transition);
			else {
				outputTransitions.put(placeIn[i], new ArrayList<Integer>());
				outputTransitions.get(placeIn[i]).add(transition);
			}
		}

		if (splitLine.length == 4)
			transitions.put(transition, new Transition(transition, placeIn, placeOut, weightIn, weightOut));
		else
			transitions.put(transition, new Transition(transition, placeIn, placeOut, weightIn, weightOut, splitLine[4].trim()));
	}

	public String stateToString() {
		String state = "";

		for (int i = 1; i < places.size(); i++)
			state += places.get(i).getTokens() + ",";
		state += places.get(places.size()).getTokens();

		return state;
	}

}
