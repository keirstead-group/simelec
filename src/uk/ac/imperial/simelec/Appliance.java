package uk.ac.imperial.simelec;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;

/**
 * Describes an electrical appliance.
 * 
 * @author James Keirstead
 * 
 */
public class Appliance {

	// Member fields
	public int power;
	String name;
	String use_profile;
	private double ownership_rate;
	int standby_power;
	private int mean_power;
	private int rated_power;
	double cycles_per_year;
	private int cycle_length;
	private int restart_delay;
	double calibration;
	private boolean owned = false;
	private int cycle_time_left = 0;
	int restart_delay_time_left = 0;
	protected double[] consumption = new double[1440]; // W

	/**
	 * Creates a new appliance with specified attributes. The constructor uses
	 * this information to set a random start delay for restarting appliances
	 * and a random active power demand based on the specified mean.
	 * 
	 * 
	 * @param name
	 *            a String giving the plain text name
	 * @param profile
	 *            a String describing the use profile
	 * @param ownership
	 *            a double giving the ownership probability
	 * @param standby
	 *            an int giving the standby power demand in Watts
	 * @param mean
	 *            an int giving the mean power demand in Watts
	 * @param cycles
	 *            a double giving the average number of duty cycles per year
	 * @param length
	 *            an int giving the length of an average cycle in minutes
	 * @param restart
	 *            an int giving the delay between cycles in minutes
	 * @param calibration
	 *            a double giving a calibration constant. See
	 *            <link>http://dx.doi.org/10.1016/S0378-7788(02)00241-4</link>
	 */
	public Appliance(String name, String profile, double ownership,
			int standby, int mean, double cycles, int length, int restart,
			double calibration) {

		this.name = name.toUpperCase();
		this.use_profile = profile.toUpperCase();
		this.ownership_rate = ownership;

		// this.total_energy = energy;
		this.standby_power = standby;
		this.mean_power = mean;
		this.cycles_per_year = cycles;
		this.cycle_length = length;
		this.restart_delay = restart;
		this.calibration = calibration;

		// Randomly delay the start of appliances that have a restart
		// delay
		this.setRestartDelay();
		// Make the rated power variable over a normal distribution to
		// provide some variation
		this.setRatedPower();

	}

	/**
	 * Sets the rated power of this Appliance. Assumes that the true rated power
	 * is normally distributed about the stated mean power for the appliance
	 * type.
	 */
	private void setRatedPower() {
		rated_power = (int) Normal
				.staticNextDouble(mean_power, mean_power / 10);

	}

	/**
	 * Sets the restart delay for this Appliance. Assumes that the delay is
	 * uniformly distributed with an expected value equal to the stated restart
	 * delay.
	 */
	private void setRestartDelay() {
		restart_delay_time_left = (int) Uniform.staticNextDouble()
				* restart_delay * 2;
	}

	/**
	 * Starts this Appliance. Calling this method will calculate a new cycle
	 * length, reset the restart delay, and calculate the
	 */
	public void start() {

		// Determine how long this appliance is going to be on for
		cycle_time_left = calculateCycleLength();

		// Determine if this appliance has a delay after the cycle before it
		// can restart
		restart_delay_time_left = this.restart_delay;

		// Set the power
		this.power = getPowerUsage(cycle_time_left);

		// Decrement the cycle time left
		cycle_time_left--;

	}

	/**
	 * A convenience function to test if a value is within specified limits.
	 * 
	 * @param x
	 *            an int giving the value to test
	 * @param lower
	 *            an int giving the lower limit
	 * @param upper
	 *            an int giving the upper limit
	 * @return a boolean <code>true</code> if x>=lower AND x<=upper
	 */
	private static boolean isBetween(int x, int lower, int upper) {
		return lower <= x && x <= upper;
	}

