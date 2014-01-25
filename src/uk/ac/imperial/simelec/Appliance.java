package uk.ac.imperial.simelec;

import cern.jet.random.Normal;
import cern.jet.random.Uniform;

public class Appliance {

	public int power;
	String name;
	String use_profile;
	double ownership_rate;
	int standby_power;
	int mean_power;
	int rated_power;
	double cycles_per_year;
	int cycle_length;
	int restart_delay;
	double calibration;
	boolean owned = false;
	int cycle_time_left = 0;
	int restart_delay_time_left = 0;

	public Appliance(String name, String profile, double ownership,
			double energy, int standby, int mean, double cycles,
			int length, int restart, double calibration) {
		this.name = name;
		this.use_profile = profile.toUpperCase();
		this.ownership_rate = ownership;

		// this.total_energy = energy;
		this.standby_power = standby;
		this.mean_power = mean;
		this.cycles_per_year = cycles;
		this.cycle_length = length;
		this.restart_delay = restart;
		this.calibration = calibration;
	}

	public void setRatedPower() {
		rated_power = (int) Normal.staticNextDouble(mean_power,
				mean_power / 10);

	}

	public void setRestartDelay() {
		restart_delay_time_left = (int) Uniform.staticNextDouble()
				* restart_delay * 2;
	}

	public void start() {

		// Determine how long this appliance is going to be on for
		cycle_time_left = CycleLength();

		// Determine if this appliance has a delay after the cycle before it
		// can restart
		restart_delay_time_left = this.restart_delay;

		// Set the power
		this.power = GetPowerUsage(cycle_time_left);

		// Decrement the cycle time left
		cycle_time_left--;

	}

	public static boolean isBetween(int x, int lower, int upper) {
		return lower <= x && x <= upper;
	}
	
	public int GetPowerUsage(int cycle_time_left) {
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
			// machine
			// This simplistic model is based upon data from personal
			// communication with a major washing maching manufacturer
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

	private int CycleLength() {
		// Set the value to that provided in the configuration
		int CycleLength = this.cycle_length;

		// Use the TV watching length data approximation, derived from the
		// TUS data
		if ((this.name.equals("TV1")) || (this.name.equals("TV2"))
				|| (this.name.equals("TV3"))) {

			// The cycle length is approximated by the following function
			// The average viewing time is approximately 73 minutes
			CycleLength = (int) Math.round((70 * ((0 - Math.pow(
					Math.log((1 - Uniform.staticNextDouble())), 1.1)))));

		} else if ((this.name.equals("STORAGE_HEATER"))
				|| (this.name.equals("ELEC_SPACE_HEATING"))) {

			// Provide some variation on the cycle length of heating
			// appliances
			CycleLength = (int) Normal.staticNextDouble(this.cycle_length,
					this.cycle_length / 10);
		}

		return (CycleLength);
	}

	public void assign_ownership() {
		double rnd = Uniform.staticNextDouble();
		if (rnd < this.ownership_rate)
			owned = true;
	}

	public boolean isOwned() {
		return (this.owned);
	}
	
}
