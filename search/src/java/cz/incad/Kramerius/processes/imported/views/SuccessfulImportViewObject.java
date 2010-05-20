package cz.incad.Kramerius.processes.imported.views;

public class SuccessfulImportViewObject {
	
	private String name;
	private String data;
	private String href;
	
	public SuccessfulImportViewObject(String data, String name, String href) {
		super();
		this.data = data;
		this.href = href;
		this.name = name;
	}

	public String getData() {
		return data;
	}


	public String getHref() {
		return href;
	}

	public String getName() {
		return name;
	}

	
}
