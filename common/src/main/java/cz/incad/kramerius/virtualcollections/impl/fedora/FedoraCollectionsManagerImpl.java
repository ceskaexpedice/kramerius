package cz.incad.kramerius.virtualcollections.impl.fedora;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionUtils;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import cz.incad.kramerius.virtualcollections.impl.AbstractCollectionManager;

public class FedoraCollectionsManagerImpl extends AbstractCollectionManager {

    public static final Logger LOGGER = Logger.getLogger(FedoraCollectionsManagerImpl.class.getName());

    public FedoraCollectionsManagerImpl() {
        super();
    }
    
    @Override
    protected List<String> languages() {
        return super.languages();
    }

    @Override
    public List<Collection> getCollections() throws CollectionException {
        try {
            List<Collection> cols = new ArrayList<Collection>();
            Document doc = getCollectionListFromResourceIndex();
            NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(SPARQL_NS, "result");
            for (int i = 0, ll = nodes.getLength(); i < ll; i++) {
                Node item = nodes.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElm = (Element) item;
                    Collection col = new Collection(sparqlPid(itemElm), sparqlTitle(itemElm), sparqlType(itemElm));
                    cols.add(col);
                }
            }
            for (Collection col : cols) {
                this.enhanceDescriptions(col);
            }
            return cols;
        } catch (ClassNotFoundException e) {
            throw new CollectionException(e);
        } catch (InstantiationException e) {
            throw new CollectionException(e);
        } catch (IllegalAccessException e) {
            throw new CollectionException(e);
        } catch (DOMException e) {
            throw new CollectionException(e);
        } catch (LexerException e) {
            throw new CollectionException(e);
        } catch (IOException e) {
            throw new CollectionException(e);
        } catch (Exception e) {
            throw new CollectionException(e);
        }
    }

    Document getCollectionListFromResourceIndex()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
        Document doc = g.getVirtualCollections();
        return doc;
    }

    protected String sparqlPid(Element result) throws LexerException {
        Element findElement = XMLUtils.findElement(result, "object");
        if (findElement != null) {
            String text = findElement.getAttribute("uri");
            PIDParser pidParser = new PIDParser(text);
            pidParser.disseminationURI();
            return pidParser.getObjectPid();
        }
        return "";
    }

    protected String sparqlTitle(Element result) {
        Element findElement = XMLUtils.findElement(result, "title");
        if (findElement != null) {
            String text = findElement.getTextContent();
            return text;
        }
        return "";
    }

    protected boolean sparqlType(Element result) {
        Element findElement = XMLUtils.findElement(result, "canLeave");
        if (findElement != null) {
            String text = findElement.getTextContent();
            if (text.startsWith("\"")) text=text.substring(1);
            if (text.endsWith("\"")) text=text.substring(0,text.length()-1);
            return "canLeave:true".equals(text);
        }
        return false;
    }
    
}
