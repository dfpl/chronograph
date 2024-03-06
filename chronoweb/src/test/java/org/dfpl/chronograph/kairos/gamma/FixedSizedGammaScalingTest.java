package org.dfpl.chronograph.kairos.gamma;

import org.dfpl.chronograph.kairos.gamma.persistent.file.BooleanGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.file.DoubleGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.file.IntegerGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.junit.*;

public class FixedSizedGammaScalingTest {
	FixedSizedGammaTable<String, Integer> gammaTable;
	FixedSizedGammaTable<String, Double> gammaDTable;
	FixedSizedGammaTable<String, Boolean> gammaBTable;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		gammaTable = new FixedSizedGammaTable<String, Integer>("d:\\g", IntegerGammaElement.class);
		gammaDTable = new FixedSizedGammaTable<String, Double>("d:\\d", DoubleGammaElement.class);
		gammaBTable = new FixedSizedGammaTable<String, Boolean>("d:\\b", BooleanGammaElement.class);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void baseIntegerTest() {
		for (int i = 0; i < 100; i++) {
			gammaTable.set(String.valueOf(i), String.valueOf(i), new IntegerGammaElement(3));

			for (int j = 0; j <= i; j++) {
				System.out.println(gammaTable.getGamma(String.valueOf(j)).toMap(true));
			}

		}
		gammaTable.clear();
	}

	
	public void baseDoubleTest() {
		for (int i = 0; i < 100; i++) {
			gammaDTable.set(String.valueOf(i), String.valueOf(i), new DoubleGammaElement(3d));

			for (int j = 0; j <= i; j++) {
				System.out.println(gammaDTable.getGamma(String.valueOf(j)).toMap(true));
			}

		}
		gammaDTable.clear();
	}
	
	public void baseBooleanTest() {
		for (int i = 0; i < 100; i++) {
			gammaBTable.set(String.valueOf(i), String.valueOf(i), new BooleanGammaElement(true));

			for (int j = 0; j <= i; j++) {
				System.out.println(gammaBTable.getGamma(String.valueOf(j)).toMap(false));
			}

		}
		gammaBTable.clear();
	}
}
