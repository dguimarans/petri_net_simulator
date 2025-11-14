package com.petrinet.cli;

import static com.petrinet.io.OutputFormat.*;
import static com.petrinet.cli.CommandLineConstants.*;
import com.petrinet.engine.SimulationControl;

import com.petrinet.engine.SimulationEngine;
import com.petrinet.io.PetriNetException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Simulate {

	public static void main(String[] args) throws PetriNetException {

		if(args.length < 2) {
			throw new PetriNetException(USAGE_MESSAGE);
		}
		
		if(args.length > 2 && args[2].matches(NUMERIC_PATTERN))
			SimulationControl.setSimulationRuns(Integer.valueOf(args[2]).intValue());	
		
		boolean verbose = false;
		if(args.length > 3 && args[3].equals(VERBOSE_FLAG))
			verbose = true;
		
		for(int i = 1; i <= SimulationControl.SIMULATION_RUNS; i++) {
			SimulationEngine simulation = new SimulationEngine(args[0], args[1].split(FILE_EXTENSION_PATTERN)[0] + OUTPUT_FILE_SEPARATOR + i + OUTPUT_FILE_EXTENSION, verbose);
			simulation.run();
			
			if(verbose) 
				log.info("---");
			log.info("Simulation {} ended at time: {}", i, simulation.getSimulationTime());
			if(verbose) 
				log.info("============");
		}
		
		log.info("Outputs written in {}{}[simulationRun]{}", args[1].split(FILE_EXTENSION_PATTERN)[0], OUTPUT_FILE_SEPARATOR, OUTPUT_FILE_EXTENSION);		
	}
	
}
