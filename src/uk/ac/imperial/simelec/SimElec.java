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

import java.io.IOException;

/**
 * Simulates electricity demand for a single UK household
 * 
 * @author jkeirste
 * 
 */
public class SimElec {

	private int month;
	private int nResidents;
	private boolean weekend;
	private String output_dir;

	/**
	 * Run the simulation.
	 * 
	 * @param args
	 *            An array of four String objects. The first entry should be a
	 *            numeral indicating the month of the year (1-12), the second a
	 *            numeral giving the number of residents in the household, the
	 *            third a two-letter code indicating whether to simulate a
	 *            weekend ('we') or weekday ('wd'), and a String giving the
	 *            output directory. If the arguments are not specified, the
	 *            default simulation is for a two-person household on a weekday
	 *            in January with the files written in the current directory.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		int month;
		int residents;
		boolean weekend;
		String output_dir;

		if (args.length == 4) {
			month = Integer.valueOf(args[0]);
			residents = Integer.valueOf(args[1]);
			weekend = args[2].equals("we") ? true : false;
			output_dir = args[3];
		} else {
			System.out.printf(
					"%d arguments detected.  Using default arguments.%n",
					args.length);
			month = 1;
			residents = 2;
			weekend = false;
			output_dir = ".";
		}

		System.out.println("Running simulation...");
		SimElec model = new SimElec(month, residents, weekend, output_dir);
		model.run();
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
		this.setMonth(month);
		this.setResidents(residents);
		this.weekend = weekend;
		this.output_dir = output_dir;
	}

	public void setMonth(int month) {
		if (month >= 1 && month <= 12) {
			this.month = month;
		} else {
			System.out
					.printf("Invalid month %d specified.  Defaulting to 1 (January).%n",
							month);
			this.month = 1;
		}
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
	 * Runs the simulation.
	 * 
	 * @throws IOException 
	 */
	public void run() throws IOException {

		OccupancyModel occ = new OccupancyModel(nResidents, weekend, output_dir);
		occ.RunOccupancySimulation();
		LightingModel lights = new LightingModel(month, output_dir);
		lights.RunLightingSimulation();
		ApplianceModel appliances = new ApplianceModel(month, weekend, output_dir);
		appliances.RunApplianceSimulation();

	}
}
