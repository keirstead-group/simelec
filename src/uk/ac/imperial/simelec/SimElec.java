/* Domestic Appliance Model - Simulation Example Code

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/**
 * Simulates electricity demand for a single UK household
 * 
 * @author James Keirstead
 * 
 */
public class SimElec {

	private static String R_DIRECTORY = "R/";
	private int month;
	private int residents;
	private boolean weekend;
	private String output_dir;
	private boolean runLighting = true;
	private boolean runAppliances = true;
	private boolean makeRPlots = true;

	/**
	 * Run the simulation.
	 * 
	 * @param args
	 *            An array of four String objects. The first entry should be a
	 *            numeral indicating the month of the year (1-12), the second a
	 *            numeral giving the number of residents in the household, the
	 *            third a two-letter code indicating whether to simulate a
	 *            weekend ('we') or weekday ('wd'), and a String giving the
	 *            output directory. An optional fifth argument can be specified,
	 *            an int giving a random number seed. If the arguments are not
	 *            specified, the default simulation is for a two-person
	 *            household on a weekday in January with the files written in
	 *            the current directory.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		int month;
		int residents;
		boolean weekend;
		String output_dir;

		if (args.length == 4 || args.length == 5) {
			month = Integer.valueOf(args[0]);
			residents = Integer.valueOf(args[1]);
			weekend = args[2].equals("we") ? true : false;
			output_dir = args[3];

			if (args.length == 5)
				SimElec.setSeed(Integer.valueOf(args[4]));

		} else {
			System.out.printf(
					"%d arguments detected.  Using default arguments.%n",
					args.length);
			month = 1;
			residents = 2;
			weekend = false;
			output_dir = ".";
		}

		System.out.println("Running SimElec...");
		SimElec model = new SimElec(month, residents, weekend, output_dir);
		model.run();
		System.out.printf("Complete.  Results can be found in '%s'%n",
				output_dir);
	}

	/**
	 * Create a new SimElec model with specified arguments.
	 * 
	 * @param month
	 *            an int giving the month of the year (1-12)
	 * @param residents
	 *            an int giving the number of residents in the household (1-5)
	 * @param weekend
	 *            a boolean indicating whether to simulating a weekend (
	 *            <code>true</code>) or weekday (<code>false</code>)
	 * @param output_dir
	 *            a String giving the output directory
	 */
	public SimElec(int month, int residents, boolean weekend, String output_dir) {

		// Set the inputs cleaning as necessary
		this.month = validateMonth(month);
		this.residents = validateResidents(residents);
		this.weekend = weekend;
		this.output_dir = output_dir;
	}

	/**
	 * Validates a specified month.
	 * 
	 * @param month
	 *            an int specifying the month (1-12)
	 * @return the specified month is a valid month, or 1 if not.
	 */
	public static int validateMonth(int month) {
		if (month >= 1 && month <= 12) {
			return (month);
		} else {
			System.out
					.printf("Invalid month %d specified.  Defaulting to 1 (January).%n",
							month);
			return (1);
		}
	}

	/**
	 * Validate the number of residents to simulation.
	 * 
	 * @param residents
	 *            an int giving the number of residents to simulate
	 * 
	 * @return the specified value if >=1 and <=5. If greater than 5, returns 5.
	 *         If less than 1, returns 1.
	 */
	public static int validateResidents(int residents) {
		if (residents >= 1 && residents <= 5) {
			return (residents);
		} else if (residents < 1) {
			System.out
					.printf("%d residents specified, only 1 to 5 supported. Defaulting to 1.%n",
							residents);
			return (1);
		} else {
			System.out
					.printf("%d residents specified, only 1 to 5 supported. Defaulting to 5.%n",
							residents);
			return (5);
		}
	}

	/**
	 * Runs the simulation.
	 * 
	 * @throws IOException
	 */
	public void run() throws IOException {

		OccupancyModel occ = new OccupancyModel(residents, weekend, output_dir);
		occ.run();

		if (runLighting) {
			LightingModel lights = new LightingModel(month, output_dir, occ);
			lights.run();
		}

		if (runAppliances) {
			ApplianceModel appliances = new ApplianceModel(month, weekend,
					output_dir, occ);
			appliances.run();
		}

		if (makeRPlots) {
			try {
				makeRPlots();
			} catch (Exception e) {
				System.out.println("Unable to create R plots.");
				System.out.println(e.getMessage());
			}
		}

	}

	/**
	 * Runs an R script to generate a summary plot
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	private void makeRPlots() throws IOException, InterruptedException {

		String dataDir = Paths.get(output_dir).toFile().getAbsolutePath();
		File outputFile = new File(output_dir, "simelec.png");

		String cmd = String.format("Rscript make-summary-plot.r %s %s",
				dataDir, outputFile.getAbsolutePath());

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd, null, new File(R_DIRECTORY));

			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}		

	}

	/**
	 * Sets the seed for the random number generator used by SimElec.
	 * 
	 * @param seed
	 *            an int giving the seed
	 */
	public static void setSeed(int seed) {
		DiscretePDF.setSeed(seed);
	}

	/**
	 * Set whether to run the Appliance simulation
	 * 
	 * @param run
	 */
	public void setRunAppliances(boolean run) {
		this.runAppliances = run;
	}

	/**
	 * Set whether to run the Lighting simulation
	 * 
	 * @param run
	 */
	public void setRunLighting(boolean run) {
		this.runLighting = run;
	}
	
	/**
	 * Set whether to make the R plots 
	 * 
	 * @param makePlots a boolean indicating if the plots should be made
	 */
	public void setMakeRPlots(boolean makePlots) {
		this.makeRPlots = makePlots;
	}
}
