package cz.incad.kramerius.pdf.model;

import com.qbizm.kramerius.ext.ontheflypdf.edi.Element;

public interface Outlineable<T extends Element> {

	T getWrapped();
	
	String getDest();
}
