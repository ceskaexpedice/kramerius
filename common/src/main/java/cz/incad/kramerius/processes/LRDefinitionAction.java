package cz.incad.kramerius.processes;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Actions defined for LR PROCESS
 * @author pavels
 * @deprecated
 */
public class LRDefinitionAction {

	public static final LRDefinitionAction LOGS_ACTION = new LRDefinitionAction("inc/admin/_processes_outputs.jsp","logs","administrator.processes.logs");
	
	
	private String actionURL;
	private String name;
	private String resourceBundleKey;
	
	public LRDefinitionAction() {
		super();
	}
	

	private LRDefinitionAction(String actionURL, String name, String rbkey) {
		super();
		this.actionURL = actionURL;
		this.name = name;
		this.resourceBundleKey = rbkey;
	}




	public String getActionURL() {
		return actionURL;
	}

	public String getName() {
		return name;
	}

	
	public void setActionURL(String actionURL) {
		this.actionURL = actionURL;
	}


	public void setName(String name) {
		this.name = name;
	}

	
	
	public String getResourceBundleKey() {
		return resourceBundleKey;
	}


	public void setResourceBundleKey(String resourceBundleKey) {
		this.resourceBundleKey = resourceBundleKey;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((actionURL == null) ? 0 : actionURL.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}




	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LRDefinitionAction other = (LRDefinitionAction) obj;
		if (actionURL == null) {
			if (other.actionURL != null)
				return false;
		} else if (!actionURL.equals(other.actionURL))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}




	public void loadFromXml(Element elm) {
		NodeList nodes = elm.getChildNodes();
		for (int i = 0,ll=nodes.getLength(); i < ll; i++) {
			Node childNode = nodes.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				if(childNode.getNodeName().equals("name")) {
					this.name = childNode.getTextContent();
				}
				if(childNode.getNodeName().equals("actionURL")) {
					this.actionURL = childNode.getTextContent();
				}
				if(childNode.getNodeName().equals("resourceBundleKey")) {
					this.resourceBundleKey = childNode.getTextContent();
				}
			}
		}
	}
	
}
