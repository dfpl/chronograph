package org.dfpl.chronograph.kairos.gamma.persistent.db;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import io.vertx.core.json.JsonObject;

public class ExpandableGamma<E> implements Gamma<String, E> {

	private MongoCollection<Document> col;

	public ExpandableGamma(MongoCollection<Document> col) {
		this.col = col;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E getElement(String to) {
		Document doc = col.find(new Document("_id", to)).first();
		if (doc == null)
			return null;
		else
			return (E) doc.get("_g");
	}

	@Override
	public void setElement(String to, GammaElement<E> element) {
		col.replaceOne(new Document("_id", to), new Document("_id", to).append("_g", element.getElement()),
				new ReplaceOptions().upsert(true));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, E> toMap(boolean setDefaultToNull) {
		HashMap<String, E> map = new HashMap<String, E>();
		for (Document doc : col.find()) {
			map.put(doc.getString("_id"), (E) doc.get("_g"));
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonObject toJson(boolean setDefaultToNull) {
		JsonObject obj = new JsonObject();
		for (Document doc : col.find()) {
			obj.put(doc.getString("_id"), (E) doc.get("_g"));
		}
		return obj;
	}

}