	/**
	 * Gets the power consumed by this Appliance at a certain point in its cycle
	 * 
	 * @param cycle_time_left
	 *            an int giving the number of minutes left in the Appliance's
	 *            cycle
	 * @return an int giving the power consumption in Watts at this time
	 */
	public int getPowerUsage(int cycle_time_left) {

		// Set the working power to the rated power
		int tmp_power = this.rated_power;

		// Some appliances have a custom (variable) power profile depending
		// on the time left
		if (this.name.equals("WASHING_MACHINE")
				|| this.name.equals("WASHER_DRYER")) {

			int total_cycle_time = 0;

			// Calculate the washing cycle time
			if (this.name.equals("WASHING_MACHINE"))
				total_cycle_time = 138;
			if (this.name.equals("WASHER_DRYER"))
				total_cycle_time = 198;

			// This is an example power profile for an example washing
			// machine. This simplistic model is based upon data from personal
			// communication with a major washing machine manufacturer
			int tmp = total_cycle_time - cycle_time_left + 1;

			if (isBetween(tmp, 1, 8)) {
				tmp_power = 73; // start-up and fill
			} else if (isBetween(tmp, 9, 29)) {
				tmp_power = 2056; // heating
			} else if (isBetween(tmp, 30, 81)) {
				tmp_power = 73; // Wash and drain
			} else if (isBetween(tmp, 82, 92)) {
				tmp_power = 73; // spin
			} else if (isBetween(tmp, 93, 94)) {
				tmp_power = 250; // rinse
			} else if (isBetween(tmp, 95, 105)) {
				tmp_power = 73; // Spin
			} else if (isBetween(tmp, 106, 107)) {
				tmp_power = 250; // rinse
			} else if (isBetween(tmp, 108, 118)) {
				tmp_power = 73; // Spin
			} else if (isBetween(tmp, 119, 120)) {
				tmp_power = 250; // rinse
			} else if (isBetween(tmp, 121, 131)) {
				tmp_power = 73; // Spin
			} else if (isBetween(tmp, 132, 133)) {
				tmp_power = 250; // rinse
			} else if (isBetween(tmp, 134, 138)) {
				tmp_power = 568; // fast spin
			} else if (isBetween(tmp, 139, 198)) {
				tmp_power = 2500; // Drying cycle
			} else {
				tmp_power = this.standby_power;
			}

		}

		return (tmp_power);

	}

	/**
	 * Calculate the length of this Appliance's operating cycle. The duration
	 * (in minutes) is calculated as follows:
	 * <ul>
	 * <li>For televisions, an average viewing time of 73 minutes is assumed
	 * from Time Use Survey data with some random variation.</li>
	 * <li>For storage and electric space heaters, the cycle time is assumed to
	 * be normally distributed based on the specified cycle length</li>
	 * <li>For all other appliances, the specified cycle length is assumed with
	 * no random variation.</li>
	 * </ul>
	 * 
	 * @return an int giving the operating cycle length in minutes
	 */
	private int calculateCycleLength() {
		// Set the value to that provided in the configuration
		int length = this.cycle_length;

		// Use the TV watching length data approximation, derived from the
		// TUS data
		if ((this.name.equals("TV1")) || (this.name.equals("TV2"))
				|| (this.name.equals("TV3"))) {

			// The cycle length is approximated by the following function
			// The average viewing time is approximately 73 minutes
			length = (int) Math.round(70 * Math.pow(
					(0 - Math.log10(1 - Uniform.staticNextDouble())), 1.1));

		} else if ((this.name.equals("STORAGE_HEATER"))
				|| (this.name.equals("ELEC_SPACE_HEATING"))) {

			// Provide some variation on the cycle length of heating
			// appliances
			length = (int) Normal.staticNextDouble(this.cycle_length,
					this.cycle_length / 10);
		}

		return length;
	}

	/**
	 * Assigns ownership of this Appliance. Draws a random number and if this is
	 * less than the average ownership rate specified in the constructor, then
	 * this Appliance is deemed to be owned.
	 */
	public void assignOwnership() {
		double rnd = Uniform.staticNextDouble();
		owned = (rnd < this.ownership_rate);
	}

	/**
	 * Is this Appliance owned?
	 * 
	 * @return a boolean stating whether or not this appliance is owned.
	 */
	public boolean isOwned() {
		return (this.owned);
	}

	/**
	 * Runs the appliance. In other words, sets the power demand for the current
	 * point in the cycle and step the timer.
	 * 
	 */
	public void run() {

		// Set the power
		power = getPowerUsage(cycle_time_left);

		// Decrement the cycle time left
		cycle_time_left--;

	}

	/**
	 * Checks if this Appliance is off.
	 * 
	 * @return a boolean <code>true</code> if the current cycle time remaining
	 *         is less than or equal to zero.
	 */
	public boolean isOff() {
		return (cycle_time_left <= 0);
	}
	
	/**
	 * Converts this Appliance object into a formatted String array for export.
	 * 
	 * @return a String array containing the Appliance's name, and then
	 *         1440 entries representing the consumption in Watts at each minute
	 *         interval.
	 */
	public String[] toExportString() {
		String[] tmp = new String[consumption.length + 1];
		tmp[0] = this.name;
		for (int i = 0; i < consumption.length; i++)
			tmp[i + 1] = String.valueOf(consumption[i]);
		return tmp;
		
	}

}
