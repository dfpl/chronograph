package org.dfpl.chronograph.kairos.gamma.persistent.db;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.bson.Document;
import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

public class ExpandableGammaTable<E> implements GammaTable<String, E> {

	public MongoClient client;
	public MongoDatabase database;

	public ExpandableGammaTable(String connectionString, String dbName) {
		client = MongoClients.create(connectionString);
		database = client.getDatabase(dbName);
	}

	@Override
	public Set<String> getSources() {
		HashSet<String> set = new HashSet<String>();
		for (String colName : database.listCollectionNames()) {
			set.add(colName);
		}
		return set;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get(String from, String to) {
		return (E) database.getCollection(from, Document.class).find(new Document("_id", to)).first();
	}

	@Override
	public void set(String from, String to, GammaElement<E> element) {
		database.getCollection(from, Document.class).replaceOne(new Document("_id", to),
				new Document("_g", element.getElement()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Set<String> sources, String check, Predicate<E> testCheck, String update,
			GammaElement<E> newValue, BiPredicate<E, E> testUpdate) {
		for (String colName : database.listCollectionNames()) {
			if (!sources.contains(colName))
				continue;
			MongoCollection<Document> col = database.getCollection(colName);
			Document checkDoc = col.find(new Document("_id", check)).first();
			if (checkDoc == null)
				continue;
			E checkValue = (E) checkDoc.get("_g");
			if (!testCheck.test(checkValue))
				continue;
			Document updateDoc = col.find(new Document("_id", update)).first();
			E updateValue = null;
			if (updateDoc != null)
				updateValue = (E) updateDoc.get("_g");

			if (testUpdate.test(updateValue, newValue.getElement())) {
				col.replaceOne(new Document("_id", update),
						new Document("_id", update).append("_g", newValue.getElement()),
						new ReplaceOptions().upsert(true));
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(String check, Predicate<E> testCheck, String update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate) {
		for (String colName : database.listCollectionNames()) {
			MongoCollection<Document> col = database.getCollection(colName);
			Document checkDoc = col.find(new Document("_id", check)).first();
			if (checkDoc == null)
				continue;
			E checkValue = (E) checkDoc.get("_g");
			if (!testCheck.test(checkValue))
				continue;
			Document updateDoc = col.find(new Document("_id", update)).first();
			E updateValue = null;
			if (updateDoc != null)
				updateValue = (E) updateDoc.get("_g");

			if (testUpdate.test(updateValue, newValue.getElement())) {
				col.replaceOne(new Document("_id", update),
						new Document("_id", update).append("_g", newValue.getElement()),
						new ReplaceOptions().upsert(true));
			}
		}
	}

	@Override
	public Gamma<String, E> getGamma(String from) {
		return new ExpandableGamma<E>(database.getCollection(from));
	}

	@Override
	public void clear() {
		database.drop();
	}

	@Override
	public void addSource(String source, GammaElement<E> element) {
		database.createCollection(source);
	}

	@Override
	public void print() {
		for (String s : getSources()) {
			System.out.println(s + " -> " + getGamma(s).toMap(true));
		}
	}

}
