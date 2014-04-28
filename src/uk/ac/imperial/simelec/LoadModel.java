package uk.ac.imperial.simelec;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * A template for simulation models of Load objects
 * 
 * @author James Keirstead
 * 
 */
public abstract class LoadModel<V extends Load> {

	// Data fields
	protected int month;
	protected boolean weekend;
	protected String out_dir;
	protected File out_file;
	protected OccupancyModel model;
	protected List<V> loads;
	protected boolean totalOnly = true;
	protected double[] totalConsumption; // W

	/**
	 * Create a new LoadModel specifying various parameters
	 * 
	 * @param month
	 *            the month being simulated
	 * @param weekend
	 *            a boolean if the simulated day is a weekend (else it's a
	 *            weekday)
	 * @param dir
	 *            the output directory
	 * @param file
	 *            the output file name
	 * @param model
	 *            the occupancy model
	 */
	public LoadModel(int month, boolean weekend, String dir, File file,
			OccupancyModel model) {

		this.month = SimElec.validateMonth(month);
		this.weekend = weekend;
		this.out_dir = dir;
		this.out_file = file;
		this.model = model;
	}

	/**
	 * Run the load simulation.
	 * 
	 * @throws IOException
	 */
	public final void run() throws IOException {

		runModel();

		// Write the results to a CSV file
		writeResults(out_file);

	}

	/**
	 * Provides the detailed simulation code for a LoadModel implementation.
	 * 
	 * @throws IOException
	 *             if there's a problem reading input files
	 */
	protected abstract void runModel() throws IOException;

	/**
	 * Writes the results of this LoadModel to a specified File
	 * 
	 * @param file
	 *            the file on which to write the results
	 * @throws IOException
	 *             if there are problems writing the results to file
	 */
	private void writeResults(File file) throws IOException {

		/*
		 *  Calculate the totals.  We do this even if the total flag
		 *  isn't set because someone might ask for the grand total.
		 */
		totalConsumption = new double[1440]; // W
		for (Load l : loads) {
			for (int i = 0; i < totalConsumption.length; i++) {
				totalConsumption[i] += l.getConsumption(i + 1);
			}
		}

		
		// Write the data back to the simulation sheet
		ArrayList<String[]> results;
		if (totalOnly) {
			results = new ArrayList<String[]>(1);
			String label = this.getClass().getSimpleName()
					.replaceFirst("Model", "");
			results.add(Load.buildExportString(label.toUpperCase(),
					totalConsumption));
		} else {
			results = new ArrayList<String[]>(loads.size());
			for (Load a : loads) {
				results.add(a.toExportString());
			}
		}

		// Write the data to a file
		CSVWriter writer = new CSVWriter(new FileWriter(file), ',', '\0');
		writer.writeAll(results);
		writer.close();

	}

	/**
	 * Set if this LoadModel should report the detailed profiles of each
	 * constituent load or only the totals.
	 * 
	 * @param total
	 *            a boolean to indicate if the totals only should be calculated.
	 */
	public void setTotalsOnly(boolean total) {
		this.totalOnly = total;
	}

	/**
	 * Gets the total load profile from this LoadModel.
	 * 
	 * @return an array of length 1440 giving minute-by-minute electricity loads
	 *         (W)
	 */
	public double[] getTotalConsumption() {
		return totalConsumption;
	}

}