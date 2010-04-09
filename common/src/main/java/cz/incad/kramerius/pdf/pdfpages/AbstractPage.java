package cz.incad.kramerius.pdf.pdfpages;

import cz.incad.kramerius.KrameriusModels;

public abstract class AbstractPage extends AbstractObject {

	
	private String outlineTitle;
	private String outlineDestination;
	
	public AbstractPage(KrameriusModels model, String uuid) {
		super(model, uuid);
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
		buffer.append(this.outlineTitle.trim()).append("["+this.model+"]").append('\n');
	}

	
	
	
}
