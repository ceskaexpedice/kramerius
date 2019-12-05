package cz.incad.kramerius.document.model;

/**
 * Abstract document object
 * @author pavels
 */
public abstract class AbstractObject {

	protected String modelName;
	protected String uuid;
	
	
	public AbstractObject(String modelName, String uuid) {
		super();
		this.modelName = modelName;
		this.uuid = uuid;
	}
	
	/**
	 * Returns model
	 * @return
	 */
	public String getModel() {
		return modelName;
	}
	/**
	 * Returns pid
	 * @return
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Prints debug information
	 * @param buffer Information string buffer
	 * @param level Indentation level
	 */
	public abstract void debugInformations(StringBuffer buffer, int level);
}
