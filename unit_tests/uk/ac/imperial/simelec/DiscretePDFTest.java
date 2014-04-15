package uk.ac.imperial.simelec;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DiscretePDFTest {

	double[] values = {-1,0,1,2,3,4,5};
	double eps = 0.00001;
	DiscretePDF pdf;
	
	@Before
	public void setUp() throws Exception {
		pdf = new DiscretePDF(values);		
	}

	@After
	public void tearDown() throws Exception {
		pdf = null;
	}

	@Test
	public void testDiscretePDF() {
		double[] tmp = pdf.getValues();
		for (int i = 0; i < tmp.length; i++) {
			assertTrue(tmp[i]>=0);
		}
	}

	@Test
	public void testNormalize() {
		pdf.normalize();
		double[] tmp = pdf.getValues();
		double sum = 0;
		for (int i=0; i<tmp.length; i++) {
			sum+= tmp[i];
		}
		assertEquals(sum, 1, eps);
	}

	@Test
	public void testGetCDF() {
		pdf.normalize();
		double[] tmp = pdf.getCDF();
		assertEquals(tmp[tmp.length - 1], 1, eps);
	}

	@Test
	public void testGetRandomIndex() {
		int max = 0;
		int min = values.length;
		
		double[] tmp = pdf.getValues();
		for (int i = 0; i<1000; i++) {
			int draw = pdf.getRandomIndex();
			if (draw<min) min = draw;
			if (draw>max) max = draw;
		}
		
		// Get expected max and min indices
		int maxIndex = 0;
		int minIndex = tmp.length;
		double prev =0;
		tmp = pdf.getCDF();
		for (int i = 0; i<tmp.length; i++) {
			if (tmp[i]==0 && prev==0) minIndex=i + 1;
			if (tmp[i]==1) maxIndex = i;
		}
		assertEquals(min, minIndex);
		assertEquals(max, maxIndex);
	}

}
