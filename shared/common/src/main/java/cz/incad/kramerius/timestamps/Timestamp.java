package cz.incad.kramerius.timestamps;

import java.util.Date;

import org.apache.solr.common.SolrInputDocument;
import org.json.JSONObject;

public interface Timestamp {
	
	public String getType();
	
	public String getName();
	
	public Date getDate();
	
	public long getIndexed();
	
	public long getUpdated();
	
	public long getBatches();

	public long getWorkers();
	
	public String getId();

    public SolrInputDocument toSolrDoc();
    
    public JSONObject toJSONObject();
    
    public void updateDate(Date date);

    public void updateName(String name);
}
