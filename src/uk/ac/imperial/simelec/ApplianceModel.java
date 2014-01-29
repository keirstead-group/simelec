/* Domestic Appliance Model - Simulation Example Code

    Copyright (C) 2008 Ian Richardson, Murray Thomson
    CREST (Centre for Renewable Energy Systems Technology),
    Department of Electronic and Electrical Engineering
    Loughborough University, Leicestershire LE11 3TU, UK
    Tel. +44 1509 635326. Email address: I.W.Richardson@lboro.ac.uk

	Java Implementation (c) 2014 James Keirstead
	Imperial College London
	j.keirstead@imperial.ac.uk
	
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.imperial.simelec;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Simulates the electricity demand for appliances in a household.
 * 
 * @author jkeirste
 * 
 */
public class ApplianceModel {

	// Class variables
	private int month;
	private boolean weekend;
	private String out_dir;
	private String occupancy_file = "occupancy_output.csv";
	private File out_file;

	// Data files
	// TODO Make these resources
	private static String activity_file = "data/activities.csv";
	private static String appliance_file = "data/appliances.csv";

	// Define the relative monthly temperatures
	// Data derived from MetOffice temperature data for the Midlands in 2007
	// (http://www.metoffice.gov.uk/climate/uk/2007/) Crown Copyright
	private static double[] oMonthlyRelativeTemperatureModifier = { 1.63,
			1.821, 1.595, 0.867, 0.763, 0.191, 0.156, 0.087, 0.399, 0.936,
			1.561, 1.994 };

	/**
	 * 
	 * Simulate the electricity demand from appliances for a household.
	 * 
	 * @param args
	 *            takes three arguments, plus one option. The first is an int
	 *            giving the month to simulate (1-12), the second is a
	 *            two-letter code indicating weekend (<code>we</code>) or
	 *            weekday (<code>wd</code>), and the third is a String giving
	 *            the output directory. The optional fourth argument is an int
	 *            giving a random number seed.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		int month = 1;
		boolean weekend = false;
		String dir = ".";

		// Check arguments
		if (args.length == 3 || args.length == 4) {
			month = Integer.valueOf(args[0]);
			weekend = args[1].equals("we") ? true : false;
			dir = args[2];

			if (args.length == 4)
				ApplianceModel.setSeed(Integer.valueOf(args[3]));
		} else {
			System.out.printf(
					"%d arguments detected.  Using default arguments.%n",
					args.length);
		}

		ApplianceModel model = new ApplianceModel(month, weekend, dir);
		model.run();
	}

	/**
	 * 
	 * Create an ApplianceModel with a specified month, weekday, and output
	 * directory.
	 * 
	 * @param month
	 *            an integer giving the month of the year to simulate (1-12)
	 * @param weekend
	 *            a boolean indicating whether to simulate a weekend
	 *            <code>true</code> or weekday <code>false</code>
	 * @param output_file
	 *            a string giving the path for the output file
	 */
	public ApplianceModel(int month, boolean weekend, String dir) {
		this.month = SimElec.validateMonth(month);
		this.weekend = weekend;
		this.out_dir = dir;
		this.out_file = new File(out_dir, "appliance_output.csv");
	}

