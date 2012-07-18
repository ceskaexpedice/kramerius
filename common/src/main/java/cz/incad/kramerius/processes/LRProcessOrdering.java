package cz.incad.kramerius.processes;

public enum LRProcessOrdering {
	
	ID("pid"),
	STATE("status"),
	NAME("name"),
	STARTED("started"),
	LOGINNAME("loginname"),
    PLANNED("planned"),
	BATCHSTATE("batch_status"), 
	FINISHED("finished");
	
	private String ordering;

	private LRProcessOrdering(String ord) {
		this.ordering = ord;
	}

	public String getOrdering() {
		return ordering;
	}
	
	

	
}
