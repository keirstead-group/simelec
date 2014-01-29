package uk.ac.imperial.simelec;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * Describes a discrete probability density function
 * 
 * Switch to http://jdistlib.sourceforge.net/ or http://www.jsc.nildram.co.uk/?
 * 
 * @author jkeirste
 *
 */
public class DiscretePDF {

	private double[] values;
	
	/**
	 * Creates a new DiscretePDF with a given set of values.  If negative values
	 * are contained in <code>values</code> array, these will be replaced with
	 * zeros.
	 * 
	 * @param values an array of non-negative values  
	 */
	public DiscretePDF(double[] values) {
		this.values = new double[values.length];
		for (int i=0; i<values.length; i++) {
			this.values[i] = values[i]<0 ? 0 : values[i];			
		}		
	}
	
	/**
	 * Normalizes all of the values in this DiscretePDF so that their sum
	 * equals one.
	 * 
	 */
	public void normalize() {
		
		// Calculate the sum
		double sum = 0;
		for (double v : values) {
			sum = sum + v;
		}
		
		// Do the normalization
		for (int i = 0; i<values.length; i++) {
			values[i] = values[i]/sum;			
		}
	}

	/**
	 * Calculates the cumulative distribution of this DiscretePDF.
	 * 
	 * @return a vector of float values giving the cumulative distribution
	 */
	public double[] cumsum() {
		
		// Create a placeholder
		double[] cumdist = new double[values.length];
		
		// Calculate the sum
		double prev = 0;
		for (int i = 0; i < cumdist.length; i++) {
			cumdist[i] = prev + values[i];
			prev = cumdist[i];
		}
		return (cumdist);
	}
	
	/**
	 * Draws from a discrete probability density function.
	 * 
	 * @return an integer giving the index of the selected interval
	 */
	public int getRandomIndex() {

		// Normalize the values 
		this.normalize();
		
		// Draw the value
		float rand = (float) Uniform.staticNextDouble();
		
		// Initialize the loop
		int interval = 0;
		double[] cumdist = this.cumsum();
		do {
			if (rand <= cumdist[interval])
				break;
			interval++;
		} while (interval < values.length);
		
		return (interval);
	}

	/**
	 * Get the values describing this DiscretePDF
	 * 
	 * @return
	 */
	public double[] getValues() {
		return values;
	}

	/**
	 * Sets the seed for the random number generator.
	 * 
	 * @param seed
	 *            an int giving the seed
	 */
	public static void setSeed(int seed) {
		RandomEngine engine = new MersenneTwister(seed);
		Uniform.staticSetRandomEngine(engine);
	}
	
	
}
