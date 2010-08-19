package cz.incad.kramerius.processes;

public enum LRProcessOrdering {
	
	ID("order by pid"),
	STATE("order by status"),
	NAME("order by name"),
	STARTED("order by started"),
    PLANNED("order by planned");
	
	private String ordering;

	private LRProcessOrdering(String ord) {
		this.ordering = ord;
	}

	public String getOrdering() {
		return ordering;
	}
	
	

	
}
