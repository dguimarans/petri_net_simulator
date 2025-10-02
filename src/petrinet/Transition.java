package petrinet;

public class Transition {
	private static int nTransitions = 1;
	private int id;
	private int[] placesIn;
	private int[] placesOut;
	private int[] weightsIn;
	private int[] weightsOut;
	
	private boolean timed;
	private int distribution;
	private double param1;	// LB for uniform distributions, LB for triangular, LB for bounded exponential, RATE (lambda) for exponential, MEAN for normal and lognormal, SHAPE for gamma.
	private double param2;	// UB for uniform distributions, UB for triangular, SD for normal and lognormal, MEAN (1/lambda) for bounded exponential, SCALE for gamma.
	private double param3;	// MODE for triangular, THRESHOLD for gamma.
	private Distributions distributions;
	
	private int firings;
	
	public Transition(){
		this.id = nTransitions++;
		this.firings = 0;
		this.timed = false;
	}
	
	public Transition(int[] placesIn, int[] placesOut, int[] weightsIn, int[] weightsOut){
		this.id = nTransitions++;
		this.placesIn = placesIn;
		this.placesOut = placesOut;
		this.weightsIn = weightsIn;
		this.weightsOut = weightsOut;
		this.timed = false;
		this.firings = 0;
	}
	
	public Transition(int id, int[] placesIn, int[] placesOut, int[] weightsIn, int[] weightsOut){
		this.id = id;
		this.placesIn = placesIn;
		this.placesOut = placesOut;
		this.weightsIn = weightsIn;
		this.weightsOut = weightsOut;
		this.timed = false;
		this.firings = 0;
	}
	
	public Transition(int[] placesIn, int[] placesOut, int[] weightsIn, int[] weightsOut, String distribution){
		this.id = nTransitions++;
		this.placesIn = placesIn;
		this.placesOut = placesOut;
		this.weightsIn = weightsIn;
		this.weightsOut = weightsOut;
		this.firings = 0;
		
		this.timed = true;
		setDistribution(distribution);	
	}
	
	public Transition(int id, int[] placesIn, int[] placesOut, int[] weightsIn, int[] weightsOut, String distribution){
		this.id = id;
		this.placesIn = placesIn;
		this.placesOut = placesOut;
		this.weightsIn = weightsIn;
		this.weightsOut = weightsOut;
		this.firings = 0;
		
		this.timed = true;
		setDistribution(distribution);	
	}
	
