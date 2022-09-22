package cz.incad.kramerius.services.iterators.timestamps.solr;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.services.iterators.timestamps.Timestamp;

public class SOLRTimestamp implements Timestamp{
	
	private String uuid;
	
	private String name;
	private String date;
	private int indexed;
	private int updated;
	private int batches;
	

	public SOLRTimestamp(String uuid, String name, String date, int indexed, int updated, int batches) {
		super();
		this.uuid = uuid;
		this.name = name;
		this.date = date;
		this.indexed = indexed;
		this.updated = updated;
		this.batches = batches;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDate() {
		return this.date;
	}

	@Override
	public int getIndexed() {
		return this.indexed;
	}

	@Override
	public int getUpdated() {
		return this.updated;
	}

	@Override
	public int getBatches() {
		return this.batches;
	}

	@Override
	public int getWorkers() {
		return 0;
	}

	@Override
	public String getUUID() {
		return this.uuid;
	}
	

	/*
	public Element document(Document doc) {
		Element docElement = doc.createElement("doc");
		
		Element nameElm = doc.createElement("field");
		nameElm.setAttribute("name", "name");
		nameElm.setTextContent(this.name);
		docElement.appendChild(nameElm);
		
		Element uuidElm = doc.createElement("field");
		uuidElm.setAttribute("name", "uid");
		uuidElm.setTextContent(this.uuid);
		docElement.appendChild(uuidElm);

		
		Element batchesElm = doc.createElement("field");
		batchesElm.setAttribute("name", "batches");
		batchesElm.setTextContent(""+this.batches);
		docElement.appendChild(batchesElm);
		
		Element indexedElm = doc.createElement("field");
		indexedElm.setAttribute("name", "indexed");
		indexedElm.setTextContent(""+this.indexed);
		docElement.appendChild(indexedElm);
		
		Element updatedElm = doc.createElement("field");
		updatedElm.setAttribute("name", "updated");
		updatedElm.setTextContent(""+this.updated);
		docElement.appendChild(updatedElm);
	}*/
	
}
