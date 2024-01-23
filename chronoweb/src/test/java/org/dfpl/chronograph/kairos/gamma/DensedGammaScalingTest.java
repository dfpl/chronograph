package org.dfpl.chronograph.kairos.gamma;

import org.dfpl.chronograph.kairos.gamma.persistent.IntegerGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.DensedGammaTable;
import org.junit.*;


public class DensedGammaScalingTest {
	DensedGammaTable<String, Integer> gammaTable;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		gammaTable = new DensedGammaTable<String, Integer>("d:\\g1", IntegerGammaElement.class, 1000000, 2);
	}

	@After
	public void tearDown() {
	}

	public void baseTest() {
		for(int i = 0 ; i < 1000; i ++) {
			
			if(i == 536) {
				System.out.println();
			}
			gammaTable.set(String.valueOf(i), String.valueOf(i+1), new IntegerGammaElement(i+2));
			System.out.println(gammaTable.get(String.valueOf(i), String.valueOf(i+1)));
		}
	}
}
