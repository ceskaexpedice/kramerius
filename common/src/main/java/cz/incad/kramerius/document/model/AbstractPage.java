package cz.incad.kramerius.document.model;


public abstract class AbstractPage extends AbstractObject {

	
	private String outlineTitle;
	private String pageNumber;
	private String outlineDestination;
	
	public AbstractPage(String modelName, String uuid) {
		super(modelName, uuid);
	}

	public String getOutlineTitle() {
		return outlineTitle;
	}


	public void setOutlineTitle(String outlineTitle) {
		this.outlineTitle = outlineTitle;
	}


	public String getOutlineDestination() {
		return outlineDestination;
	}


	public void setOutlineDestination(String outlineDestination) {
		this.outlineDestination = outlineDestination;
	}

	@Override
	public void debugInformations(StringBuffer buffer, int level) {
		for (int i = 0; i < level; i++) { buffer.append(' '); }
		buffer.append(this.outlineTitle.trim()).append("["+this.modelName+"]").append('\n');
	}

	public String getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(String pageNumber) {
		this.pageNumber = pageNumber;
	}

	
	
	public abstract void visitPage(PageVisitor visitor, Object obj);
	
}
