package uk.ac.imperial.simelec;

/**
 * Describes a light bulb
 * 
 * @author admin
 * 
 */
public class Bulb {

	protected int id;
	protected int rating; // in Watts
	protected float weight; // dimensionless
	private float[] consumption = new float[1440]; // W

	public Bulb(int id, int rating, float weight) {
		this.id = id;
		this.rating = rating;
		this.weight = weight;
	}

	public String toString() {
		return (String.format("%d: %d W, cf = %.4f", this.id, this.rating,
				this.weight));
	}

	public void on(int j) {
		this.consumption[j] = this.rating;
	}

	public String[] to_export_string() {
		String[] tmp = new String[consumption.length + 3];
		tmp[0] = String.valueOf(this.id);
		tmp[1] = String.valueOf(this.rating);
		tmp[2] = String.valueOf(this.weight);
		for (int i = 0; i < consumption.length; i++)
			tmp[i + 3] = String.valueOf(consumption[i]);
		return tmp;
	}
}
