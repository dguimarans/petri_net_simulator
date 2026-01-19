package com.petrinet.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.petrinet.model.PetriNet;

import static com.petrinet.model.ModelDefaultStrings.INITIAL_STATE_MARKER;
import static com.petrinet.model.ModelDefaultStrings.DEFAULT_TRANSITION_NAME_PREFIX;
import static com.petrinet.io.OutputFormat.*;

public class Output implements AutoCloseable {

	private final PrintWriter pw;

	public Output(String fileName) throws PetriNetException {
		try {
			Path filePath = Paths.get(fileName);
			Path directory = filePath.getParent();

			if(directory != null && !Files.exists(directory)) {
				Files.createDirectories(directory);
			}

			this.pw = new PrintWriter(new FileWriter(fileName));


		} catch (IOException e) {
			throw new PetriNetException("Error opening output file: " + fileName);
		}
	}

	/**
	 * Writes headers for full-state output format.
	 * Format: Transition;Time;Place1;Place2;...;PlaceN
	 */
	public void writeHeaders(PetriNet pn) {
		pw.write(CSV_HEADER_TRANSITION + CSV_DELIMITER + CSV_HEADER_TIME + CSV_DELIMITER);
		for(int i = 1; i < pn.getPlaces().size(); i++)
			pw.write(pn.getPlaces().get(i).getName() + CSV_DELIMITER);
		pw.write(pn.getPlaces().get(pn.getPlaces().size()).getName() + CSV_NEWLINE);
	}

	/**
	 * Writes headers for compact output format.
	 * Format: Transition;Time;Place;Tokens
	 */
	public void writeCompactHeaders() {
		pw.write(CSV_HEADER_TRANSITION + CSV_DELIMITER +
				CSV_HEADER_TIME + CSV_DELIMITER +
				CSV_HEADER_PLACE + CSV_DELIMITER +
				CSV_HEADER_PLACE_NAME + CSV_DELIMITER +
				CSV_HEADER_TOKENS + CSV_NEWLINE);
	}

	/**
	 * Writes the initial state for full-state output format.
	 */
	public void writeInitialState(double time, PetriNet pn) {
		pw.write(INITIAL_STATE_MARKER + CSV_DELIMITER + time + CSV_DELIMITER + pn.stateToString() + CSV_NEWLINE);
	}

	/**
	 * Writes a single line in compact format.
	 * Format: T{transition};{time};{placeId};{placeName};{tokens}
	 */
	public void writeCompactLine(int transition, double time, int placeId, String placeName, int tokens) {
		pw.write(DEFAULT_TRANSITION_NAME_PREFIX + transition + CSV_DELIMITER +
				time + CSV_DELIMITER +
				placeId + CSV_DELIMITER +
				placeName + CSV_DELIMITER +
				tokens + CSV_NEWLINE);
	}

	/**
	 * Writes a single line in full-state format.
	 * Format: T{transition};{time};{state}
	 */
	public void writeFullStateLine(int transition, double time, String stateString) {
		pw.write(DEFAULT_TRANSITION_NAME_PREFIX + transition + CSV_DELIMITER +
				time + CSV_DELIMITER +
				stateString + CSV_NEWLINE);
	}

	@Override
	public void close() throws PetriNetException {
		if(pw != null) {
			pw.close();
		}
	}

}
