package petrinet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class SimulationEngine {

	private double time;
	private LinkedList<Event> listEvents;

	private PetriNet pn;

	private Output outputWriter;

	private boolean verbose;

	private boolean terminateByTime;
	private boolean terminateByMarking;
	private double terminationTime;
	private ArrayList<int[]> terminationMarking;

	public SimulationEngine(String petrinetFile, String outputFile, boolean verbose) {
		this.time = 0.0;
		this.pn = new PetriNet(petrinetFile);
		this.listEvents = new LinkedList<Event>();

		this.terminateByTime = false;
		this.terminateByMarking = false;
		readTerminationCriteria(petrinetFile);

		this.verbose = verbose;

		openOutputFiles(outputFile);
	}

	public void run() {

		while (!terminateSimulation()) {

			if (!listEvents.isEmpty()) {
				Collections.sort(listEvents);
				Event firingEvent = listEvents.remove(0);
//				listEvents.remove(0);

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
						if (pn.getTransitions().get(i).isTimed() && !scheduledTransition(pn.getTransitions().get(i).getId())) 							
							listEvents.add(new Event(pn.getTransitions().get(i).getId(),
									time + pn.getTransitions().get(i).call()));
						else if(!pn.getTransitions().get(i).isTimed())
							pn.fireTransition(pn.getTransitions().get(i).getId(), this);
						else
							i++;
						deadlock = false;
					} else {
						i++;
					}
				} while (i <= pn.getTransitions().size());

				if (deadlock) {
					System.out.println("Simulation reached a deadlock state.");
					break;
				}
			}

		}

		closeOutputFiles();
	}

	private void openOutputFiles(String outputFile) {
		outputWriter = new Output(outputFile);
		outputWriter.writeHeaders(pn);
		outputWriter.writeInitialState(getSimulationTime(), pn);
	}

	private void closeOutputFiles() {
		outputWriter.closeOutput();
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
		for(int i = 0; i < listEvents.size(); i++)
			if(listEvents.get(i).getTransition() == transition)
				return true;
		return false;
	}

	public boolean enabledVerbose() {
		return this.verbose;
	}

	private void readTerminationCriteria(String petrinetFile) {
		try {
			FileReader fr = new FileReader(petrinetFile);
			BufferedReader br = new BufferedReader(fr);

			String line;
			do {
				line = br.readLine();
			} while (!line.equals("@TerminationTime") && !line.equals("@TerminationMarking"));

			do {
				if (line.equals("@TerminationTime")) {
					terminateByTime = true;

					do {
						line = br.readLine();
						if (!line.substring(0, 1).equals("#"))
							terminationTime = Double.valueOf(line.trim()).doubleValue();
					} while (line.substring(0, 1).equals("#"));

				} else if (line.equals("@TerminationMarking")) {
					terminateByMarking = true;
					terminationMarking = new ArrayList<int[]>();

					line = br.readLine();
					do {
						if (!line.substring(0, 1).equals("#")) {
							int[] placeFinalMarking = new int[2];
							placeFinalMarking[0] = Integer.valueOf(line.split(";")[0].trim()).intValue();
							placeFinalMarking[1] = Integer.valueOf(line.split(";")[1].trim()).intValue();
							terminationMarking.add(placeFinalMarking);
						}
						line = br.readLine();

					} while (line != null && line.length() > 0);
				}
				line = br.readLine();
			} while (line != null);

			br.close();
			fr.close();

		} catch (FileNotFoundException fnf) {
			System.err.println("Petri Net file not found.");
			System.exit(0);
		} catch (IOException ioe) {
			System.err.println("Simulation Engine: Can't read the specified file.");
			System.exit(0);
		} catch (NullPointerException npe) {
			System.err.println(
					"Simulation Engine: Unbounded simulation detected. No termination criteria specified or bad format.");
			System.exit(0);
		}
	}

	private boolean terminateSimulation() {
		if (terminateByTime && time >= terminationTime)
			return true;
		else if (terminateByMarking && checkFinalMarking())
			return true;
		else
			return false;
	}

	private boolean checkFinalMarking() {
		int finalPlaceMarking = 0;
		for (int i = 0; i < terminationMarking.size(); i++) {
			if (pn.getPlaces().get(terminationMarking.get(i)[0]).getTokens() >= terminationMarking.get(i)[1])
				finalPlaceMarking++;
		}

		if (finalPlaceMarking == terminationMarking.size())
			return true;
		else
			return false;
	}

}
