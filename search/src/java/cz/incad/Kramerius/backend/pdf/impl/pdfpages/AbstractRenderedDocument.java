package cz.incad.Kramerius.backend.pdf.impl.pdfpages;

import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.KrameriusModels;

public abstract class AbstractRenderedDocument extends AbstractObject {

	private String uuidTitlePage;
	protected OutlineItem outlineItemRoot;
	private List<AbstractPage> pages = new ArrayList<AbstractPage>();
	private String documentTitle;

	public AbstractRenderedDocument(KrameriusModels model, String uuid) {
		super(model, uuid);
		// TODO Auto-generated constructor stub
	}

	public String getUuidTitlePage() {
		return uuidTitlePage;
	}

	public void setUuidTitlePage(String uuidTitlePage) {
		this.uuidTitlePage = uuidTitlePage;
	}

	public OutlineItem getOutlineItemRoot() {
		return outlineItemRoot;
	}

	public void setOutlineItemRoot(OutlineItem outlineItemRoot) {
		this.outlineItemRoot = outlineItemRoot;
	}

	public List<AbstractPage> getPages() {
		return pages;
	}

	public void setPages(List<AbstractPage> pages) {
		this.pages = pages;
	}

	public void addPage(AbstractPage page) {
		this.pages.add(page);
	}

	public void removePage(AbstractPage page) {
		this.pages.remove(page);
	}

	@Override
	public void debugInformations(StringBuffer buffer, int level) {
		getOutlineItemRoot().debugInformations(buffer, level+1);
		for (AbstractPage page : this.pages) {
			page.debugInformations(buffer, level+1);
		}
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	
}
