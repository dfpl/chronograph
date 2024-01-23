package org.dfpl.chronograph.kairos.gamma;

import org.dfpl.chronograph.kairos.gamma.persistent.BooleanGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.DoubleGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.IntegerGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.SparseGammaTable;
import org.junit.*;

public class SparseGammaScalingTest {
	SparseGammaTable<String, Integer> gammaTable;
	SparseGammaTable<String, Double> gammaDTable;
	SparseGammaTable<String, Boolean> gammaBTable;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		gammaTable = new SparseGammaTable<String, Integer>("d:\\g", IntegerGammaElement.class);
		gammaDTable = new SparseGammaTable<String, Double>("d:\\d", DoubleGammaElement.class);
		gammaBTable = new SparseGammaTable<String, Boolean>("d:\\b", BooleanGammaElement.class);
	}

	@After
	public void tearDown() {

	}

	public void baseIntegerTest() {
		for (int i = 0; i < 100; i++) {
			gammaTable.set(String.valueOf(i), String.valueOf(i), new IntegerGammaElement(3));

			for (int j = 0; j <= i; j++) {
				System.out.println(gammaTable.getGamma(String.valueOf(j)).toList(true));
			}

		}
		gammaTable.clear();
	}

	
	public void baseDoubleTest() {
		for (int i = 0; i < 100; i++) {
			gammaDTable.set(String.valueOf(i), String.valueOf(i), new DoubleGammaElement(3d));

			for (int j = 0; j <= i; j++) {
				System.out.println(gammaDTable.getGamma(String.valueOf(j)).toList(true));
			}

		}
		gammaDTable.clear();
	}
	
	public void baseBooleanTest() {
		for (int i = 0; i < 100; i++) {
			gammaBTable.set(String.valueOf(i), String.valueOf(i), new BooleanGammaElement(true));

			for (int j = 0; j <= i; j++) {
				System.out.println(gammaBTable.getGamma(String.valueOf(j)).toList(false));
			}

		}
		gammaBTable.clear();
	}
}