	/**
	 * Run the appliance electricity demand simulation.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {

		System.out.print("Running appliance model...");
		
		File occupancy_file = new File(out_dir, this.occupancy_file);
		int[] occupancy = LightingModel.getOccupancy(occupancy_file.getPath());

		// Load in the basic data
		List<ProbabilityModifier> activities = loadActivityStatistics();
		List<Appliance> appliances = loadAppliances();

		// Assign the appliances to households
		configure_appliances(appliances);

		// Set up a map to hold the results
		Map<Appliance, double[]> results = new HashMap<Appliance, double[]>();

		// Simulate each appliance
		for (Appliance a : appliances) {

			if (!a.isOwned()) {
				// If the appliance isn't present, then store an empty array
				results.put(a, new double[1440]);
			} else {

				// Randomly delay the start of appliances that have a restart
				// delay
				a.setRestartDelay();

				// Make the rated power variable over a normal distribution to
				// provide some variation
				a.setRatedPower();

				// Initialise the daily simulation loop
				int time = 0;
				double[] power_vals = new double[1440];

				while (time < 1440) {
					// Set the default (standby) power demand at this time step
					a.power = a.standby_power;

					// Get the ten minute period count
					int iTenMinuteCount = (int) Math.floor((time - 1) / 10);

					// Get the number of current active occupants for this
					// minute
					// Convert from 10 minute to 1 minute resolution
					int iActiveOccupants = occupancy[(int) Math
							.floor((time - 1) / 10)];

					// If this appliance is off having completed a cycle (ie. a
					// restart delay)
					if ((a.cycle_time_left <= 0)
							&& (a.restart_delay_time_left > 0)) {

						// Decrement the cycle time left
						a.restart_delay_time_left--;

					} else if (a.cycle_time_left <= 0) {
						// Else if this appliance is off

						// There must be active occupants, or the profile must
						// not depend on occupancy for a start event to occur
						if ((iActiveOccupants > 0 && !a.use_profile
								.equals("CUSTOM"))
								|| (a.use_profile.equals("LEVEL"))) {

							// Variable to store the event probability (default
							// to 1)
							double dActivityProbability = 1;

							// For appliances that depend on activity profiles
							// and is not a custom profile ...
							if ((!a.use_profile.equals("LEVEL"))
									&& (!a.use_profile.equals("ACTIVE_OCC"))
									&& (!a.use_profile.equals("CUSTOM"))) {

								// Get the right activity
								ProbabilityModifier pm = getProbabilityModifier(
										activities, weekend, iActiveOccupants,
										a.use_profile);

								// Get the activity statistics for this profile
								// Get the probability for this activity profile
								// for this time step
								dActivityProbability = pm.modifiers[iTenMinuteCount];

							} else if (a.name.equals("ELEC_SPACE_HEATING")) {
								// For electric space heaters ... (excluding
								// night storage heaters)

								// If this appliance is an electric space
								// heater, then activity probability is a
								// function of the month of the year
								dActivityProbability = oMonthlyRelativeTemperatureModifier[month - 1];
							}

							// Check the probability of a start event
							if (Uniform.staticNextDouble() < (a.calibration * dActivityProbability)) {

								// This is a start event
								a.start();

							}
						} else if (a.use_profile.equals("CUSTOM")
								&& a.name.equals("STORAGE_HEATER")) {
							// Custom appliance handler: storage heaters have a
							// simple representation
							// The number of cycles (one per day) set out in the
							// calibration sheet
							// is used to determine whether the storage heater
							// is used

							// This model does not account for the changes in
							// the Economy 7 time
							// It assumes that the time starts at 00:30 each day
							if (iTenMinuteCount == 4) { // ie. 00:30 - 00:40

								// Assume January 14th is the coldest day of the
								// year
								int iMonthOn, iMonthOff;
								Calendar cal = GregorianCalendar.getInstance();
								cal.set(1997, 1, 14);

								// Get the month and day when the storage
								// heaters are turned on and off, using the
								// number of cycles per year
								cal.add(Calendar.DAY_OF_YEAR,
										(int) a.cycles_per_year / 2);
								iMonthOff = cal.get(Calendar.MONTH);
								cal.set(1997, 1, 14);
								cal.add(Calendar.DAY_OF_YEAR,
										(int) -a.cycles_per_year / 2);
								iMonthOn = cal.get(Calendar.MONTH);

								// Declare a probability of use variable
								double prob;

								// If this is a month in which the appliance is
								// turned on of off
								if ((month == iMonthOff) || (month == iMonthOn)) {
									// Pick a 50% chance since this month has
									// only a month of year resolution
									prob = 0.5 / 10; // (since there are 10
														// minutes in this
														// period)
								} else if ((month > iMonthOff)
										&& (month < iMonthOn)) {
									// The appliance is not used in summer
									prob = 0;
								} else {
									// The appliance is used in winter
									prob = 1;
								}

								// Determine if a start event occurs
								if (Uniform.staticNextDouble() <= prob) {

									// This is a start event
									a.start();

								}
							}

						}
					} else {
						// The appliance is on - if the occupants become
						// inactive, switch off the appliance
						if ((iActiveOccupants == 0)
								&& (!a.use_profile.equals("LEVEL"))
								&& (!a.use_profile.equals("ACT_LAUNDRY"))
								&& (!a.use_profile.equals("CUSTOM"))) {

							// Do nothing. The activity will be completed upon
							// the return of the active occupancy.
							// Note that LEVEL means that the appliance use is
							// not related to active occupancy.
							// Note also that laundry appliances do not switch
							// off upon a transition to inactive occupancy.
						} else {

							// Set the power
							a.power = a.GetPowerUsage(a.cycle_time_left);

							// Decrement the cycle time left
							a.cycle_time_left--;

						}
					}

					// Save the power value
					power_vals[time] = (double) a.power;

					// Increment the time
					time++;
				}

				results.put(a, power_vals);
			}
		}

		// Write the data back to the simulation sheet
		ArrayList<String[]> final_vals = new ArrayList<String[]>(
				appliances.size());
		for (Appliance a : appliances) {
			String[] tmp = new String[1440 + 1];
			double[] data = results.get(a);
			tmp[0] = a.name;

			for (int i = 0; i < 1440; i++) {
				tmp[i + 1] = String.valueOf(data[i]);
			}
			final_vals.add(tmp);
		}

		// Save the result to a CSV file
		CSVWriter writer = new CSVWriter(new FileWriter(this.out_file));
		writer.writeAll(final_vals);
		writer.close();

		System.out.println("done.");
	}

	/**
	 * Gets a ProbabilityModifier with a specified characteristics from a list.
	 * 
	 * @param activities
	 *            a List of all ProbabilityModifier objects
	 * @param weekend
	 *            a boolean indicating whether to to find weekend
	 *            <code>true</code> or weekday <code>false</code> modifiers.
	 * @param occupants
	 *            an int giving the number of active occupants that should be in
	 *            the ProbabilityModifier.
	 * @param id
	 *            a String giving an identifier.
	 * @return a ProbabilityModifier matching all of the specified arguments. If
	 *         none found, then returns <code>null</code>
	 */
	private ProbabilityModifier getProbabilityModifier(
			List<ProbabilityModifier> activities, boolean weekend,
			int occupants, String id) {

		for (ProbabilityModifier pm : activities) {
			if (pm.isWeekend == weekend
					&& pm.active_occupant_count == occupants
					&& pm.ID.equals(id)) {
				return (pm);
			}
		}
		return null;
	}

