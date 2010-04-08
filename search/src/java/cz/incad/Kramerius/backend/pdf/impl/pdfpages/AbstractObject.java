package cz.incad.Kramerius.backend.pdf.impl.pdfpages;

import cz.incad.kramerius.KrameriusModels;

public abstract class AbstractObject {

	protected KrameriusModels model;
	protected String uuid;
	
	
	public AbstractObject(KrameriusModels model, String uuid) {
		super();
		this.model = model;
		this.uuid = uuid;
	}
	
	public KrameriusModels getModel() {
		return model;
	}
	public String getUuid() {
		return uuid;
	}

	public abstract void debugInformations(StringBuffer buffer, int level);
}
