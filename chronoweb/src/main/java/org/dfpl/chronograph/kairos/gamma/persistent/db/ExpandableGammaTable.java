package org.dfpl.chronograph.kairos.gamma.persistent.db;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.bson.Document;
import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

public class ExpandableGammaTable implements GammaTable<String, Document> {

	public MongoDatabase database;

	public ExpandableGammaTable(MongoClient client, String dbName) {
		this.database = client.getDatabase(dbName);
	}

	@Override
	public Set<String> getSources() {
		HashSet<String> set = new HashSet<String>();
		for (String colName : database.listCollectionNames()) {
			set.add(colName);
		}
		return set;
	}

	@Override
	public Document get(String from, String to) {
		return database.getCollection(from, Document.class).find(new Document("_id", to)).first();
	}

	@Override
	public void set(String from, String to, GammaElement<Document> element) {
		database.getCollection(from, Document.class).replaceOne(new Document("_id", to),
				new Document("_g", element.getElement()), new ReplaceOptions().upsert(true));
	}

	@Override
	public void update(Set<String> sources, String check, Predicate<Document> testCheck, String update,
			GammaElement<Document> newValue, BiPredicate<Document, Document> testUpdate) {
		for (String colName : database.listCollectionNames()) {
			if (!sources.contains(colName))
				continue;
			MongoCollection<Document> col = database.getCollection(colName);
			Document checkDoc = col.find(new Document("_id", check)).first();
			if (checkDoc == null)
				continue;
			Document checkValue = checkDoc.get("_g", Document.class);
			if (!testCheck.test(checkValue))
				continue;
			Document updateDoc = col.find(new Document("_id", update)).first();
			Document updateValue = null;
			if (updateDoc != null)
				updateValue = updateDoc.get("_g", Document.class);

			if (testUpdate.test(updateValue, newValue.getElement())) {
				col.replaceOne(new Document("_id", update),
						new Document("_id", update).append("_g", newValue.getElement()),
						new ReplaceOptions().upsert(true));
			}
		}
	}

	@Override
	public void update(String check, Predicate<Document> testCheck, String update, GammaElement<Document> newValue,
			BiPredicate<Document, Document> testUpdate) {
		for (String colName : database.listCollectionNames()) {
			MongoCollection<Document> col = database.getCollection(colName);
			Document checkDoc = col.find(new Document("_id", check)).first();
			if (checkDoc == null)
				continue;
			Document checkValue = checkDoc.get("_g", Document.class);
			if (!testCheck.test(checkValue))
				continue;
			Document updateDoc = col.find(new Document("_id", update)).first();
			Document updateValue = null;
			if (updateDoc != null)
				updateValue = updateDoc.get("_g", Document.class);

			if (testUpdate.test(updateValue, newValue.getElement())) {
				col.replaceOne(new Document("_id", update),
						new Document("_id", update).append("_g", newValue.getElement()),
						new ReplaceOptions().upsert(true));
			}
		}
	}

	@Override
	public void append(Set<String> sources, String check, Predicate<Document> testCheck, String update,
			GammaElement<Document> newValue, BiPredicate<Document, Document> testUpdate) {
		Document newValueElement = newValue.getElement();
		if (!(newValueElement instanceof Document)) {
			throw new IllegalArgumentException();
		}
		for (String colName : database.listCollectionNames()) {
			if (!sources.contains(colName))
				continue;
			MongoCollection<Document> col = database.getCollection(colName);
			Document checkDoc = col.find(new Document("_id", check)).first();
			if (checkDoc == null)
				continue;
			Document checkValue = checkDoc.get("_g", Document.class);
			if (!testCheck.test(checkValue))
				continue;
			Document checkPath = (Document) checkValue;
			Document updateDoc = col.find(new Document("_id", update)).first();
			Document updateValue = null;
			if (updateDoc != null) {
				updateValue = updateDoc.get("_g", Document.class);
				if (testUpdate.test(updateValue, newValue.getElement())) {
					List<String> checkPathList = checkPath.getList("path", String.class);
					checkPathList.add(update);
					Document newPath = (Document) newValue.getElement();
					checkPath.put("time", newPath.getLong("time"));
					col.replaceOne(new Document("_id", update), new Document("_id", update).append("_g", checkPath),
							new ReplaceOptions().upsert(true));
				}
			} else {
				List<String> checkPathList = checkPath.getList("path", String.class);
				checkPathList.add(update);
				Document newPath = (Document) newValue.getElement();
				checkPath.put("time", newPath.getLong("time"));
				List<Long> checkTimes = checkPath.getList("times", Long.class);
				checkTimes.add(newPath.getLong("time"));
				col.replaceOne(new Document("_id", update), new Document("_id", update).append("_g", checkPath),
						new ReplaceOptions().upsert(true));
			}
		}
	}

	@Override
	public void append(String check, Predicate<Document> testCheck, String update, GammaElement<Document> newValue,
			BiPredicate<Document, Document> testUpdate) {
		Document newValueElement = newValue.getElement();
		if (!(newValueElement instanceof Document)) {
			throw new IllegalArgumentException();
		}
		for (String colName : database.listCollectionNames()) {
			MongoCollection<Document> col = database.getCollection(colName);
			Document checkDoc = col.find(new Document("_id", check)).first();
			if (checkDoc == null)
				continue;
			Document checkValue = checkDoc.get("_g", Document.class);
			if (!testCheck.test(checkValue))
				continue;
			Document checkPath = (Document) checkValue;
			Document updateDoc = col.find(new Document("_id", update)).first();
			Document updateValue = null;
			if (updateDoc != null) {
				updateValue = updateDoc.get("_g", Document.class);
				if (testUpdate.test(updateValue, newValue.getElement())) {
					List<String> checkPathList = checkPath.getList("path", String.class);
					checkPathList.add(update);
					Document newPath = (Document) newValue.getElement();
					checkPath.put("time", newPath.getLong("time"));
					col.replaceOne(new Document("_id", update), new Document("_id", update).append("_g", checkPath),
							new ReplaceOptions().upsert(true));
				}
			} else {
				List<String> checkPathList = checkPath.getList("path", String.class);
				checkPathList.add(update);
				Document newPath = (Document) newValue.getElement();
				checkPath.put("time", newPath.getLong("time"));
				col.replaceOne(new Document("_id", update), new Document("_id", update).append("_g", checkPath),
						new ReplaceOptions().upsert(true));
			}
		}
	}

	@Override
	public Gamma<String, Document> getGamma(String from) {
		return new ExpandableGamma(database.getCollection(from));
	}

	@Override
	public void clear() {
		database.drop();
	}

	@Override
	public void addSource(String source, GammaElement<Document> element) {
		database.createCollection(source);
	}

	@Override
	public void print() {
		for (String s : getSources()) {
			System.out.println(s + " -> " + getGamma(s).toMap(true));
		}
	}

	@Override
	public void invalidate(String id, String key) {

	}

}
