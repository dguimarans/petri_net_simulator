package petrinet;

import java.util.Random;

public class Distributions {
	
	Random rn;
	
	public Distributions(){
		super();
		rn = new Random();
	}
	
	public double nextUnif() {
		return rn.nextDouble(); 
	}
	
	public double nextUnif(double lb, double ub) {
		return (ub - lb) * rn.nextDouble() + lb;
	}
	
	public double nextNormal() {
		return rn.nextGaussian();
	}
	
	public double nextNormal(double mean, double sd) {
		return mean + sd * rn.nextGaussian();
	}
	
	public double nextExp(double rate) {
		return - Math.log(rn.nextDouble())/rate;
	}
	
	public double nextTriang(double a, double b, double c) {
		double f = (c - a) / (b - a);
		double u = rn.nextDouble();
		
		if(u < f)
			return a + Math.sqrt(u * (b - a) * (c - a));
		else
			return b - Math.sqrt((1 - u) * (b - a) * (b -c));
	}
	
	public double nextLogNormal(double mean, double stdev) {
		double ess = Math.log(1.0 + Math.pow(stdev/mean,2));
	    double mu = Math.log(mean) - (0.5 * Math.pow(ess, 2));
	    return Math.exp(mu + (ess * rn.nextGaussian()));
	}
	
	public double nextBoundedExp(double lb, double mean) {
		return lb - Math.log(rn.nextDouble()) * mean;
	}
	
}
