package org.dfpl.chronograph.kairos.gamma;

import java.util.function.BiPredicate;

import org.dfpl.chronograph.kairos.gamma.persistent.IntegerGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.SparseGammaTable;
import org.junit.*;

public class SparseGammaTableTCPTest {
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

		BiPredicate<Integer, Integer> predicate = new BiPredicate<Integer, Integer>() {

			@Override
			public boolean test(Integer t, Integer u) {
				if (u < t)
					return true;

				return false;
			}
		};
		
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 5; j++) {
				gammaTable.set(String.valueOf(i), String.valueOf(j), new IntegerGammaElement(Integer.MAX_VALUE));
			}
		}

		gammaTable.set("1", "1", new IntegerGammaElement(3));
		gammaTable.set("2", "2", new IntegerGammaElement(3));
		gammaTable.set("3", "3", new IntegerGammaElement(3));
		gammaTable.set("4", "4", new IntegerGammaElement(3));
		gammaTable.set("5", "5", new IntegerGammaElement(3));

		gammaTable.setIfExists("1", "4", new IntegerGammaElement(5), predicate);
		gammaTable.setIfExists("2", "3", new IntegerGammaElement(8), predicate);
		gammaTable.setIfExists("1", "2", new IntegerGammaElement(10), predicate);
		gammaTable.setIfExists("4", "2", new IntegerGammaElement(12), predicate);
		gammaTable.setIfExists("4", "3", new IntegerGammaElement(13), predicate);
		gammaTable.setIfExists("3", "5", new IntegerGammaElement(14), predicate);
		gammaTable.setIfExists("2", "3", new IntegerGammaElement(16), predicate);

		for (int i = 1; i <= 5; i++) {
			System.out.println(i);
			for (int j = 1; j <= 5; j++) {
				System.out.print(gammaTable.get(String.valueOf(i), String.valueOf(j)) + "\t");
			}
			System.out.println();
		}

	}
}
