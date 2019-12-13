package cz.incad.kramerius.virtualcollections.impl.fedora;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.*;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.*;
import cz.incad.kramerius.virtualcollections.Collection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.utils.XMLUtils;

import javax.xml.xpath.*;

/**
 * Implementation stands on fedora
 * @author pstastny
 *
 */
public class FedoraCollectionsManagerImpl implements CollectionsManager {

    public static final Logger LOGGER = Logger.getLogger(FedoraCollectionsManagerImpl.class.getName());
    public static final String TEXT_DS_PREFIX = "TEXT_";
    public static final String LONG_TEXT_DS_PREFIX = "LONG_TEXT_";
    protected static final String SPARQL_NS = "http://www.w3.org/2001/sw/DataAccess/rf1/result";
    protected XPathFactory factory = XPathFactory.newInstance();

    private IResourceIndex resourceIndex;
    private FedoraAccess fa;
    private SolrAccess sa;

    @Inject
    public FedoraCollectionsManagerImpl(IResourceIndex g, SolrAccess sa,  @Named("rawFedoraAccess")FedoraAccess fa) {
        super();
        this.factory = XPathFactory.newInstance();
        this.fa = fa;
        this.sa = sa;
        this.resourceIndex = g;
    }

    protected List<String> languages() {
        List<String> langCodes = new ArrayList<String>();
        String[] propertyList = KConfiguration.getInstance().getPropertyList("interface.languages");
        int iterate = propertyList.length/2;
        for (int i = 0; i < iterate; i++) {
            int nameIndex = i*2;
            int langIndex = (i*2)+1;
            String name = nameIndex < propertyList.length ? propertyList[nameIndex] : null;
            String langcode = langIndex < propertyList.length ? propertyList[langIndex] : null;
            if (langcode != null) langCodes.add(langcode);
        }
        return langCodes;
    }
    
    private Collection findCollection(String pid,List<Collection> cols) {
        for (Collection cl : cols) {
            if (cl.getPid().equals(pid)) return cl;
        }
        return null;
    }
    
    @Override
    public List<Collection> getCollections() throws CollectionException {
        try {
            List<Collection> cols = new ArrayList<>();
            //IResourceIndex g = ResourceIndexService.getResourceIndexImpl();
            List<String> collectionPids = this.resourceIndex.getCollections();
            for (String cPid : collectionPids) {
                if (findCollection(cPid, cols) == null) {
                    if (this.fa.isObjectAvailable(cPid)) {
                        if (this.fa.isStreamAvailable(cPid, FedoraUtils.DC_STREAM)) {
                            Document dc = fa.getDC(cPid);
                            Collection col = new Collection(cPid, dcTitle(dc), dcUrl(dc), dcType(dc));
                            enhanceNumberOfDocs(col);
                            enhanceDescriptions(col);
                            cols.add(col);
                        } else {
                            LOGGER.warning("Collection '"+cPid+"' doesn't exist");
                        }
                    } else {
                        LOGGER.warning("Collection '"+cPid+"' doesn't exist");
                        //throw new CollectionException("Collection '"+cPid+"' doesn't exist");
                    }
                }
            }
            return cols;
        } catch (DOMException e) {
            throw new CollectionException(e);
        } catch (IOException e) {
            throw new CollectionException(e);
        } catch (Exception e) {
            throw new CollectionException(e);
        }
    }

    public FedoraAccess getFedoraAccess() {
        return fa;
    }

    public void setFedoraAccess(FedoraAccess fa) {
        this.fa = fa;
    }

    public SolrAccess getSolrAccess() {
        return sa;
    }

    public void setSolrAccess(SolrAccess sa) {
        this.sa = sa;
    }

    @Override
    public List<Collection> getSortedCollections(Locale locale, SortOrder ordering, SortType type) throws CollectionException {
        List<Collection> cols = new ArrayList<Collection>(getCollections());
        if (type.equals(SortType.ALPHABET)) {
            Collections.sort(cols, new NameCollectionComparator(locale, ordering));
        } else {
            Collections.sort(cols, new NumberOfDocuments(ordering));
        }
        return cols;
    }

