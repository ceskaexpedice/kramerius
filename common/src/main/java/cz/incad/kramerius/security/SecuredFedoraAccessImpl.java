package cz.incad.kramerius.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.impl.FedoraAccessImpl;

/**
 * This is secured variant of  class FedoraAccessImpl {@link FedoraAccessImpl}. <br>
 * Only three methos are secured: 
 * <ul>
 * 		<li> FedoraAccess#getImageFULL(String) </li>
 * 		<li> FedoraAccess#isImageFULLAvailable(String)</li>
 * 		<li> FedoraAccess#getImageFULLMimeType(String) </li>
 * </ul>
 * 
 * @see FedoraAccess#getImageFULL(String)
 * @see FedoraAccess#isImageFULLAvailable(String)
 * @see FedoraAccess#getImageFULLMimeType(String)
 * @author pavels
 */
public class SecuredFedoraAccessImpl implements FedoraAccess {
	
	private FedoraAccess rawAccess;
	private SecurityAcceptor acceptor;
	
	@Inject
	public SecuredFedoraAccessImpl(@Named("rawFedoraAccess")FedoraAccess rawAccess ) {
		super();
		this.rawAccess = rawAccess;
	}

	public Document getBiblioMods(String uuid) throws IOException {
		return rawAccess.getBiblioMods(uuid);
	}

	public Document getDC(String uuid) throws IOException {
		return rawAccess.getDC(uuid);
	}

	public InputStream getImageFULL(String uuid) throws IOException {
		if (!this.acceptor.privateVisitor()) {
			Document relsExt = this.rawAccess.getRelsExt(uuid);
			checkPolicyElement(relsExt);
		}
		return rawAccess.getImageFULL(uuid);
	}

	private void checkPolicyElement(Document relsExt) throws IOException {
		try {
			XPathFactory xpfactory = XPathFactory.newInstance();
			XPath xpath = xpfactory.newXPath();
			xpath.setNamespaceContext(new FedoraNamespaceContext());
			XPathExpression expr = xpath.compile("//kramerius:policy/text()");
			Object policy = expr.evaluate(relsExt,XPathConstants.STRING);
			if ((policy != null) && (policy.toString().trim().equals("policy:private"))) {
				throw new SecurityException("access denided");
			}
		} catch (XPathExpressionException e) {
			throw new IOException(e);
		}
	}

	public String getImageFULLMimeType(String uuid) throws IOException,
			XPathExpressionException {
		return rawAccess.getImageFULLMimeType(uuid);
	}

	public Document getImageFULLProfile(String uuid) throws IOException {
		return rawAccess.getImageFULLProfile(uuid);
	}

	public KrameriusModels getKrameriusModel(Document relsExt) {
		return rawAccess.getKrameriusModel(relsExt);
	}

	public KrameriusModels getKrameriusModel(String uuid) throws IOException {
		return rawAccess.getKrameriusModel(uuid);
	}

	public List<Element> getPages(String uuid, boolean deep) throws IOException {
		return rawAccess.getPages(uuid, deep);
	}

	public List<Element> getPages(String uuid, Element rootElementOfRelsExt)
			throws IOException {
		return rawAccess.getPages(uuid, rootElementOfRelsExt);
	}

	public Document getRelsExt(String uuid) throws IOException {
		return rawAccess.getRelsExt(uuid);
	}

	public InputStream getThumbnail(String uuid) throws IOException {
		return rawAccess.getThumbnail(uuid);
	}

	public String getThumbnailMimeType(String uuid) throws IOException,
			XPathExpressionException {
		return rawAccess.getThumbnailMimeType(uuid);
	}

	public Document getThumbnailProfile(String uuid) throws IOException {
		return rawAccess.getThumbnailProfile(uuid);
	}

	public boolean isImageFULLAvailable(String uuid) throws IOException {
		if (!this.acceptor.privateVisitor()) {
			Document relsExt = this.rawAccess.getRelsExt(uuid);
			checkPolicyElement(relsExt);
		}
		return rawAccess.isImageFULLAvailable(uuid);
	}

	public void processRelsExt(Document relsExtDocument, RelsExtHandler handler)
			throws IOException {
		rawAccess.processRelsExt(relsExtDocument, handler);
	}

	public void processRelsExt(String uuid, RelsExtHandler handler)
			throws IOException {
		rawAccess.processRelsExt(uuid, handler);
	}

	public SecurityAcceptor getAcceptor() {
		return acceptor;
	}

	
	@Inject
	public void setAcceptor(SecurityAcceptor acceptor) {
		this.acceptor = acceptor;
	}
	
	
	public FedoraAPIA getAPIA(){
	    return rawAccess.getAPIA();
	}
    public FedoraAPIM getAPIM(){
        return rawAccess.getAPIM();
    }
    public ObjectFactory getObjectFactory(){
        return rawAccess.getObjectFactory();
    }
    
    public void processSubtree(String pid, TreeNodeProcessor processor){
        rawAccess.processSubtree(pid, processor);
    }
    public Set<String> getPids(String pid){
        return rawAccess.getPids(pid);
    }
	
	
}
