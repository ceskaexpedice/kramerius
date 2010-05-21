package cz.incad.kramerius.ngwt.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.fedora.api.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.TreeNodeProcessor;

public class CachedFedoraAccess implements FedoraAccess {

	@Inject
	@Named("rawFedoraAccess")
	private FedoraAccess fedoraAccess;

	public org.fedora.api.FedoraAPIA getAPIA() {
		return fedoraAccess.getAPIA();
	}

	public org.fedora.api.FedoraAPIM getAPIM() {
		return fedoraAccess.getAPIM();
	}

	public Document getBiblioMods(String uuid) throws IOException {
		return fedoraAccess.getBiblioMods(uuid);
	}

	public Document getDC(String uuid) throws IOException {
		return fedoraAccess.getDC(uuid);
	}

	public InputStream getImageFULL(String uuid) throws IOException {
		return fedoraAccess.getImageFULL(uuid);
	}

	public String getImageFULLMimeType(String uuid) throws IOException,
			XPathExpressionException {
		return fedoraAccess.getImageFULLMimeType(uuid);
	}

	public Document getImageFULLProfile(String uuid) throws IOException {
		return fedoraAccess.getImageFULLProfile(uuid);
	}

	public KrameriusModels getKrameriusModel(Document relsExt) {
		return fedoraAccess.getKrameriusModel(relsExt);
	}

	public KrameriusModels getKrameriusModel(String uuid) throws IOException {
		return fedoraAccess.getKrameriusModel(uuid);
	}

	public ObjectFactory getObjectFactory() {
		return fedoraAccess.getObjectFactory();
	}

	public List<Element> getPages(String uuid, boolean deep) throws IOException {
		return fedoraAccess.getPages(uuid, deep);
	}

	public List<Element> getPages(String uuid, Element rootElementOfRelsExt)
			throws IOException {
		return fedoraAccess.getPages(uuid, rootElementOfRelsExt);
	}

	public Set<String> getPids(String pid) {
		return fedoraAccess.getPids(pid);
	}

	public Document getRelsExt(String uuid) throws IOException {
		return fedoraAccess.getRelsExt(uuid);
	}

	public InputStream getThumbnail(String uuid) throws IOException {
		return fedoraAccess.getThumbnail(uuid);
	}

	public String getThumbnailMimeType(String uuid) throws IOException,
			XPathExpressionException {
		return fedoraAccess.getThumbnailMimeType(uuid);
	}

	public Document getThumbnailProfile(String uuid) throws IOException {
		return fedoraAccess.getThumbnailProfile(uuid);
	}

	public boolean isImageFULLAvailable(String uuid) throws IOException {
		return fedoraAccess.isImageFULLAvailable(uuid);
	}

	public void processRelsExt(Document relsExtDocument, RelsExtHandler handler)
			throws IOException {
		fedoraAccess.processRelsExt(relsExtDocument, handler);
	}

	public void processRelsExt(String uuid, RelsExtHandler handler)
			throws IOException {
		fedoraAccess.processRelsExt(uuid, handler);
	}

	public void processSubtree(String pid, TreeNodeProcessor processor) {
		fedoraAccess.processSubtree(pid, processor);
	}
	
	
}
