/* Domestic Lighting Model - Simulation Example Code

    Copyright (C) 2008 Ian Richardson, Murray Thomson
    CREST (Centre for Renewable Energy Systems Technology),
    Department of Electronic and Electrical Engineering
    Loughborough University, Leicestershire LE11 3TU, UK
    Tel. +44 1509 635326. Email address: I.W.Richardson@lboro.ac.uk

	Java implementation (c) 2014 James Keirstead
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * Simulates electricity demand for lighting in a household at one-minute
 * intervals during the day.
 * 
 * @author James Keirstead
 * 
 */
public class LightingModel extends LoadModel<Bulb> {

	private float mean_irradiance = 60f;
	private float sd_irradiance = 10f;	
	
	// Data files
	private static String irradiance_file = "/data/irradiance.csv";
	private static String bulbs_file = "/data/bulbs.csv";

	/**
	 * Create a LightingModel for a specified month and output directory
	 * 
	 * @param month
	 *            an int giving the month to simulate (1-12)
	 * @param dir
	 *            a String giving the output directory
	 * @param model
	 *            an OccupancyModel to provide data on occupancy within the home
	 */
	public LightingModel(int month, String dir, OccupancyModel model) {
		super(month, true, dir, new File(dir, "lighting_output.csv"), model);		
	}

	/**
	 * 
	 * Simulates the electricity demand for lighting in a household for a single
	 * day at one-minute intervals.
	 * 
	 * @param args
	 *            takes four arguments, plus three options. The first is an int
	 *            giving the number of residents in the home, the second an int
	 *            giving the month to simulate (1-12), the third is a two-letter
	 *            code indicating weekend (<code>we</code>) or weekday (
	 *            <code>wd</code>), and the fourth is a String giving the output
	 *            directory. The optional arguments five and six are floats
	 *            giving the mean and standard deviation of the irradiance in
	 *            W/m2. Optional argument seven is an int giving a random number
	 *            seed.
	 * 
	 *            If these are not specified, the default is to simulate two
	 *            occupants for a weekday with results saved in the current
	 *            directory.
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		int month = 1;
		int residents = 2;
		boolean weekend = false;
		String dir = ".";
		OccupancyModel occ = new OccupancyModel(residents, weekend, dir);

		if (args.length == 4 || args.length == 6 || args.length == 7) {
			residents = Integer.valueOf(args[0]);
			month = Integer.valueOf(args[1]);
			weekend = args[2].equals("we") ? true : false;
			dir = args[3];

			occ = new OccupancyModel(residents, weekend, dir);

			if (args.length == 7)
				LightingModel.setSeed(Integer.valueOf(args[4]));

		} else {
			System.out.printf(
					"%d arguments detected.  Using default arguments.%n",
					args.length);
		}

		LightingModel model = new LightingModel(month, dir, occ);

		if (args.length == 6) {
			model.mean_irradiance = Float.valueOf(args[2]);
			model.sd_irradiance = Float.valueOf(args[3]);
		}

		model.run();
	}

	/**
	 * 
	 * Runs the LightingModel.
	 * 
	 * @throws IOException
	 * 
	 */
	public void runModel() throws IOException {

		// Ensure the output directory exists
		File dir = new File(this.out_dir);
		if (!dir.isDirectory())
			dir.mkdirs();

		// Calculation the irradiance threshold for the house
		float iThreshold = (float) Normal.staticNextDouble(
				this.mean_irradiance, this.sd_irradiance);

		// Calculate the number of bulbs in the household
		loads = getBulbs();
		int[] irradiance = getIrradianceData(month);

		int[] occupancy = model.getOccupancy();

		// Main simulation loop
		// for each bulb in the household
		for (Bulb b : loads) {

			// for each minute of the day
			int t = 0;
			while (t < 1440) {

				// Get the irradiance at that moment
				int ir = irradiance[t];

				// Get the number of active occupants, adjusting for 10 minute
				// intervals
				int occ = occupancy[(int) Math.floor(t / 10f)];

				// if at least one active occupant and insufficient irradiance,
				// turn on a bulb
				boolean low_light = (ir < iThreshold)
						|| (Uniform.staticNextDouble() < 0.05);

				// Get effective occupant to account for sharing
				float effective_occupancy = getEffectiveOccupancy(occ);

				// if bulb switched on:
				if (low_light
						&& Uniform.staticNextDouble() < (effective_occupancy * b.weight)) {
					int duration = getLightDuration();
					for (int j = 0; j < duration; j++) {
						if (t >= 1440)
							break;
						if (occ != 0) {
							b.on(t);
							t++;
						}
					}
				} else {
					t++;
				}
			}
		}

	}

	

