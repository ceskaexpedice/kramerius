package cz.incad.kramerius.indexer.dnnt;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.*;
import java.io.IOException;
import java.util.HashMap;
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

}

