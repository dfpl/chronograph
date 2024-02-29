package org.dfpl.chronograph.kairos.gamma;

import org.dfpl.chronograph.kairos.gamma.persistent.file.IntegerGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.junit.*;

public class FixedSizedGammaTableTest {
	FixedSizedGammaTable<String, Integer> gammaTable;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		gammaTable = new FixedSizedGammaTable<String, Integer>("d:\\g", IntegerGammaElement.class);
	}

	@After
	public void tearDown() {
	}

	public void baseTest() {
		gammaTable.set("1", "2", new IntegerGammaElement(3));
		gammaTable.set("1", "4", new IntegerGammaElement(5));
		gammaTable.set("2", "8", new IntegerGammaElement(7));

		System.out.println(gammaTable.get("1", "3"));
		System.out.println(gammaTable.get("1", "4"));
		System.out.println(gammaTable.get("2", "8"));

		gammaTable.set("9", "1", new IntegerGammaElement(9));
		gammaTable.set("10", "3", new IntegerGammaElement(11));

		System.out.println(gammaTable.get("1", "2"));
		System.out.println(gammaTable.get("1", "4"));
		System.out.println(gammaTable.get("2", "8"));
		System.out.println(gammaTable.get("9", "1"));
		System.out.println(gammaTable.get("10", "3"));
	}
}
