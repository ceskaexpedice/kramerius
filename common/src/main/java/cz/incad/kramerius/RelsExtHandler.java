package cz.incad.kramerius;

import java.util.Stack;

import org.w3c.dom.Element;

/**
 * Interface allows to user to be informed of events 
 * during processRelsExt method call
 *  
 * @author pavels
 * @see FedoraAccess#processRelsExt(org.w3c.dom.Document, RelsExtHandler)
 * @see FedoraAccess#processRelsExt(String, RelsExtHandler)
 */
public interface RelsExtHandler {

	/**
	 * Accept or deny this relation. If method returns false, algortighm  
	 * skip this element and continues with next
	 * @param relation Relation type
	 * @param relationShipName TODO
	 */
	public boolean accept(FedoraRelationship relation, String relationShipName);

	/**
	 * Handle processing element 
	 * @param elm Processing element 
	 * @param relation Type of relation
	 * @param relationshipName TODO
	 */
	public void handle(Element elm, FedoraRelationship relation, String relationshipName, int level);

	public boolean breakProcess();
}
