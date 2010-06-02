package cz.incad.kramerius.pdf.pdfpages;

import cz.incad.kramerius.KrameriusModels;

public class TextPage extends AbstractPage {

	public TextPage(KrameriusModels model, String uuid) {
		super(model, uuid);
		if (model == null) throw new IllegalArgumentException("neplatny argument "+model);
	}
}
