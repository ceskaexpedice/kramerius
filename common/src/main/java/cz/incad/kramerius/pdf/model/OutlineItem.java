package cz.incad.kramerius.pdf.model;

import java.util.ArrayList;
import java.util.List;

import com.qbizm.kramerius.ext.ontheflypdf.edi.Element;

public class OutlineItem extends Element {

	private String title;
	private String destination;
	
	private List<OutlineItem> children = new ArrayList<OutlineItem>();

	public void addItem(OutlineItem item) {
		children.add(item);
	}
	
	public void removeItem(OutlineItem item) {
		children.remove(item);
	}
	
	public OutlineItem[] getItems() {
		return (OutlineItem[]) this.children.toArray(new OutlineItem[this.children.size()]);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	
	
}
