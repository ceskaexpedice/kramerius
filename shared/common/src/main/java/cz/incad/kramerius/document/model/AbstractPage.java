package cz.incad.kramerius.document.model;

import org.w3c.dom.Document;

/** Abstract page - image page, text page,etc.. */
public abstract class AbstractPage extends AbstractObject {

    /** BIBLIO_MODS */
    private Document biblioMods = null;
    /** DC */
    private Document dc = null;

	private String outlineTitle;
	private String pageNumber;
	private String outlineDestination;

    /** Real page dimension */
    private PageDimension pageDimension = null ;

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
     * Sets page dimension
     * @param pageDimension
     */
    public void setPageDimension(PageDimension pageDimension) {
        this.pageDimension = pageDimension;
    }

    /**
     * Gets page dimension
     * @return
     */
    public PageDimension getPageDimension() {
        return pageDimension;
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