	/**
	 * Randomly assign ownership for appliances.
	 * 
	 * @param apps
	 *            a List of Appliance objects.
	 */
	private void configure_appliances(List<Appliance> apps) {
		for (Appliance a : apps)
			a.assign_ownership();
	}

	/**
	 * Loads the activity statistics from a file.
	 * 
	 * @return a List of ProbabilityModifier objects describing each activity.
	 * 
	 * @throws IOException
	 */
	private List<ProbabilityModifier> loadActivityStatistics()
			throws IOException {
		CSVReader reader = new CSVReader(new FileReader(
				ApplianceModel.activity_file), ',', '\'', 6);
		List<String[]> activities = reader.readAll();

		List<ProbabilityModifier> result = new ArrayList<ProbabilityModifier>();
		for (String[] s : activities) {
			boolean weekend = Boolean.valueOf(s[0]);
			int occupants = Integer.valueOf(s[1]);
			String ID = s[2].toUpperCase();

			ProbabilityModifier stats = new ProbabilityModifier(weekend,
					occupants, ID);
			for (int i = 0; i < 144; i++) {
				stats.modifiers[i] = Double.valueOf(s[i + 3]);
			}

			result.add(stats);
		}

		return (result);

	}

	/**
	 * Load the appliances from a file.
	 * 
	 * @return a List of Appliance objects
	 * 
	 * @throws IOException
	 */
	private List<Appliance> loadAppliances() throws IOException {
		CSVReader reader = new CSVReader(new FileReader(
				ApplianceModel.appliance_file), ',', '\'', 37);
		List<String[]> appliances = reader.readAll();

		List<Appliance> results = new ArrayList<Appliance>();
		for (String[] s : appliances) {
			Appliance a = new Appliance(s[0], s[1], Double.valueOf(s[2]),
					Double.valueOf(s[3]), Integer.valueOf(s[4]),
					Integer.valueOf(s[5]), Double.valueOf(s[6]),
					Integer.valueOf(s[7]), Integer.valueOf(s[8]),
					Double.valueOf(s[9]));
			results.add(a);
		}

		return (results);
	}

	/**
	 * Describes a factor used to modify the probability of an appliance
	 * running. Each modifier contains a vector of doubles which describes the
	 * proportion of households where at least one occupant is engaged in a
	 * particular activity during a particular ten minute period
	 *  
	 * @author jkeirste
	 * 
	 */
	private class ProbabilityModifier {

		// Class variables
		boolean isWeekend;
		int active_occupant_count;
		String ID;
		double[] modifiers = new double[144];

		/**
		 * Create a ProbabilityModified with specified parameters.
		 * 
		 * @param weekend
		 *            does this apply to a weekend? <code>true</code>
		 * @param occupants
		 *            an int giving the number of active occupants
		 * @param id
		 *            a String giving an identifier
		 */
		private ProbabilityModifier(boolean weekend, int occupants, String id) {
			this.isWeekend = weekend;
			this.active_occupant_count = occupants;
			this.ID = id;
		}
	}

	/**
	 * Sets the seed for the random number generator.
	 * 
	 * @param seed
	 *            an int giving the seed
	 */
	public static void setSeed(int seed) {
		// TODO verify that this works for the Normal draws as well
		RandomEngine engine = new MersenneTwister(seed);
		Uniform.staticSetRandomEngine(engine);
	}
}
