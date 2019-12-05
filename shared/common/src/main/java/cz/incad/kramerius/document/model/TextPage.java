package cz.incad.kramerius.document.model;


public class TextPage extends AbstractPage {

	public TextPage(String modelName, String uuid) {
		super(modelName, uuid);
	}

    @Override
    public void visitPage(PageVisitor visitor, Object obj) {
        visitor.visit(this, obj);
    }
	
}
