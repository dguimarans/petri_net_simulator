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

	public double nextGamma(double shape, double scale, double threshold) {
		if (shape <= 0 || scale <= 0)
			return 0;
		else
			return nextGamma(shape) * scale + threshold;
	}

	private double nextGamma(double shape) {
		double x = 0, b, p, u, v, n;

		/*
		 * Shape parameter 0 < shape < 1:
		 * 
		 * Ahrens, J.H. and Dieter, U. (1974). Computer methods for sampling
		 * from gamma, beta, poisson and binomial distributions. Computing, 12,
		 * 223-246.
		 */

		if (shape < 1) {
			b = 1 + shape * Math.exp(-1);

			for (;;) {
				p = b * nextUnif();
				if (p > 1) {
					x = -Math.log((b - p) / shape);
					if (nextUnif() <= Math.pow(x, shape - 1))
						return x;
				} else {
					x = Math.pow(p, 1 / shape);
					if (nextUnif() <= Math.exp(-x))
						return x;

				}
			}
		}

		else if (shape == 1)
			return -Math.log(nextUnif());

		/*
		 * Shape parameter > 1:
		 * 
		 * Marsaglia, G and Tsang, W. A simple method for generating gamma
		 * variables. ACM Transactions on Mathematical Software, 26(3):363-372,
		 * 2000.
		 */

		else {

			b = shape - 1. / 3.;
			p = 1. / Math.sqrt(9 * b);
			for (;;) {
				do {
					n = nextNormal();
					v = 1. + p * n;
				} while (v <= 0);

				v = v * v * v;
				u = nextUnif();
				if (u < 1. - 0.0331 * (n * n) * (n * n))
					return (b * v);
				if (Math.log(u) < 0.5 * n * n + b * (1. - v + Math.log(v)))
					return (b * v);
			}

		}

	}
	
}
