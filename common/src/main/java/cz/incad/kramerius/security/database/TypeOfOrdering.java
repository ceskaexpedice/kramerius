package cz.incad.kramerius.processes;

public enum TypeOfOrdering {

	ASC("ASC"),
	DESC("DESC");
	
	private String sql;

	private TypeOfOrdering(String s) {
		this.sql = s;
	}

	public String getTypeOfOrdering() {
		return this.sql;
	}
}
