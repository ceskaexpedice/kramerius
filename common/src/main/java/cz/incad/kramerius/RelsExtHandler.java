package cz.incad.kramerius;

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
	 */
	public boolean accept(FedoraRelationship relation);

	/**
	 * Handle processing element 
	 * @param elm Processing element 
	 * @param relation Type of relation
	 */
	public void handle(Element elm, FedoraRelationship relation);
}
