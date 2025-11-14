package com.petrinet.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.petrinet.model.PetriNet;

import static com.petrinet.model.ModelDefaultStrings.INITIAL_STATE_MARKER;
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
			throw new PetriNetException("Error opening output file");
		}   
	}
	
	public void writeHeaders(PetriNet pn) {
		pw.write(CSV_HEADER_TRANSITION + CSV_DELIMITER + CSV_HEADER_TIME + CSV_DELIMITER);
		for(int i = 1; i < pn.getPlaces().size(); i++)
			pw.write(pn.getPlaces().get(i).getName() + CSV_DELIMITER);
		pw.write(pn.getPlaces().get(pn.getPlaces().size()).getName() + CSV_NEWLINE);
	}
	
	public void writeInitialState(double time, PetriNet pn) {
		pw.write(INITIAL_STATE_MARKER + CSV_DELIMITER + time + CSV_DELIMITER + pn.stateToString() + CSV_NEWLINE);
	}
	
	public void writeOutput(String line) {
		pw.write(line);
	}
	
	@Override
	public void close() throws PetriNetException {
		if(pw != null) {
			pw.close();
		}
	}

}
