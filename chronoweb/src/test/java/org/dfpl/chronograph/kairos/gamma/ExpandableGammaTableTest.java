package org.dfpl.chronograph.kairos.gamma;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.bson.Document;
import org.dfpl.chronograph.kairos.gamma.persistent.db.ExpandableGammaTable;
import org.dfpl.chronograph.kairos.gamma.persistent.db.PathGammaElement;
import org.junit.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class ExpandableGammaTableTest {
	ExpandableGammaTable gammaTable;

	Predicate<Document> sourceTest = new Predicate<Document>() {
		@Override
		public boolean test(Document t) {
			if (t == null)
				return false;
			return true;
		}
	};

	BiPredicate<Document, Document> targetTest = new BiPredicate<Document, Document>() {
		@Override
		public boolean test(Document t, Document u) {
			if (u.getLong("time") < t.getLong("time"))
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
		MongoClient client = MongoClients.create("mongodb://localhost:27017");
		gammaTable = new ExpandableGammaTable(client, "gammaTest");
	}

	@After
	public void tearDown() {

	}

	public void baseTest() {
		gammaTable.clear();
		gammaTable.set("1", "1", new PathGammaElement(List.of("1"), 0l));
		gammaTable.set("2", "2", new PathGammaElement(List.of("2"), 0l));
		gammaTable.set("3", "3", new PathGammaElement(List.of("3"), 0l));
		gammaTable.set("4", "4", new PathGammaElement(List.of("4"), 0l));
		gammaTable.set("5", "5", new PathGammaElement(List.of("5"), 0l));

		gammaTable.append("1", sourceTest, "4", new PathGammaElement(List.of("4"), 1l), targetTest);
		gammaTable.append("2", sourceTest, "3", new PathGammaElement(List.of("3"), 2l), targetTest);
		gammaTable.append("1", sourceTest, "2", new PathGammaElement(List.of("2"), 3l), targetTest);
		gammaTable.append("4", sourceTest, "2", new PathGammaElement(List.of("2"), 4l), targetTest);
		gammaTable.append("4", sourceTest, "3", new PathGammaElement(List.of("3"), 5l), targetTest);
		gammaTable.append("3", sourceTest, "5", new PathGammaElement(List.of("5"), 6l), targetTest);
		gammaTable.append("2", sourceTest, "3", new PathGammaElement(List.of("3"), 7l), targetTest);

		for (int i = 1; i <= 5; i++) {
			System.out.println(gammaTable.getGamma(String.valueOf(i)).toMap(true));
		}

		gammaTable.clear();
	}
}
