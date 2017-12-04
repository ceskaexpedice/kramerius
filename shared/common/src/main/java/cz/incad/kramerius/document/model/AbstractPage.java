package cz.incad.kramerius.document.model;

import org.w3c.dom.Document;

/**
 * Represents one page
 * @author pavels
 */
public abstract class AbstractPage extends AbstractObject {

    private Document biblioMods = null;
    private Document dc = null;
    
	
	private String outlineTitle;
	private String pageNumber;
	private String outlineDestination;
	
	public AbstractPage(String modelName, String uuid) {
		super(modelName, uuid);
	}
	
	/**
	 * Returns outline title
	 * @return outline title
	 */
	public String getOutlineTitle() {
		return outlineTitle;
	}


	/**
	 * Sets outline title
	 * @param outlineTitle Outline title
	 */
	public void setOutlineTitle(String outlineTitle) {
		this.outlineTitle = outlineTitle;
	}


	/**
	 * Returns outline destination
	 * @return outline destination
	 */
	public String getOutlineDestination() {
		return outlineDestination;
	}


	/**
	 * Sets outline destination
	 * @param outlineDestination outlinedestination
	 */
	public void setOutlineDestination(String outlineDestination) {
		this.outlineDestination = outlineDestination;
	}

	@Override
	public void debugInformations(StringBuffer buffer, int level) {
		for (int i = 0; i < level; i++) { buffer.append(' '); }
		buffer.append(this.outlineTitle.trim()).append("["+this.modelName+"]").append('\n');
	}

	/**
	 * Returns page number
	 * @return page number
	 */
	public String getPageNumber() {
		return pageNumber;
	}

	/**
	 * Sets page number
	 * @param pageNumber
	 */
	public void setPageNumber(String pageNumber) {
		this.pageNumber = pageNumber;
	}

	
	
	
	/**
	 * Biblio mods for this page
	 * @return parsed biblio mods metadata
	 */
	public Document getBiblioMods() {
        return biblioMods;
    }

	/**
	 * Sets biblio mods for this page
	 * @param biblioMods parsed biblio mods
	 */
    public void setBiblioMods(Document biblioMods) {
        this.biblioMods = biblioMods;
    }

    /**
     * Returns DC for this page
     * @return parsed DC metadata
     */
    public Document getDc() {
        return dc;
    }

    /**
     * Sets DC for this page
     * @param dc parsed DC metadata
     */
    public void setDc(Document dc) {
        this.dc = dc;
    }

    public abstract void visitPage(PageVisitor visitor, Object obj);
	
}
