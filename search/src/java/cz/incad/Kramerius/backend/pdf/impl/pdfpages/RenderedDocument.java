package cz.incad.Kramerius.backend.pdf.impl.pdfpages;

import java.util.ArrayList;
import java.util.List;

import cz.incad.kramerius.KrameriusModels;

public class RenderedDocument extends AbstractRenderedDocument {
	
	private List<Part> parts = new ArrayList<Part>();
	
	public RenderedDocument(KrameriusModels model, String uuid) {
		super(model, uuid);
		this.outlineItemRoot = new OutlineItem();
	}

	
	
}
