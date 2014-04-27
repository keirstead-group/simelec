package uk.ac.imperial.simelec;

/**
 * Describes an electrical load.
 * 
 * @author James Keirstead
 * 
 */
public abstract class Load {

	protected String id;
	protected double[] consumption = new double[1440]; // W

	/**
	 * Converts this Load into a formatted String array for export.
	 * 
	 * @return a String array containing the load's name, and then 1440 entries
	 *         representing the consumption in Watts at each minute interval.
	 */
	public String[] toExportString() {
		return Load.buildExportString(this.id, this.consumption);
	}

	/**
	 * Creates a formatted String array for exporting a Load profile.
	 * 
	 * @param id
	 *            a String giving the Load's name
	 * @param consumption
	 *            an array of 1440 entries giving the consumption values in
	 *            watts
	 * @return a String array containing the load's name, and then 1440 entries
	 *         representing the consumption in Watts at each minute interval.
	 */
	public static String[] buildExportString(String id, double[] consumption) {
		String[] tmp = new String[consumption.length + 1];
		tmp[0] = id;
		for (int i = 0; i < consumption.length; i++)
			tmp[i + 1] = String.valueOf(consumption[i]);
		return tmp;
	}

	/**
	 * Gets the consumption of this Load at a specified time interval
	 * 
	 * @param i
	 *            the time interval, from 1 to 1440.
	 * @return a double giving the consumption in watts
	 */
	public double getConsumption(int i) {
		return consumption[i - 1];
	}

}