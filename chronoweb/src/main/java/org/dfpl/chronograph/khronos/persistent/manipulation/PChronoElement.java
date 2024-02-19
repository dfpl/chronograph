package org.dfpl.chronograph.khronos.persistent.manipulation;

import java.util.HashSet;
import java.util.Set;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.tinkerpop.blueprints.Element;

public class PChronoElement implements Element {

	protected PChronoGraph g;
	protected String id;
	protected MongoCollection<Document> collection;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Document getProperties() {
		try {
			return collection.find(new Document("_id", id)).first().get("properties", Document.class);
		} catch (Exception e) {
			return new Document();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProperty(String key) {
		try {
			return (T) collection.find(new Document("_id", id)).first().get("properties", Document.class).get(key);
		} catch (Exception e) {
			return null;
		}
	}


	@Override
	public Set<String> getPropertyKeys() {
		try {
			return collection.find(new Document("_id", id)).first().get("properties", Document.class).keySet();
		} catch (Exception e) {
			return new HashSet<String>();
		}
	}

	@Override
	public void setProperty(String key, Object value) {
		collection.updateOne(new Document("_id", id), new Document("$set", new Document("properties." + key, value)),
				new UpdateOptions().upsert(true));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(String key) {
		Document doc = collection.find(new Document("_id", id)).first().get("properties", Document.class);
		if (doc == null)
			return null;
		if (doc.containsKey(key)) {
			collection.updateOne(new Document("_id", id), new Document("$unset", new Document("properties." + key, "")),
					new UpdateOptions().upsert(true));
			return (T) doc.get(key);
		} else {
			return null;
		}
	}

	public Document toDocument(boolean includeProperties) {
		Document object = new Document();
		object.put("_id", id);
		if (includeProperties) {
			object.put("properties", getProperties());
		}
		return object;
	}

	public void setProperties(Document properties, boolean isSet) {
		if (!isSet) {
			collection.updateOne(new Document("_id", id), new Document("$set", new Document("properties", properties)),
					new UpdateOptions().upsert(true));
		} else {
			Document existingProperties = collection.find(new Document("_id", id)).first().get("properties",
					Document.class);
			if (existingProperties == null) {
				existingProperties = properties;
			} else {
				for (String key : properties.keySet()) {
					existingProperties.put(key, properties.get(key));
				}
				collection.updateOne(new Document("_id", id),
						new Document("$set", new Document("properties", existingProperties)), new UpdateOptions().upsert(true));
			}
		}
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return id.equals(obj.toString());
	}

}