	/**
	 * Effective occupancy represents the sharing of light use.
	 * 
	 * Derived from: U.S. Department of Energy, Energy Information
	 * Administration, 1993 Residential Energy Consumption Survey, Mean Annual
	 * Electricity Consumption for Lighting, by Family Income by Number of
	 * Household Members
	 * 
	 * @param occ
	 *            an int giving the active number of household occupants
	 * @return a float giving the effective occupant factor
	 */
	private float getEffectiveOccupancy(int occ) {
		switch (occ) {
		case 0:
			return (0f);
		case 1:
			return (1.000f);
		case 2:
			return (1.528f);
		case 3:
			return (1.694f);
		case 4:
			return (1.983f);
		case 5:
			return (2.094f);
		}

		return (0f);
	}

	/**
	 * Gets the bulbs in the household based on 100 sample bulb configurations.
	 * The data has been generated stochastically, based upon statistics
	 * available from: (1) The Lighting Association, In Home Lighting Audit
	 * Report, Domestic Lighting Report 2008. (2) Market Transformation
	 * Programme, Assumptions for energy scenarios in the domestic lighting
	 * sector, version 4.0, 2008.
	 * 
	 * @return a List of Bulb objects
	 * @throws IOException
	 */
	private List<Bulb> getBulbs() throws IOException {

		// Load in the raw data
		InputStream is = this.getClass().getResourceAsStream(bulbs_file);
		CSVReader reader = new CSVReader(new InputStreamReader(is), ',', '\'',
				10);
		List<String[]> myEntries = reader.readAll();
		reader.close();

		// Choose a random house
		int house = Uniform.staticNextIntFromTo(0, myEntries.size() - 1);

		// Create a set of bulbs corresponding to that line.
		String[] data = myEntries.get(house);
		int nBulbs = Integer.valueOf(data[1]);
		List<Bulb> bulbs = new ArrayList<Bulb>(nBulbs);

		// Note that the input data file is a ragged array. To get CSVReader
		// to work, it has been padded with 0 values for bulb ratings. However
		// the first column "nBulbs" specifies how many valid values to read.
		for (int i = 0; i < nBulbs; i++) {

			// Read in the power rating of the bulb
			int rating = Integer.valueOf(data[2 + i]);
			Bulb b = new Bulb(i, rating);
			bulbs.add(b);
		}

		return (bulbs);

	}

	/**
	 * Gets irradiance data
	 * 
	 * @param month
	 *            an integer giving the month of the year (1-12)
	 * @return an array of irradiance values in W/m2 at one-minute intervals
	 * @throws IOException
	 */
	private int[] getIrradianceData(int month) throws IOException {

		InputStream is = this.getClass().getResourceAsStream(irradiance_file);
		CSVReader reader = new CSVReader(new InputStreamReader(is), ',', '\'',
				8);
		List<String[]> myEntries = reader.readAll();
		reader.close();

		int[] answer = new int[myEntries.size()];
		for (int i = 0; i < myEntries.size(); i++) {
			answer[i] = Integer.valueOf(myEntries.get(i)[1 + month]);
		}
		return (answer);

	}

	/**
	 * 
	 * Gets the duration of a lighting event (i.e. how long will a bulb will
	 * stay on for once switched on?).
	 * 
	 * Source: M. Stokes, M. Rylatt, K. Lomas, A simple model of domestic
	 * lighting demand, Energy and Buildings 36 (2004) 103-116
	 * 
	 * @return
	 */
	private int getLightDuration() {
		int interval = Uniform.staticNextIntFromTo(0, 8);
		int low = 0;
		int up = 0;
		switch (interval) {
		case 0:
			low = 1;
			up = 1;
			break;
		case 1:
			low = 2;
			up = 2;
			break;
		case 2:
			low = 3;
			up = 4;
			break;
		case 3:
			low = 5;
			up = 8;
			break;
		case 4:
			low = 9;
			up = 16;
			break;
		case 5:
			low = 17;
			up = 27;
			break;
		case 6:
			low = 28;
			up = 49;
			break;
		case 7:
			low = 50;
			up = 91;
			break;
		case 8:
			low = 92;
			up = 259;
			break;
		}

		float rnd = (float) Uniform.staticNextDouble();

		return (int) (low + rnd * (up - low));
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
