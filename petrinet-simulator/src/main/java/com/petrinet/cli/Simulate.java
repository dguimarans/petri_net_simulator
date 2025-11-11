package com.petrinet.cli;

import com.petrinet.engine.SimulationEngine;
import com.petrinet.io.PetriNetException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Simulate {

	public static void main(String[] args) throws PetriNetException {

		if(args.length < 2) {
			throw new PetriNetException("Syntax: java petrinet.Simulate inputFileName outputFileName [simulationRuns] [-verbose]");
		}
		
		int simulationRuns = 1;
		if(args.length > 2 && args[2].matches("\\d+"))
			simulationRuns = Integer.valueOf(args[2]).intValue();
		
		boolean verbose = false;
		if(args.length > 3 && args[3].equals("-verbose"))
			verbose = true;
		
		for(int i = 1; i <= simulationRuns; i++) {
			SimulationEngine simulation = new SimulationEngine(args[0], args[1].split("\\.")[0] + "_" + i + ".csv", verbose);
			simulation.run();
			
			if(verbose) 
				log.info("---");
			log.info("Simulation {} ended at time: {}", i, simulation.getSimulationTime());
			if(verbose) 
				log.info("============");
		}
		
		log.info("Outputs written in {}_[simulationRun].csv", args[1].split("\\.")[0]);		
	}
	
}
