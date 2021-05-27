package petrinet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Output {
	
//	String directoryName = "./output";
	FileWriter fw;
	PrintWriter pw;
	
	public Output(String fileName) {
		try {
			if(fileName.contains("/")) {
				String directoryName = fileName.substring(0, fileName.lastIndexOf("/"));
				File directory = new File(directoryName);
				if (! directory.exists()){
					directory.mkdir();
				}
				this.fw = new FileWriter(directoryName + "/" + fileName.substring(fileName.lastIndexOf("/"),fileName.length()));
				this.pw = new PrintWriter(fw);
			} else {
				this.fw = new FileWriter(fileName);
				this.pw = new PrintWriter(fw);
			}
	    
	    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}
	
	public void writeHeaders(PetriNet pn) {
		pw.write("Transition,Time,");
		for(int i = 1; i < pn.getPlaces().size(); i++)
			pw.write(pn.getPlaces().get(i).getName() + ",");
		pw.write(pn.getPlaces().get(pn.getPlaces().size()).getName() + "\n");
	}
	
	public void writeInitialState(double time, PetriNet pn) {
		pw.write("T0," + time + "," + pn.stateToString() + "\n");
	}
	
	public void writeOutput(String line) {
		pw.write(line);
	}
	
	public void closeOutput() {
		try {
			pw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
