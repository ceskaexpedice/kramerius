package cz.incad.Kramerius.processes.imported.views;

public class FailedImportViewObject {

	private String name;
	private String exception;
	
	public FailedImportViewObject(String name, String exception) {
		super();
		this.name = name;
		this.exception = exception;
	}

	public String getException() {
		return exception;
	}

	public String getName() {
		return name;
	}

	
}
