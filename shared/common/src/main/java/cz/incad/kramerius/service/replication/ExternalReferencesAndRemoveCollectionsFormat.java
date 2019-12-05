package cz.incad.kramerius.service.replication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.PIDParser;

public class ExternalReferencesAndRemoveCollectionsFormat extends ExternalReferencesFormat {

    @Override
    public byte[] formatFoxmlData(byte[] input, Object... params) throws ReplicateException{
        try {
        	
        	Document document = XMLUtils.parseDocument(new ByteArrayInputStream(input), true);
        	processDOM(document);
        	// remove all virtual collections
        	removeVirtualCollections(document,document.getDocumentElement());
            return serializeToBytes(document);
        } catch (ParserConfigurationException e) {
            throw new ReplicateException(e);
        } catch (SAXException e) {
            throw new ReplicateException(e);
        } catch (IOException e) {
            throw new ReplicateException(e);
        } catch (TransformerException e) {
            throw new ReplicateException(e);
        } catch (DOMException e) {
            throw new ReplicateException(e);
		} catch (URISyntaxException e) {
            throw new ReplicateException(e);
		}
    }


	
	private void  removeVirtualCollections(Document document, Element element) {

		List<Element> rdfversions =  XMLUtils.getElementsRecursive(element , new XMLUtils.ElementsFilter() {
			
			@Override
			public boolean acceptElement(Element el) {
				String localName = el.getLocalName();
				String namespace = el.getNamespaceURI();
				if (namespace.equals(FedoraNamespaces.RDF_NAMESPACE_URI)) {
					return localName.equals("Description");
				}
				return false;
			}
		});
		
		for (Element rdfDesc : rdfversions) {
			List<Element> delems = XMLUtils.getElements(rdfDesc);
			for (Element del : delems) {
				if (del.getNamespaceURI().equals(FedoraNamespaces.RDF_NAMESPACE_URI) && del.getLocalName().equals("isMemberOfCollection")) {

					Node parentNode = del.getParentNode();
					parentNode.removeChild(del);
//					String pidAttribute = del.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
//					if (pidAttribute.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
//						pidAttribute = pidAttribute.substring(PIDParser.INFO_FEDORA_PREFIX.length());
//					}
				}
			}
		}
		
	}

}
