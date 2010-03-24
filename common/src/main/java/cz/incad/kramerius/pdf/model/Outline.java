package cz.incad.kramerius.pdf.model;

import java.util.ArrayList;
import java.util.List;


import com.qbizm.kramerius.ext.ontheflypdf.edi.Element;

/**
 * Represents PDF outline
 * @author pavels
 */
public class Outline extends Element {
	
	private OutlineItem root = new OutlineItem();

	public OutlineItem getRoot() {
		return root;
	}

	public void setRoot(OutlineItem root) {
		this.root = root;
	}
	
}
