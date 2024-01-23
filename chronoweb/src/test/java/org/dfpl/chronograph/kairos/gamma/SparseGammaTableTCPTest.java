package org.dfpl.chronograph.kairos.gamma;

import java.util.function.BiPredicate;

import org.dfpl.chronograph.kairos.gamma.persistent.BooleanGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.DoubleGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.IntegerGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.SparseGammaTable;
import org.junit.*;

public class SparseGammaTableTCPTest {
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

	public void baseTest() {

		BiPredicate<Integer, Integer> predicate = new BiPredicate<Integer, Integer>() {

			@Override
			public boolean test(Integer t, Integer u) {
				if (u < t)
					return true;

				return false;
			}
		};

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
			System.out.println(gammaTable.getGamma(String.valueOf(i)).toList(true));
		}

		gammaTable.clear();
	}

	public void baseDoubleTest() {

		BiPredicate<Double, Double> predicate = new BiPredicate<Double, Double>() {

			@Override
			public boolean test(Double t, Double u) {
				if (u < t)
					return true;

				return false;
			}
		};

		gammaDTable.set("1", "1", new DoubleGammaElement(3d));
		gammaDTable.set("2", "2", new DoubleGammaElement(3d));
		gammaDTable.set("3", "3", new DoubleGammaElement(3d));
		gammaDTable.set("4", "4", new DoubleGammaElement(3d));
		gammaDTable.set("5", "5", new DoubleGammaElement(3d));

		gammaDTable.setIfExists("1", "4", new DoubleGammaElement(5d), predicate);
		gammaDTable.setIfExists("2", "3", new DoubleGammaElement(8d), predicate);
		gammaDTable.setIfExists("1", "2", new DoubleGammaElement(10d), predicate);
		gammaDTable.setIfExists("4", "2", new DoubleGammaElement(12d), predicate);
		gammaDTable.setIfExists("4", "3", new DoubleGammaElement(13d), predicate);
		gammaDTable.setIfExists("3", "5", new DoubleGammaElement(14d), predicate);
		gammaDTable.setIfExists("2", "3", new DoubleGammaElement(16d), predicate);

		for (int i = 1; i <= 5; i++) {
			System.out.println(gammaDTable.getGamma(String.valueOf(i)).toList(true));
		}

		gammaDTable.clear();
	}
	
	
	@Test
	public void baseBooleanTest() {

		BiPredicate<Boolean, Boolean> predicate = new BiPredicate<Boolean, Boolean>() {

			@Override
			public boolean test(Boolean t, Boolean u) {
				if(t == false && u == true)
					return true;
				return false;
			}
		};

		gammaBTable.set("1", "1", new BooleanGammaElement(true));
		gammaBTable.set("2", "2", new BooleanGammaElement(true));
		gammaBTable.set("3", "3", new BooleanGammaElement(true));
		gammaBTable.set("4", "4", new BooleanGammaElement(true));
		gammaBTable.set("5", "5", new BooleanGammaElement(true));

		gammaBTable.setIfExists("1", "4", new BooleanGammaElement(true), predicate);
		gammaBTable.setIfExists("2", "3", new BooleanGammaElement(true), predicate);
		gammaBTable.setIfExists("1", "2", new BooleanGammaElement(true), predicate);
		gammaBTable.setIfExists("4", "2", new BooleanGammaElement(true), predicate);
		gammaBTable.setIfExists("4", "3", new BooleanGammaElement(true), predicate);
		gammaBTable.setIfExists("3", "5", new BooleanGammaElement(true), predicate);
		gammaBTable.setIfExists("2", "3", new BooleanGammaElement(true), predicate);

		for (int i = 1; i <= 5; i++) {
			System.out.println(gammaBTable.getGamma(String.valueOf(i)).toList(false));
		}

		gammaBTable.clear();
	}
}
