package org.dfpl.chronograph.kairos.gamma;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.dfpl.chronograph.kairos.gamma.persistent.file.BooleanGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.file.DoubleGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.file.IntegerGammaElement;
import org.dfpl.chronograph.kairos.gamma.persistent.file.FixedSizedGammaTable;
import org.junit.*;

public class FixedSizedGammaTableTCPTest {
	FixedSizedGammaTable<String, Integer> gammaTable;
	FixedSizedGammaTable<String, Double> gammaDTable;
	FixedSizedGammaTable<String, Boolean> gammaBTable;

	BiPredicate<Double, Double> predicate = new BiPredicate<Double, Double>() {

		@Override
		public boolean test(Double t, Double u) {
			if (u < t)
				return true;

			return false;
		}
	};

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

	public void baseTest() {

		Predicate<Integer> predicate = new Predicate<Integer>() {
			@Override
			public boolean test(Integer t) {
				if (t.intValue() == 2139062143)
					return false;
				return true;
			}
		};

		BiPredicate<Integer, Integer> biPredicate = new BiPredicate<Integer, Integer>() {

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

		gammaTable.update("1", predicate, "4", new IntegerGammaElement(5), biPredicate);
		gammaTable.update("2", predicate, "3", new IntegerGammaElement(8), biPredicate);
		gammaTable.update("1", predicate, "2", new IntegerGammaElement(10), biPredicate);
		gammaTable.update("4", predicate, "2", new IntegerGammaElement(12), biPredicate);
		gammaTable.update("4", predicate, "3", new IntegerGammaElement(13), biPredicate);
		gammaTable.update("3", predicate, "5", new IntegerGammaElement(14), biPredicate);
		gammaTable.update("2", predicate, "3", new IntegerGammaElement(16), biPredicate);

		for (int i = 1; i <= 5; i++) {
			System.out.println(gammaTable.getGamma(String.valueOf(i)).toMap(true));
		}

		gammaTable.clear();
	}

	public void baseDoubleTest() {

		Predicate<Double> predicate = new Predicate<Double>() {
			@Override
			public boolean test(Double t) {
				if (t.doubleValue() == 1.3824172084878715E306)
					return false;
				return true;
			}
		};

		BiPredicate<Double, Double> biPredicate = new BiPredicate<Double, Double>() {

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

		gammaDTable.update("1", predicate, "4", new DoubleGammaElement(5d), biPredicate);
		gammaDTable.update("2", predicate, "3", new DoubleGammaElement(8d), biPredicate);
		gammaDTable.update("1", predicate, "2", new DoubleGammaElement(10d), biPredicate);
		gammaDTable.update("4", predicate, "2", new DoubleGammaElement(12d), biPredicate);
		gammaDTable.update("4", predicate, "3", new DoubleGammaElement(13d), biPredicate);
		gammaDTable.update("3", predicate, "5", new DoubleGammaElement(14d), biPredicate);
		gammaDTable.update("2", predicate, "3", new DoubleGammaElement(16d), biPredicate);

		for (int i = 1; i <= 5; i++) {
			System.out.println(gammaDTable.getGamma(String.valueOf(i)).toMap(true));
		}

		gammaDTable.clear();
	}

	public void baseBooleanTest() {

		Predicate<Boolean> predicate = new Predicate<Boolean>() {
			@Override
			public boolean test(Boolean t) {
				return t.booleanValue();
			}
		};

		BiPredicate<Boolean, Boolean> biPredicate = new BiPredicate<Boolean, Boolean>() {

			@Override
			public boolean test(Boolean t, Boolean u) {
				if (t == false && u == true)
					return true;
				return false;
			}
		};

		gammaBTable.set("1", "1", new BooleanGammaElement(true));
		gammaBTable.set("2", "2", new BooleanGammaElement(true));
		gammaBTable.set("3", "3", new BooleanGammaElement(true));
		gammaBTable.set("4", "4", new BooleanGammaElement(true));
		gammaBTable.set("5", "5", new BooleanGammaElement(true));

		gammaBTable.update("1", predicate, "4", new BooleanGammaElement(true), biPredicate);
		gammaBTable.update("2", predicate, "3", new BooleanGammaElement(true), biPredicate);
		gammaBTable.update("1", predicate, "2", new BooleanGammaElement(true), biPredicate);
		gammaBTable.update("4", predicate, "2", new BooleanGammaElement(true), biPredicate);
		gammaBTable.update("4", predicate, "3", new BooleanGammaElement(true), biPredicate);
		gammaBTable.update("3", predicate, "5", new BooleanGammaElement(true), biPredicate);
		gammaBTable.update("2", predicate, "3", new BooleanGammaElement(true), biPredicate);

		for (int i = 1; i <= 5; i++) {
			System.out.println(gammaBTable.getGamma(String.valueOf(i)).toMap(false));
		}

		gammaBTable.clear();
	}
}
