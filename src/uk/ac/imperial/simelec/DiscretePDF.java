package uk.ac.imperial.simelec;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * Describes a discrete probability density function.
 * 
 * @author James Keirstead
 *
 */
public class DiscretePDF {

	// Member fields
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
		
		this.normalize();
	}
	
	/**
	 * Normalizes the values in this DiscretePDF so that their sum
	 * equals one.
	 * 
	 */
	protected void normalize() {
		
		double sum = this.sum();
		
		// Do the normalization
		for (int i = 0; i<values.length; i++) {
			values[i] = values[i]/sum;			
		}
	}

	/**
	 * Gets the cumulative distribution of this DiscretePDF.
	 * 
	 * @return a vector of float values giving the cumulative distribution
	 */
	public double[] getCDF() {
		
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
	 * Draw from this DiscretePDF.
	 * 
	 * @return an integer giving the index of the selected interval
	 */
	public int getRandomIndex() {

		if (this.sum()!=1f) {
			this.normalize();
		}
		
		// Draw a random value value
		float rand = (float) Uniform.staticNextDouble();
		
		// Initialize the loop
		int interval = 0;
		double[] cumdist = this.getCDF();
		do {
			if (rand <= cumdist[interval])
				break;
			interval++;
		} while (interval < values.length);
		
		return interval;
	}

	/**
	 * Calculate the sum of this DiscretePDF
	 * 
	 * @return	a float
	 */
	private double sum() {
		
		double sum = 0;
		
		for (double v : values) {
			sum = sum + v;
		}
		return sum;
	}

	/**
	 * Get the values describing this DiscretePDF
	 * 
	 * @return an array of double
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
