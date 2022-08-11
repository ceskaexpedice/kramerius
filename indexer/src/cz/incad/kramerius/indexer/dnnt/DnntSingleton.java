package cz.incad.kramerius.indexer.dnnt;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DnntSingleton {

    private static final DnntSingleton _INSTANCE = new DnntSingleton();

    private Map<String, Document> relsExts = new HashMap<>();

    private DnntSingleton() { }

    public static DnntSingleton getInstance() {
        return _INSTANCE;
    }

    public synchronized Document getRelsExt(String pid, FedoraAccess fa) throws IOException {
        if (!relsExts.containsKey(pid)) {
            relsExts.put(pid, fa.getRelsExt(pid));
        }
        return relsExts.get(pid);
    }


    public synchronized String dnnt(String pid, FedoraAccess fa) throws IOException, XPathExpressionException {
        String sxpath = "//kramerius:dnnt/text()";

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression compiled = xpath.compile(sxpath);

        String value = (String) compiled.evaluate(getRelsExt(pid, fa), XPathConstants.STRING);
        return value;
    }

    public synchronized List<String> dnntLabels(String pid, FedoraAccess fa) throws IOException, XPathExpressionException {
        List<String> list = new ArrayList<>();
        String sxpath = "//kramerius:dnnt-label";

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression compiled = xpath.compile(sxpath);

        NodeList value = (NodeList) compiled.evaluate(getRelsExt(pid, fa), XPathConstants.NODESET);
        for (int i = 0; i < value.getLength(); i++) {
            Node item = value.item(i);
            list.add(item.getTextContent());
        }
        return list;
    }

    public synchronized List<String> dnntContainsDNNTLabels(String pid, FedoraAccess fa) throws IOException, XPathExpressionException {
        List<String> list = new ArrayList<>();
        String sxpath = "//kramerius:contains-dnnt-labels";
        
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression compiled = xpath.compile(sxpath);

        NodeList value = (NodeList) compiled.evaluate(getRelsExt(pid, fa), XPathConstants.NODESET);
        for (int i = 0; i < value.getLength(); i++) {
            Node item = value.item(i);
            list.add(item.getTextContent());
        }
        return list;
    }

}

