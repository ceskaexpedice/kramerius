package cz.incad.kramerius.document.model;

import org.w3c.dom.Document;


public abstract class AbstractPage extends AbstractObject {

    private Document biblioMods = null;
    private Document dc = null;
    
	
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

	
	
	
	
	public Document getBiblioMods() {
        return biblioMods;
    }

    public void setBiblioMods(Document biblioMods) {
        this.biblioMods = biblioMods;
    }

    public Document getDc() {
        return dc;
    }

    public void setDc(Document dc) {
        this.dc = dc;
    }

    public abstract void visitPage(PageVisitor visitor, Object obj);
	
}
