package com.petrinet.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.petrinet.model.*;
import com.petrinet.engine.SimulationControl;

import static com.petrinet.io.PetriNetFileFormat.*;


public class PetriNetFileParser {

    public static ParsedPetriNet parse(String fileName) throws PetriNetException {
		Map<String, List<String>> sections;

        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            sections = readAllSections(br);

        } catch(FileNotFoundException fnf) {
			throw new PetriNetFileNotFoundException("Petri Net file not found.");
		} catch (IOException ioe) {
			throw new PetriNetParseException("Petri Net: Can't read the specified file.");
		}

		if(!sections.containsKey(SECTION_TERMINATION_TIME) && !sections.containsKey(SECTION_TERMINATION_MARKING)) {
			throw new PetriNetSimulationException(
					"Simulation Engine: Unbounded simulation detected. No termination criteria specified or bad format.");
		}

		PetriNet petriNet = buildPetriNet(sections);
        SimulationControl simulationControl = buildSimulationControl(sections, petriNet);

        return new ParsedPetriNet(petriNet, simulationControl);
    }

    private static Map<String, List<String>> readAllSections(BufferedReader br) throws IOException {
        Map<String, List<String>> sections = new HashMap<>();
        String currentSection = null;
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
                continue; // Skip empty lines and comments
            }
            if (line.startsWith("@")) {
                currentSection = line;
                sections.put(currentSection, new ArrayList<>());
            } else if (currentSection != null) {
                sections.get(currentSection).add(line);
            }
        }
        
		return sections;
    }

    private static PetriNet buildPetriNet(Map<String, List<String>> sections) throws PetriNetException {
        List<String> placeLines = sections.get(SECTION_PLACES);
        List<String> transitionLines = sections.get(SECTION_TRANSITIONS);

        if (placeLines == null || transitionLines == null) {
            throw new PetriNetParseException("Petri Net: Missing required sections in Petri Net file.");
        }

        PetriNet petriNet = new PetriNet(placeLines.size(), transitionLines.size());

        int placeId = 1;
        for (String line : placeLines) {
                petriNet.addPlace(parsePlaceLine(line, placeId++));
        }

        int transitionId = 1;
        for (String line : transitionLines) {
                petriNet.addTransition(parseTransitionLine(line, transitionId++));
        }

        return petriNet;
    }

	private static SimulationControl buildSimulationControl(Map<String, List<String>> sections, PetriNet petriNet) throws PetriNetException {
		List<String> terminationTimeLines = sections.get(SECTION_TERMINATION_TIME);
		List<String> terminationMarkingLines = sections.get(SECTION_TERMINATION_MARKING);

		SimulationControl simulationControl = new SimulationControl();

		if (terminationTimeLines != null && !terminationTimeLines.isEmpty()) {
			String line = terminationTimeLines.get(0);
			try {
				double terminationTime = Double.parseDouble(line);
				simulationControl.setTerminationByTime(true);
				simulationControl.setTerminationTime(terminationTime);
			} catch (NumberFormatException e) {
				throw new PetriNetParseException("Simulation Control: Invalid termination time format.");
			}
		}

		if (terminationMarkingLines != null && !terminationMarkingLines.isEmpty()) {
			simulationControl.setTerminateByMarking(true);

			for(String line : terminationMarkingLines) {
				String[] placeGoalState = line.split(FIELD_DELIMITER);
				try {
					int placeId = Integer.parseInt(placeGoalState[0].trim());
					int tokens = Integer.parseInt(placeGoalState[1].trim());

					if (!petriNet.getPlaces().containsKey(placeId)) {
						throw new PetriNetParseException("Simulation Control: Termination marking refers to non-existent place: P " + placeId);
					}

					simulationControl.addTerminationMarking(placeId, tokens);
				} catch (NumberFormatException e) {
					throw new PetriNetParseException("Simulation Control: Invalid termination marking format.");
				}
			}
		}

		return simulationControl;
	}

    private static Place parsePlaceLine(String line, int placeId) throws PetriNetParseException {
		String[] parts = line.split(FIELD_DELIMITER);
		
		if(parts.length == 1)
			return new Place(placeId, Integer.parseInt(line));
		else if(parts.length == 2)
			return new Place(placeId, Integer.parseInt(parts[0]), parts[1]);
		else
			throw new PetriNetParseException("Invalid place format: " + line);
	}

	private static Transition parseTransitionLine(String line, int transitionId) throws PetriNetParseException {
		String[] splitLine = line.split(FIELD_DELIMITER);

		String[] pIn = splitLine[0].split(ARRAY_DELIMITER);
		String[] pOut = splitLine[1].split(ARRAY_DELIMITER);
		String[] wIn = splitLine[2].split(ARRAY_DELIMITER);
		String[] wOut = splitLine[3].split(ARRAY_DELIMITER);
		String distribution = (splitLine.length == 5) ? splitLine[4].trim() : null;

		if(pIn.length != wIn.length || pOut.length != wOut.length) {
			throw new PetriNetParseException("Transition definition error: Mismatched input/output places and weights.");
		}

		int[] placeIn = new int[pIn.length];
		int[] placeOut = new int[pOut.length];
		int[] weightIn = new int[wIn.length];
		int[] weightOut = new int[wOut.length];

		for(int i = 0; i < placeIn.length; i++)
			placeIn[i] = Integer.parseInt(pIn[i]);
		for (int i = 0; i < placeOut.length; i++)
			placeOut[i] = Integer.parseInt(pOut[i]);
		for (int i = 0; i < weightIn.length; i++)
			weightIn[i] = Integer.parseInt(wIn[i]);
		for (int i = 0; i < weightOut.length; i++)
			weightOut[i] = Integer.parseInt(wOut[i]);

		if (splitLine.length == 4)
			return new Transition(transitionId, placeIn, placeOut, weightIn, weightOut);
		else if (splitLine.length == 5)
			return new Transition(transitionId, placeIn, placeOut, weightIn, weightOut, distribution);
		else
			throw new PetriNetParseException("Invalid transition format: " + line);
	}

    /**
     * Container for parsed Petri Net file contents.
     * 
     * @param petriNet The parsed Petri Net model
     * @param simulationControl The parsed simulation configuration
     */
    public record ParsedPetriNet(PetriNet petriNet, SimulationControl simulationControl)  {}

    private PetriNetFileParser() {
        // Private constructor to prevent instantiation
        throw new AssertionError("Cannot instantiate utility class");
    }
}
