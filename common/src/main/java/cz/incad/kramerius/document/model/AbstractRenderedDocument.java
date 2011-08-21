package cz.incad.kramerius.document.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cz.incad.kramerius.KrameriusModels;

public abstract class AbstractRenderedDocument extends AbstractObject {

	private String uuidTitlePage;
	private String uuidFrontCover;
	private String uuidBackCover;
	private String firstPage; 
	private String uuidMainTitle;
	
	protected OutlineItem outlineItemRoot;
	private List<AbstractPage> pages = new ArrayList<AbstractPage>();

	private String documentTitle;

	public AbstractRenderedDocument(String modelName, String uuid) {
		super(modelName, uuid);
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

	public String getUuidMainTitle() {
		return uuidMainTitle;
	}

	public void setUuidMainTitle(String uuidMainTitle) {
		this.uuidMainTitle = uuidMainTitle;
	}

	public void removePagesTill(String uuid) {
		int pocitadlo = 0;
		while(!this.pages.remove(0).getUuid().equals(uuid)) {
			pocitadlo += 1;
		};
		//System.out.println("Vymazanych stranek "+pocitadlo);
	}
	

	public void divide(OutlineItem leftRoot, OutlineItem rightRoot, String uuid) {
		State state = State.COPY_TO_RIGHT;
		Stack<OutlineItem> stack = new Stack<OutlineItem>();
		stack.push(this.outlineItemRoot);
		while(!stack.isEmpty()) {
			OutlineItem	orig = stack.pop();
			OutlineItem[] children = orig.getChildren();
			if (children.length == 0) {
				if (orig.getDestination().equals(uuid)) {
					state = State.COPY_TO_LEFT; 
				}
				if (state == State.COPY_TO_LEFT) {
					addToCopyRoot(leftRoot, orig);
				} else {
					addToCopyRoot(rightRoot, orig);
				}
			} else {
				for (OutlineItem chOrig : children) {
					stack.push(chOrig);
				}
			}
		}
	}
	
	private void addToCopyRoot(OutlineItem rootCopy, OutlineItem orig) {
		List<String> uuidPath = new ArrayList<String>();
		OutlineItem origParent = orig;
		while(origParent.getParent() != null) { 
			uuidPath.add(0,origParent.getDestination());
			origParent = origParent.getParent(); 
		}
		OutlineItem currentCopy = rootCopy;
		OutlineItem currentOrig = origParent;
		for (String uuid : uuidPath) {
			OutlineItem chCopy = currentCopy.getChild(uuid);
			OutlineItem chOrig = currentOrig.getChild(uuid);
			if (chCopy == null)  {
				chCopy = chOrig.copy();
				currentCopy.addChild(0,chCopy);
				chCopy.setParent(currentCopy);
			}
			currentCopy = chCopy;
			currentOrig = chOrig;
		}
	}

	public void removePages() {
		this.pages.clear();
	}

	public static enum State {
		COPY_TO_LEFT, 
		COPY_TO_RIGHT;	
	}

	public String getUuidFrontCover() {
		return uuidFrontCover;
	}

	public void setUuidFrontCover(String uuidFrontCover) {
		this.uuidFrontCover = uuidFrontCover;
	}

	public String getUuidBackCover() {
		return uuidBackCover;
	}

	public void setUuidBackCover(String uuidBackCover) {
		this.uuidBackCover = uuidBackCover;
	}

	public String getFirstPage() {
		return firstPage;
	}

	public void setFirstPage(String firstPage) {
		this.firstPage = firstPage;
	}

	
	
}
