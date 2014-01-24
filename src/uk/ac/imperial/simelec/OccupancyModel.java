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
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

public class OccupancyModel {

	private int nResidents;
	private boolean weekend;
	private String out_dir;
	
	public static void main(String[] args) {
		
		int residents;
		boolean weekend;
		String output_dir;

		if (args.length == 3) {
			residents = Integer.valueOf(args[0]);
			weekend = args[1].equals("we") ? true : false;
			output_dir = args[2];
		} else {
			System.out.printf(
					"%d arguments detected.  Using default arguments.%n",
					args.length);
			residents = 2;
			weekend = false;
			output_dir = ".";
		}
		
		OccupancyModel model = new OccupancyModel(residents, weekend, output_dir);
		
		try {
			model.RunOccupancySimulation();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//	model.RunOccupancySimulation(2, false, "data/occupancy_output.csv");
		
	}

	public OccupancyModel(int residents, boolean weekend, String dir) {
		setResidents(residents);
		this.weekend = weekend;
		this.out_dir = dir;
	}
	
	public void setResidents(int residents) {
		if (residents>=1 && residents<=5) {
			this.nResidents = residents;
		} else {
			System.out.printf("%d residents specified, only 1 to 5 supported. Defaulting to 2.%n", residents);
			this.nResidents = 2;
		}
	}
	
	/**
	 * Simulate the number of active occupants in a domestic dwelling for a
	 * single day at ten-minute intervals.
	 * 
	 * @param nResidents
	 *            the number of residents in the dwelling (1-5)
	 * @param weekend
	 *            if <code>true</code>, simulate a weekend day. Else simulate a
	 *            weekday
	 * @param outputFile
	 *            a String giving the relative path for the results file
	 * 
	 * @throws IOException
	 */
	public void RunOccupancySimulation() throws IOException {


		// Set up the random number generator
		RandomEngine engine = new MersenneTwister(12345);
		Uniform.staticSetRandomEngine(engine);

		// Ensure the output directory exists
		File dir = new File(this.out_dir);
		if (!dir.isDirectory()) dir.mkdirs();
				
		// Step 2: Determine the active occupancy start state between 00:00 and
		// 00:10

		// Load in the start state data from occ_start_states
		String filename = weekend ? "data/occ_start_states_weekend.csv"
				: "data/occ_start_states_weekday.csv";
		CSVReader reader = new CSVReader(new FileReader(filename), ',', '\'', 2);
		List<String[]> myEntries = reader.readAll();
		float[] vector = new float[myEntries.size()]; // vector = n rows
		int j = 0;
		for (String[] s : myEntries) {
			vector[j] = Float.valueOf(s[nResidents]);
			j++;
		}

		// Draw from the cumulative distribution
		int initialState = draw_from_pdf(vector);

		// Step 3: Determine the active occupancy transitions for each ten
		// minute period of the day.

		// First load in the correct file
		String template = "data/tpm_%d_%s.csv";
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
			vector = new float[row.length - 2];
			for (int i = 2; i < row.length - 2; i++) {
				vector[i - 2] = Float.valueOf(row[i]);
			}

			// Draw for the probability
			int newState = draw_from_pdf(vector);

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

	/**
	 * Draws from a probability density function.
	 * 
	 * @param pdf
	 *            a vector of floats giving the probability density function
	 * @return an integer giving the index of the selected interval
	 */
	private int draw_from_pdf(float[] pdf) {

		// Draw the value
		float rand = (float) Uniform.staticNextDouble();

		// Initialize the loop
		int interval = 0;
		do {
			if (rand <= cumsum(pdf)[interval])
				break;
			interval++;
		} while (interval < pdf.length);

		return (interval);
	}

	/**
	 * Calculates the cumulative sum of a vector
	 * 
	 * @param vector
	 *            a vector of float values
	 * @return a vector of float values
	 */
	private float[] cumsum(float[] vector) {
		float[] empty = new float[vector.length];
		float prev = 0f;
		for (int i = 0; i < empty.length; i++) {
			empty[i] = prev + vector[i];
			prev = empty[i];
		}
		return (empty);
	}

}
