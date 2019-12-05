package cz.incad.kramerius.document.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class OutlineItem {
	
	private OutlineItem parent;
	private List<OutlineItem> children = new ArrayList<OutlineItem>();
	
	private String title;
	private String destination;
	private int level;
	
	public void addChild(OutlineItem item) {
		this.children.add(item);
	}
	
	public void removeChild(OutlineItem item) {
		this.children.remove(item);
	}
	
	public OutlineItem[] getChildren() {
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

	public OutlineItem getParent() {
		return parent;
	}

	public void setParent(OutlineItem parent) {
		this.parent = parent;
	}

	
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void debugInformations(StringBuffer buffer, int level) {
		for (int i = 0; i < level; i++) { buffer.append(' '); }
		buffer.append('\'').append(this.title).append('\'').append(this.destination).append('\n');
		for (OutlineItem item : this.children) {
			item.debugInformations(buffer, level+1);
		}
	}	
	
	public OutlineItem copy() {
		OutlineItem item = new OutlineItem();
		item.setDestination(this.destination);
		item.setLevel(this.level);
		//item.setParent(item)
		item.setTitle(this.title);
		return item;
	}
	
	
	
	public boolean removeTill(String uuid) {
		for (int i = 0; i < this.children.size(); i++) {
			//OutlineItem item = 
		}
		return false;
	}

	public OutlineItem getChild(String uuid) {
		for (OutlineItem itm : this.children) {
			if (itm.getDestination().equals(uuid)) {
				return itm;
			}
		}
		return null;
		
	}

	public void addChild(int i, OutlineItem chCopy) {
		this.children.add(i, chCopy);
	}
	
}
