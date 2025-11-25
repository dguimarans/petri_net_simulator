package com.petrinet.cli;

import static com.petrinet.io.OutputFormat.*;
import static com.petrinet.cli.CommandLineConstants.*;

import com.petrinet.config.ExecutionControl;
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
		
		// Parse execution parameters and configure the run
		ExecutionControl executionControl = new ExecutionControl();

		if(args.length > 2 && args[2].matches(NUMERIC_PATTERN))
			executionControl.setSimulationRuns(Integer.parseInt(args[2]));	
		
		if(args.length > 3 && args[3].equals(VERBOSE_FLAG))
			executionControl.setVerbose(true);

		// Parse simulation-level parameters from file
        // TODO: Will be done by PetriNetFileParser in future
        SimulationControl simulationControl = new SimulationControl();
        // simulationControl = parser.parseSimulationControl(args[0]);
        
		// Copy verbose flag from execution to simulation level
        simulationControl.setVerbose(executionControl.isVerbose());

		
		for(int i = 1; i <= executionControl.getSimulationRuns(); i++) {
			SimulationEngine simulation = new SimulationEngine(args[0], args[1].split(FILE_EXTENSION_PATTERN)[0] + OUTPUT_FILE_SEPARATOR + i + OUTPUT_FILE_EXTENSION, simulationControl);
			simulation.run();
			
			if(executionControl.isVerbose()) 
				log.info("---");
			log.info("Simulation {} ended at time: {}", i, simulation.getSimulationTime());
			if(executionControl.isVerbose()) 
				log.info("============");
		}
		
		log.info("Outputs written in {}{}[simulationRun]{}", args[1].split(FILE_EXTENSION_PATTERN)[0], OUTPUT_FILE_SEPARATOR, OUTPUT_FILE_EXTENSION);		
	}
	
}