    @Override
    public boolean containsDataStream(String pid, String streamName) throws CollectionException {
        try {
            if (CollectionPidUtils.isCollectionPid(pid)) {
                if (this.fa.isStreamAvailable(pid, streamName)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new CollectionException(e);
        }
    }

    @Override
    public Collection getCollection(String pid) throws CollectionException {
        try {
            if (this.fa.isObjectAvailable(pid)) {
                if (this.fa.isStreamAvailable(pid, FedoraUtils.DC_STREAM)) {
                    Document doc = this.fa.getDC(pid);

                    Collection col = new Collection(pid, dcTitle(doc), dcUrl(doc),dcType(doc));
                    enhanceNumberOfDocs(col);
                    enhanceDescriptions(col);
                    return col;
                } else {
                    throw new CollectionException("Collection '"+pid+"' doesn't exist");
                }
            } else throw new CollectionException("Collection '"+pid+"' doesn't exist");
        } catch (XPathExpressionException e) {
            throw new CollectionException(e);
        } catch (DOMException e) {
            throw new CollectionException(e);
        } catch (IOException e) {
            throw new CollectionException(e);
        }
    }

    protected void enhanceNumberOfDocs(Collection col) throws IOException, XPathExpressionException {
        Document response = this.sa.request("fq=level:0&q=collection:(\""+col.getPid()+"\")&rows=0");
        Element resElement = XMLUtils.findElement(response.getDocumentElement(), "result");
        if (resElement != null){
            String attribute = resElement.getAttribute("numFound");
            int parsedInt = Integer.parseInt(attribute);
            col.setNumberOfDocs(parsedInt);
        }
    }

    protected void enhanceDescriptions(Collection col) throws IOException, XPathExpressionException {
        if (this.fa.isStreamAvailable(col.getPid(), FedoraUtils.DC_STREAM)) {
            Document dc = this.fa.getDC(col.getPid());
            boolean dcType = dcType(dc);
            if (col.isCanLeaveFlag() != dcType) {
                col.changeCanLeaveFlag(dcType);
            }
        }
        for (String lang : languages()) {
            String shortDsName = TEXT_DS_PREFIX + lang;
            String longDsName = LONG_TEXT_DS_PREFIX + lang;
            String shorText = this.fa.isStreamAvailable(col.getPid(), shortDsName) ?  IOUtils.toString(this.fa.getDataStream(col.getPid(), shortDsName), Charset.forName("UTF8")) : null;
            String longText = this.fa.isStreamAvailable(col.getPid(), longDsName) ?  IOUtils.toString(this.fa.getDataStream(col.getPid(), longDsName), Charset.forName("UTF8")) : null;
            Collection.Description descObject = longText == null ?  new Collection.Description(lang,shortDsName,shorText) : new Collection.Description(lang,shortDsName, shorText, longDsName, longText);
            col.addDescription(descObject);
        }
    }

    protected boolean dcType(Document doc) throws XPathExpressionException {
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//dc:type/text()");
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (node != null) {
            String rValue = node.getNodeValue();
            if (rValue.startsWith("canLeave:")) {
                rValue = rValue.substring("canLeave:".length());
            }
            return Boolean.parseBoolean(StringEscapeUtils.escapeXml(rValue));
        } else
            return false;
    }

    protected String dcTitle(Document doc) throws XPathExpressionException {
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//dc:title/text()");
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (node != null) {
            return StringEscapeUtils.escapeXml(node.getNodeValue());
        } else
            return "";
    }

    protected String dcUrl(Document doc) throws XPathExpressionException {
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//dc:source/text()");
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if (node != null) {
            return StringEscapeUtils.escapeXml(node.getNodeValue());
        } else
            return "";
    }

    protected class NumberOfDocuments implements Comparator<Collection> {

        private SortOrder ordering;


        public NumberOfDocuments(SortOrder ordering) {
            super();
            this.ordering = ordering;
        }


        @Override
        public int compare(Collection o1, Collection o2) {
            Integer i1 = this.ordering.equals(SortOrder.ASC) ? new Integer(o1.getNumberOfDocs()) : new Integer(o2.getNumberOfDocs());
            Integer i2 = this.ordering.equals(SortOrder.ASC) ? new Integer(o2.getNumberOfDocs()) : new Integer(o1.getNumberOfDocs());
            return i1.compareTo(i2);
        }

    }

    protected class NameCollectionComparator implements Comparator<Collection> {

        private Locale locale;
        private Collator coll;
        private SortOrder ordering;

        public NameCollectionComparator(Locale locale, SortOrder ordering) {
            super();
            this.locale = locale;
            this.ordering = ordering;
            this.coll = Collator.getInstance(locale);
        }

        @Override
        public int compare(Collection o1, Collection o2) {
            Collection firstToComparsion = this.ordering.equals(SortOrder.ASC) ? o1 : o2;
            Collection secondToComparsion = this.ordering.equals(SortOrder.ASC) ? o2 : o1;
            return this.coll.compare(firstToComparsion.lookup(this.locale.getLanguage()).getText(), secondToComparsion.lookup(this.locale.getLanguage()).getText());
        }
    }
}
