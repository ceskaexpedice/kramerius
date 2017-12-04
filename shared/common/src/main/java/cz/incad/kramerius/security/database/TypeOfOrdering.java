package cz.incad.kramerius.security.database;

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
