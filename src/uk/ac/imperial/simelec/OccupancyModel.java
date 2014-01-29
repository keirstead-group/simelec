/*    Domestic Active Occupancy Model - Simulation Example Code

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
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Simulates the number of active occupants within a household for a single day
 * at ten-minute intervals.
 * 
 * @author jkeirste
 * 
 */
public class OccupancyModel {

	// Class variables
	private int nResidents;
	private boolean weekend;
	private String out_dir;
	
	// Data variables
	// TODO make these resources
	private static String start_states_weekend = "data/occ_start_states_weekend.csv";
	private static String start_states_weekday = "data/occ_start_states_weekday.csv";
	private static String template = "data/tpm_%d_%s.csv";

	/**
	 * Simulates the number of active occupants within a household for a single
	 * day at ten-minute intervals.
	 * 
	 * @param args
	 *            a String array specifying the number of residents, a
	 *            two-letter code for weekend (<code>we</code>) or weekday (
	 *            <code>wd</code>), and a String giving the output directory for
	 *            the results. An optional fourth argument can be specified, an
	 *            int giving a random number seed. If these are not specified,
	 *            the default is to simulate two occupants for a weekday with
	 *            results saved in the current directory.
	 */
	public static void main(String[] args) {

		int residents;
		boolean weekend;
		String output_dir;

		// Check the inputs
		if (args.length == 3 || args.length==4) {
			residents = Integer.valueOf(args[0]);
			weekend = args[1].equals("we") ? true : false;
			output_dir = args[2];
			if (args.length==4) {
				DiscretePDF.setSeed(Integer.valueOf(args[3]));
			}
		} else {
			System.out.printf(
					"%d arguments detected.  Using default arguments.%n",
					args.length);
			residents = 2;
			weekend = false;
			output_dir = ".";
		}

		// Build the model
		OccupancyModel model = new OccupancyModel(residents, weekend,
				output_dir);

		// Run the model
		try {
			model.run();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create a new OccupancyModel with a specified number of residents,
	 * simulation day, and output directory
	 * 
	 * @param residents
	 *            an int between 1 and 5 giving the number of residents
	 * @param weekend
	 *            a boolean indicating whether to simulate a weekday (
	 *            <code>false</code>) or weekend (<code>true</code>)
	 * @param dir
	 *            a String giving the output directory
	 */
	public OccupancyModel(int residents, boolean weekend, String dir) {
		this.nResidents = SimElec.validateResidents(residents);
		this.weekend = weekend;
		this.out_dir = dir;
	}

	/**
	 * Simulate the number of active occupants in a domestic dwelling for a
	 * single day at ten-minute intervals.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {

		// Ensure the output directory exists
		File dir = new File(this.out_dir);
		if (!dir.isDirectory())
			dir.mkdirs();

		// Step 2: Determine the active occupancy start state between 00:00 and
		// 00:10

		// Load in the start state data from occ_start_states
		String filename = weekend ? start_states_weekend : start_states_weekday; 				
		CSVReader reader = new CSVReader(new FileReader(filename), ',', '\'', 2);
		List<String[]> myEntries = reader.readAll();
		double[] vector = new double[myEntries.size()]; // vector = n rows
		int j = 0;
		for (String[] s : myEntries) {
			vector[j] = Float.valueOf(s[nResidents]);
			j++;
		}

		// Draw from the cumulative distribution
		DiscretePDF pdf = new DiscretePDF(vector);
		int initialState = pdf.getRandomIndex();

		// Step 3: Determine the active occupancy transitions for each ten
		// minute period of the day.

		// First load in the correct file		
		filename = String.format(template, nResidents, weekend ? "weekend"
				: "weekday");
		reader = new CSVReader(new FileReader(filename), ',', '\'', 1);
		myEntries = reader.readAll();

		// Create a list to save the results
		List<String[]> results = new ArrayList<String[]>(144);
		String[] tmp = { "1", String.valueOf(initialState) };
		results.add(tmp);

		// Already have initial state; so iterate over remaining entries
		for (int t = 1; t < 144; t++) {

			// First calculate the row
			int rowID = (t - 1) * 7 + initialState;
			String[] row = myEntries.get(rowID);

			// Grab the vector of transition probabilities
			vector = new double[row.length - 2];
			for (int i = 2; i < row.length - 2; i++) {
				vector[i - 2] = Float.valueOf(row[i]);
			}

			// Draw for the probability
			pdf = new DiscretePDF(vector);
			int newState = pdf.getRandomIndex();

			String[] tmp2 = { String.valueOf(t + 1), String.valueOf(newState) };
			results.add(tmp2);
			initialState = newState;
		}

		// Save the result to a CSV file
		File outputFile = new File(dir, "occupancy_output.csv");
		CSVWriter writer = new CSVWriter(new FileWriter(outputFile));
		writer.writeAll(results);
		writer.close();

	}

}
