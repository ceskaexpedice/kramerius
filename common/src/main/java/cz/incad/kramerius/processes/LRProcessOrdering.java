package cz.incad.kramerius.processes;

public enum LRProcessOrdering {
	
	ID("order by pid"),
	STATE("order by state"),
	NAME("order by name"),
	STARTED("order by started");
	
	private String ordering;

	private LRProcessOrdering(String ord) {
		this.ordering = ord;
	}

	public String getOrdering() {
		return ordering;
	}
	
	

	
}
