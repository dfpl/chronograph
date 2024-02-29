package org.dfpl.chronograph.kairos.gamma.persistent.db;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import io.vertx.core.json.JsonObject;

public class ExpandableGamma implements Gamma<String, Document> {

	private MongoCollection<Document> col;

	public ExpandableGamma(MongoCollection<Document> col) {
		this.col = col;
	}

	@Override
	public Document getElement(String to) {
		Document doc = col.find(new Document("_id", to)).first();
		if (doc == null)
			return null;
		else
			return doc.get("_g", Document.class);
	}

	@Override
	public void setElement(String to, GammaElement<Document> element) {
		col.replaceOne(new Document("_id", to), new Document("_id", to).append("_g", element.getElement()),
				new ReplaceOptions().upsert(true));
	}

	@Override
	public Map<String, Document> toMap(boolean setDefaultToNull) {
		HashMap<String, Document> map = new HashMap<String, Document>();
		for (Document doc : col.find()) {
			map.put(doc.getString("_id"), doc.get("_g", Document.class));
		}
		return map;
	}

	@Override
	public JsonObject toJson(boolean setDefaultToNull) {
		JsonObject obj = new JsonObject();
		for (Document doc : col.find()) {
			obj.put(doc.getString("_id"), doc.get("_g", Document.class));
		}
		return obj;
	}

}