	private void setDistribution(String distribution) {
		
		this.distributions = new Distributions();
		
		String[] distrString = distribution.split("\\(");
		// 0: constant, 1: uniform, 2: exponential, 3: normal, 4: triangular, 5: lognormal, 6: bounded exponential, 7: gamma
		switch(distrString[0].trim()) {
		case "CON": case "con":
			this.distribution = 0;
			this.param1 = Double.valueOf(distrString[1].trim().substring(0, distrString[1].trim().length() - 1)).doubleValue();
			this.param2 = 0;
			this.param3 = 0;
			break;
		case "UNI": case "uni":
			this.distribution = 1;
			String cleanParams = distrString[1].trim().substring(0, distrString[1].trim().length() - 1);
			if(cleanParams.length() > 0) {
				String[] paramArray = cleanParams.split(",");
				this.param1 = Double.valueOf(paramArray[0]).doubleValue();
				this.param2 = Double.valueOf(paramArray[1]).doubleValue();
				this.param3 = 0;
			} else {
				this.param1 = 0;
				this.param2 = 1;
				this.param3 = 0;
			}
			break;
		case "EXP": case "exp":
			this.distribution = 2;
			this.param1 = Double.valueOf(distrString[1].trim().substring(0, distrString[1].trim().length() - 1)).doubleValue();
			this.param2 = 0;
			this.param3 = 0;
			break;
		case "NOR": case "nor":
			this.distribution = 3;
			String cleanParamsNormal = distrString[1].trim().substring(0, distrString[1].trim().length() - 1);
			if(cleanParamsNormal.length() > 0) {
				String[] paramArray = cleanParamsNormal.split(",");
				this.param1 = Double.valueOf(paramArray[0]).doubleValue();
				this.param2 = Double.valueOf(paramArray[1]).doubleValue();
				this.param3 = 0;
			} else {
				this.param1 = 0;
				this.param2 = 1;
				this.param3 = 0;
			}
			break;
		case "TRI": case "tri":
			this.distribution = 4;
			String[] cleanParamsTriangular = distrString[1].trim().substring(0, distrString[1].trim().length() - 1).split(",");
			double[] paramsTriang = sortTriangValues(cleanParamsTriangular);
			this.param1 = paramsTriang[0];
			this.param2 = paramsTriang[2];
			this.param3 = paramsTriang[1];
			break;
		case "LOG": case "log":
			this.distribution = 5;
			String[] cleanParamsLNormal = distrString[1].trim().substring(0, distrString[1].trim().length() - 1).split(",");
			this.param1 = Double.valueOf(cleanParamsLNormal[0]).doubleValue();
			this.param2 = Double.valueOf(cleanParamsLNormal[1]).doubleValue();
			this.param3 = 0;
			break;
		case "BEX": case "bex":
			this.distribution = 6;
			String[] cleanParamsBoundExp = distrString[1].trim().substring(0, distrString[1].trim().length() - 1).split(",");
			this.param1 = Double.valueOf(cleanParamsBoundExp[0]).doubleValue();
			this.param2 = Double.valueOf(cleanParamsBoundExp[1]).doubleValue();
			this.param3 = 0; 
			break;
		case "GAM": case "gam":
			this.distribution = 7;
			String[] cleanParamsBoundGam = distrString[1].trim().substring(0, distrString[1].trim().length() - 1).split(",");
			this.param1 = Double.valueOf(cleanParamsBoundGam[0]).doubleValue();
			this.param2 = Double.valueOf(cleanParamsBoundGam[1]).doubleValue();
			this.param3 = Double.valueOf(cleanParamsBoundGam[2]).doubleValue(); 
			break;
		default:
			this.distribution = 0;
			this.param1 = 0;
			this.param2 = 0;
			this.param3 = 0;
		}		
	}
	
	private double[] sortTriangValues(String[] paramsArray) {
		double[] sortedValues = new double[paramsArray.length];
		
		for(int i = 0; i < paramsArray.length; i++)
			sortedValues[i] = Double.valueOf(paramsArray[i]).doubleValue(); 
		
		for(int i = 0; i < sortedValues.length - 1; i++) {
			for(int j = 0; j < sortedValues.length - i - 1; j++) {
				if(sortedValues[j] > sortedValues[j + 1]) {
					double temp = sortedValues[j];
					sortedValues[j] = sortedValues[j + 1];
					sortedValues[j + 1] = temp;
				}
			}
		}
		
		return sortedValues;
	}
	
	public void setPlacesIn(int[] placesIn){
		this.placesIn = placesIn;
	}
	
	public void setPlacesOut(int[] placesOut){
		this.placesOut = placesOut;
	}
	
	public void setWeightsIn(int[] weightsIn){
		this.weightsIn = weightsIn;
	}
	
	public void setWeightsOut(int[] weightsOut){
		this.weightsOut = weightsOut;
	}
	
	public int getId(){
		return this.id;
	}
	
	public int[] getPlacesIn(){
		return this.placesIn;
	}
	
	public int[] getPlacesOut(){
		return this.placesOut;
	}
	
	public int[] getWeightsIn(){
		return this.weightsIn;
	}
	
	public int[] getWeightsOut(){
		return this.weightsOut;
	}
	
	public int getFirings(){
		return this.firings;
	}
	
	public boolean isTimed(){
		return this.timed;
	}
	
	public void countFirings(){
		this.firings++;
	}
	
	public double call() {
		switch(distribution) {
		case 0:
			return this.param1;
		case 1:
			if(param1 == 0 && param2 == 1)
				return distributions.nextUnif();
			else 
				return distributions.nextUnif(param1, param2);
		case 2:
			return distributions.nextExp(param1);
		case 3:
			if(param1 == 0 && param2 == 1)
				return distributions.nextNormal();
			else
				return distributions.nextNormal(param1, param2);
		case 4:
			return distributions.nextTriang(param1, param2, param3);
		case 5:
			return distributions.nextLogNormal(param1, param2);
		case 6:
			return distributions.nextBoundedExp(param1, param2);
		case 7:
			return distributions.nextGamma(param1, param2, param3);
		default:
			return 0;
		}
	}

}
