package cz.incad.kramerius.services.iterators.timestamps;

public interface Timestamp {
	
	public String getName();
	
	public String getDate();

	public int getIndexed();
	
	public int getUpdated();
	
	public int getBatches();

	public int getWorkers();
	
	public String getUUID();
}
