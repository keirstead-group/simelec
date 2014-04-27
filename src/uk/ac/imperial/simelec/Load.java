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
	 * @return a String array containing the load's name, and then 1440
	 *         entries representing the consumption in Watts at each minute
	 *         interval.
	 */
	public String[] toExportString() {
		String[] tmp = new String[consumption.length + 1];
		tmp[0] = this.id;
		for (int i = 0; i < consumption.length; i++)
			tmp[i + 1] = String.valueOf(consumption[i]);
		return tmp;	
	}

}