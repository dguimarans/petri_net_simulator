package petrinet;

public class Simulate {

	public static void main(String[] args) {

		if(args.length < 2) {
			System.out.println("Syntax: java petrinet.Simulate inputFileName outputFileName [simulationRuns] [-verbose]");
			System.exit(0);
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
				System.out.println("---");
			System.out.println("Simulation " + i + " ended at time: " + simulation.getSimulationTime());
			if(verbose) 
				System.out.println("============");
		}
		
		System.out.println("Outputs written in " + args[1].split("\\.")[0] + "_[simulationRun].csv");
		
	}
	
}
