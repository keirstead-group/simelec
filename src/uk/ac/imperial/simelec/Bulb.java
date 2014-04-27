package uk.ac.imperial.simelec;

import cern.jet.random.Uniform;

/**
 * Describes a single light bulb.
 * 
 * @author James Keirstead
 * 
 */
public class Bulb extends Load {

	// Member fields
	protected int rating; // in Watts
	protected float weight; // dimensionless

	/**
	 * Create a new Bulb with a specified id, rating, and weighting.
	 * 
	 * @param id
	 *            an int giving a numeric id
	 * @param rating
	 *            an int giving the power consumption in Watts
	 * @param weight
	 *            a float giving the bulb use weighting. When multiplied by the
	 *            effective occupancy of the dwelling, this gives the
	 *            probability that a light will be switched on in low light
	 *            conditions. For example, with one effective occupant, a weight
	 *            of 1 means that the Bulb would always be turned on in low
	 *            light; 0 means it would never be turned on.
	 */
	public Bulb(int id, int rating, float weight) {
		this.id = this.getClass().getSimpleName().toUpperCase().concat("_")
				.concat(String.valueOf(id));
		this.rating = rating;
		this.weight = weight;
	}

	/**
	 * Create a new Bulb with a specified id and rating.
	 * 
	 * @param id
	 *            an int giving a numeric id
	 * @param rating
	 *            an int giving the power consumption in Watts
	 */
	public Bulb(int id, int rating) {
		this(id, rating, Bulb.getCalibratedWeight());		
	}

	/**
	 * Gets a random calibrated weight. The method calculates the weight so that
	 * the average consumption of the Bulb over a large number of simulations
	 * reflects observed consumption from UK data.
	 * 
	 * @return a float giving a calibrated weight
	 */
	protected static float getCalibratedWeight() {

		// This calibration scaler is used to ensure that the output of a Bulb
		// provides a sensible average output over a large number of runs.
		float calibration = 0.008153686f;

		// Calculate the random bulb use weighting
		float randomWeight = (float) (-calibration * Math.log(Uniform
				.staticNextDouble()));

		return randomWeight;
	}

	@Override
	public String toString() {
		return (String.format("%d: %d W, cf = %.4f", this.id, this.rating,
				this.weight));
	}

	/**
	 * Turns this Bulb on at a specified time step. This means that the
	 * consumption of the bulb in period <code>t</code> is set equal to the
	 * Bulb's rating.
	 * 
	 * @param t
	 *            an int giving the time period measured in one minute intervals
	 *            during the day, i.e. 0 = 00:00 and 1439 = 23:59.
	 */
	public void on(int t) {
		if (t > 0 & t < consumption.length) {
			this.consumption[t] = this.rating;
		}
	}
}
