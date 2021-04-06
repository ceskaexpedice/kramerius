package cz.incad.kramerius.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.StringWriter;
import java.util.List;

public class DNNTBatchUtils {

    private static final String DNNT_MODE = "dnnt.solr.mode";


    private DNNTBatchUtils() {}


    public static Document createLegacyDNNT(List<String> pids, boolean addRemoveFlag) throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance(). newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElm = document.createElement("add");
        document.appendChild(rootElm);
        for (String pid : pids) {
            Element doc = addLegacyDNNT(document, pid,  addRemoveFlag);
            rootElm.appendChild(doc);
        }
        return document;
    }

    private static Element addLegacyDNNT(Document document, String pid,  boolean addRemoveFlag) {
        Element doc = document.createElement("doc");

        Element fname = document.createElement("field");
        fname.setAttribute("name", "PID");
        fname.setTextContent(pid);

        doc.appendChild(fname);

        Element fdnnt = document.createElement("field");
        fdnnt.setAttribute("name", "dnnt");
        fdnnt.setAttribute("update", "set");
        fdnnt.setTextContent(""+addRemoveFlag);

        doc.appendChild(fdnnt);

        if (!addRemoveFlag) {
            Element labels = document.createElement("field");
            labels.setAttribute("name", "dnnt-labels");
            labels.setAttribute("update", "set");
            labels.setAttribute("null","true");
            doc.appendChild(labels);

        }


        return doc;
    }

    private static Element addLabeledDNNT(Document document, String pid, String label,  boolean addRemoveFlag, boolean parentDnnt) {
        Element doc = document.createElement("doc");

        Element fname = document.createElement("field");
        fname.setAttribute("name", "PID");
        fname.setTextContent(pid);

        doc.appendChild(fname);

        // whole flag must be removed
        Element fdnnt = document.createElement("field");
        fdnnt.setAttribute("name", "dnnt");
        fdnnt.setAttribute("update", "set");
        fdnnt.setTextContent(""+parentDnnt);
        doc.appendChild(fdnnt);


        Element labels = document.createElement("field");
        labels.setAttribute("name", "dnnt-labels");
        labels.setAttribute("update", addRemoveFlag ? "add" : "remove");
        labels.setTextContent(label);




        doc.appendChild(labels);

        return doc;
    }


    public static Document createLabeledDNNT(List<String> pids, String label, boolean addRemoveFlag, boolean changedFoxml) throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance(). newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElm = document.createElement("add");
        document.appendChild(rootElm);
        for (String pid : pids) {
            Element doc = addLabeledDNNT(document, pid, label, addRemoveFlag,changedFoxml);
            rootElm.appendChild(doc);
        }
        return document;
    }
}
