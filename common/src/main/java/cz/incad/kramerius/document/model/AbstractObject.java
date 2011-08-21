package cz.incad.kramerius.document.model;


public abstract class AbstractObject {

	protected String modelName;
	protected String uuid;
	
	
	public AbstractObject(String modelName, String uuid) {
		super();
		this.modelName = modelName;
		this.uuid = uuid;
	}
	
	public String getModel() {
		return modelName;
	}
	public String getUuid() {
		return uuid;
	}

	public abstract void debugInformations(StringBuffer buffer, int level);
}
