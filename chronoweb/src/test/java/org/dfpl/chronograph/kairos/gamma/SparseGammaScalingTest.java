package org.dfpl.chronograph.kairos.gamma;

import org.dfpl.chronograph.kairos.gamma.persistent.IntegerGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.SparseGammaTable;
import org.junit.*;

public class SparseGammaScalingTest {
	SparseGammaTable<String, Integer> gammaTable;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		gammaTable = new SparseGammaTable<String, Integer>("d:\\g", IntegerGammaElement.class);
	}

	@After
	public void tearDown() {
		gammaTable.clear();
	}

	@Test
	public void baseTest() {
		for(int i = 0 ; i < 100 ; i++) {
			gammaTable.set(String.valueOf(i), String.valueOf(i), new IntegerGammaElement(3));
			
			for(int j = 0 ; j <= i ; j++) {
				System.out.println(gammaTable.getGamma(String.valueOf(j)).toList());
			}
			

		}
		

		
	}
}
