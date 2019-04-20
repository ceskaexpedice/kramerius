package cz.incad.kramerius;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

public class DNNTBatchUtils {

    private static final String DNNT_MODE = "dnnt.solr.mode";


    private DNNTBatchUtils() {}

    public static Document createBatch(List<String> pids) throws ParserConfigurationException {
        DNNTMode mode = DNNTMode.valueOf(KConfiguration.getInstance().getConfiguration().getString(DNNT_MODE, DNNTMode.add.name()));
        DocumentBuilder builder = DocumentBuilderFactory.newInstance(). newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElm = document.createElement("add");
        document.appendChild(rootElm);
        for (String pid :
                pids) {
            Element doc = addDoc(document, pid, mode);
            rootElm.appendChild(doc);
        }
        return document;
    }

    private static Element addDoc(Document document, String pid, DNNTMode flag) {
        Element doc = document.createElement("doc");

        Element fname = document.createElement("field");
        fname.setAttribute("name", "PID");
        fname.setTextContent(pid);

        doc.appendChild(fname);

        Element fdnnt = document.createElement("field");
        fdnnt.setAttribute("name", "dnnt");
        fdnnt.setAttribute("update", flag.toString());
        fdnnt.setTextContent("true");

        doc.appendChild(fdnnt);

        return doc;
    }



}
