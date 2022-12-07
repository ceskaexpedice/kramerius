package cz.incad.kramerius.timestamps.impl;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONObject;

import cz.incad.kramerius.timestamps.Timestamp;

public class SolrTimestamp implements Timestamp{

	public static final String DEFAULT_TYPE = "update";
	
	private static final String WORKERS_KEY = "workers";
	private static final String BATCHES_KEY = "batches";
	private static final String UPDATED_KEY = "updated";
	private static final String INDEXED_KEY = "indexed";
	private static final String DATE_KEY = "date";
	private static final String NAME_KEY = "name";
	private static final String ID_KEY = "id";
	private static final String TYPE_ID = "type";

	private String id;
	private String name;
	private String type;
	private Date date;
	private long indexed;
	private long updated;
	private long batches;
	private long workers = 1;
	
	
	public SolrTimestamp(String name, String type, Date date, long indexed, long updated, long batches, long workers) {
		super();
		this.name = name;
		this.id = this.name+"_"+date.getTime();
		this.date = date;
		this.indexed = indexed;
		this.updated = updated;
		this.batches = batches;
		this.workers = workers;
		this.type = type;
	}

	public SolrTimestamp(String id, String name, String type, Date date, long indexed, long updated, long batches, long workers) {
		super();
		this.id = id;
		this.name = name;
		this.date = date;
		this.indexed = indexed;
		this.updated = updated;
		this.batches = batches;
		this.workers = workers;
		this.type = type;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Date getDate() {
		return this.date;
	}

	@Override
	public long getIndexed() {
		return this.indexed;
	}

	@Override
	public long getUpdated() {
		return this.updated;
	}

	@Override
	public long getBatches() {
		return this.batches;
	}

	@Override
	public long getWorkers() {
		return this.workers;
	}

	@Override
	public String getId() {
		return this.id;
	}
	
	
	@Override
	public String getType() {
		return this.type;
	}
	
    @Override
	public void updateDate(Date date) {
    	this.date = date;
	}
    
    
    
	@Override
	public void updateName(String name) {
		this.name = name;
	}

	@Override
	public JSONObject toJSONObject() {

    	JSONObject jsonObject = new JSONObject();
    	jsonObject.put(ID_KEY, this.id);
    	jsonObject.put(NAME_KEY, this.name);
    	jsonObject.put(DATE_KEY, DateTimeFormatter.ISO_INSTANT.format(this.date.toInstant()));
    	jsonObject.put(INDEXED_KEY, this.indexed);
    	jsonObject.put(UPDATED_KEY, this.updated);
    	jsonObject.put(BATCHES_KEY, this.batches);
    	jsonObject.put(WORKERS_KEY, this.workers);
    	jsonObject.put(TYPE_ID, this.type);
    	
    	return jsonObject;
	}	

	public SolrInputDocument toSolrDoc() {
        SolrInputDocument sdoc = new SolrInputDocument();
        sdoc.addField(ID_KEY, this.id);
        sdoc.addField(NAME_KEY, this.name);
        sdoc.addField(DATE_KEY, this.date);
        sdoc.addField(INDEXED_KEY, this.indexed);
        sdoc.addField(UPDATED_KEY, this.updated);
        sdoc.addField(BATCHES_KEY, this.batches);
        sdoc.addField(WORKERS_KEY, this.workers);
        sdoc.addField(TYPE_ID, this.type);
        
        return sdoc;
    }

	public static Timestamp fromSolrDoc(SolrDocument doc) {
		String id = (String) doc.getFieldValue(ID_KEY);
		String name = (String) doc.getFieldValue(NAME_KEY);
		Date dt = (Date) doc.getFieldValue(DATE_KEY);
		Long indexed = (Long) doc.getFieldValue(INDEXED_KEY);
		Long updated = (Long) doc.getFieldValue(UPDATED_KEY);
		Long batches = (Long) doc.getFieldValue(BATCHES_KEY);
		Long workers = (Long) doc.getFieldValue(WORKERS_KEY);
		String type = (String) doc.getFieldValue(TYPE_ID);

		return new SolrTimestamp(id,name, type, dt, indexed, updated, batches, workers);
	}

	public static Timestamp fromJSONDoc(String name, JSONObject doc) {
		String type = doc.has(TYPE_ID) ? doc.getString(TYPE_ID) : DEFAULT_TYPE;
		String id = doc.has(ID_KEY) ? doc.getString(ID_KEY) : name+"_"+System.currentTimeMillis();
		Date date = null;
		if (doc.has(DATE_KEY)) {
			OffsetDateTime parsed = OffsetDateTime.parse(doc.getString(DATE_KEY));
			Instant instant = parsed.toInstant();
			date = Date.from(instant);
		}

		long indexed = doc.has(INDEXED_KEY) ? doc.getLong(INDEXED_KEY) : -1;
		long updated = doc.has(UPDATED_KEY) ? doc.getLong(UPDATED_KEY) : -1;
		long batches = doc.has(BATCHES_KEY) ? doc.getLong(BATCHES_KEY) : -1;
		long workers = doc.has(WORKERS_KEY) ? doc.getLong(WORKERS_KEY) : -1;
		
		return new SolrTimestamp(id, name, type, date, indexed, updated, batches, workers);
 	}

}
