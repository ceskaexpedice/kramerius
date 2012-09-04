package cz.incad.kramerius.document.model;

import cz.incad.kramerius.KrameriusModels;

/**
 * Represents image page
 * @author pavels
 */
public class ImagePage extends AbstractPage {

	public ImagePage(String modelName, String uuid) {
		super(modelName, uuid);
	}

    @Override
    public void visitPage(PageVisitor visitor, Object obj) {
        visitor.visit(this, obj);
    }
	
	
}
