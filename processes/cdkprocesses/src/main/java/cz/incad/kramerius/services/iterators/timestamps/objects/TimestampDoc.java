package cz.incad.kramerius.services.iterators.timestamps.objects;

import java.util.UUID;

import org.json.JSONObject;

public class TimestampDoc {
	
	private String id;
	private TimestampType type = TimestampType.timestamp;
	private int indexed;
	private int updated;
	private int error;
	private String desc;
	
	public TimestampDoc(int indexed, int updated, int error, String desc) {
		super();
		this.id = UUID.randomUUID().toString();
		this.indexed = indexed;
		this.updated = updated;
		this.error = error;
		this.desc = desc;
	}
	
	public String getId() {
		return id;
	}
	
	public TimestampType getType() {
		return type;
	}
	
	public int getUpdated() {
		return updated;
	}
	
	public int getIndexed() {
		return indexed;
	}
	
	public int getError() {
		return error;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public JSONObject toJSON() {
		JSONObject doc = new JSONObject();

		doc.put("id", this.id);
		doc.put("type", this.type.name());
		doc.put("indexed", this.indexed);
		doc.put("updated", this.updated);
		doc.put("error", this.error);
		doc.put("desc", this.desc);
		
		return doc;
	}
}
