package org.dfpl.chronograph.kairos.gamma;

import org.dfpl.chronograph.kairos.gamma.persistent.IntegerGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.PersistentGammaTable;
import org.junit.*;


public class GammaScalingTest {
	PersistentGammaTable<String, Integer> gammaTable;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		gammaTable = new PersistentGammaTable<String, Integer>("d:\\g1", IntegerGammaElement.class);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void baseTest() {
		for(int i = 0 ; i < 100; i ++) {
			gammaTable.set(String.valueOf(i), String.valueOf(i+1), new IntegerGammaElement(i+2));
			System.out.println(gammaTable.get(String.valueOf(i), String.valueOf(i+1)));
		}
	}
}
