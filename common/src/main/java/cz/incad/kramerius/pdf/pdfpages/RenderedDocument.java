package cz.incad.kramerius.pdf.pdfpages;

import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.KrameriusModels;

public class RenderedDocument extends AbstractRenderedDocument {
	
	
	public RenderedDocument(String modelName, String uuid) {
		super(modelName, uuid);
		this.outlineItemRoot = new OutlineItem();
	}
}
