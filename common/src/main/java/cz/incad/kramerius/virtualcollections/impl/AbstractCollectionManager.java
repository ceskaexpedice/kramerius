package cz.incad.kramerius.virtualcollections.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionPidUtils;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import cz.incad.kramerius.virtualcollections.CollectionsManager.SortType;

public abstract class AbstractCollectionManager implements CollectionsManager {

    static final String TEXT_DS_PREFIX = "TEXT_";
    protected static final String SPARQL_NS = "http://www.w3.org/2001/sw/DataAccess/rf1/result";
    @Inject
    @Named("rawFedoraAccess")
    protected FedoraAccess fa;

    protected XPathFactory factory = XPathFactory.newInstance();

    public AbstractCollectionManager() {
        this.factory = XPathFactory.newInstance();
    }
    
    public FedoraAccess getFedoraAccess() {
        return fa;
    }

    public void setFedoraAccess(FedoraAccess fa) {
        this.fa = fa;
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

    @Override
    public List<Collection> getSortedCollections(Locale locale, SortType type) throws CollectionException {
        List<Collection> cols = new ArrayList<Collection>(getCollections());
        Collections.sort(cols, new CollectionComparator(locale, type));
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
            Document doc = this.fa.getDC(pid);
            Collection col = new Collection(pid, dcTitle(doc), dcType(doc));
            enhanceDescriptions(col);
            return col;
        } catch (XPathExpressionException e) {
            throw new CollectionException(e);
        } catch (DOMException e) {
            throw new CollectionException(e);
        } catch (IOException e) {
            throw new CollectionException(e);
        }
    }

    protected void enhanceDescriptions(Collection col) throws IOException, XPathExpressionException {
        Document dc = this.fa.getDC(col.getPid());
        boolean dcType = dcType(dc);
        if (col.isCanLeaveFlag() != dcType) {
            col.changeCanLeaveFlag(dcType);
        }
        for (String lang : languages()) {
            String dsName = TEXT_DS_PREFIX + lang;
            if (this.fa.isStreamAvailable(col.getPid(), dsName)) {
                String text = IOUtils.readAsString(this.fa.getDataStream(col.getPid(), dsName), Charset.forName("UTF8"),
                        true);
                col.addDescription(new Collection.Description(lang, dsName, text));
            }
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

    protected class CollectionComparator implements Comparator<Collection> {
        
        private Locale locale;
        private Collator coll;
        private SortType type;
        
        public CollectionComparator(Locale locale, SortType type) {
            super();
            this.locale = locale;
            this.type = type;
            this.coll = Collator.getInstance(locale);
        }

        @Override
        public int compare(Collection o1, Collection o2) {
            Collection firstToComparsion = this.type.equals(SortType.ASC) ? o1 : o2;
            Collection secondToComparsion = this.type.equals(SortType.ASC) ? o2 : o1;
            return this.coll.compare(firstToComparsion.lookup(this.locale.getLanguage()).getText(), secondToComparsion.lookup(this.locale.getLanguage()).getText());
        }
    }
    

}
