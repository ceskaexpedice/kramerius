package cz.incad.kramerius.utils;

import cz.incad.kramerius.solr.SolrFieldsMapping;
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


    @Deprecated
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

    @Deprecated
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

    static Element addLabel(Document document, String pid, String label, boolean addRemoveFlag) {
        Element doc = document.createElement("doc");

        Element fname = document.createElement("field");
        fname.setAttribute("name", SolrFieldsMapping.getInstance().getPidField());
        fname.setTextContent(pid);

        doc.appendChild(fname);


        Element labels = document.createElement("field");
        labels.setAttribute("name",  SolrFieldsMapping.getInstance().getDnntLabelsField());

        String addCommand = KConfiguration.getInstance().getConfiguration().getString("dnnt.solr.label.addcommand","add");
        String deleteCommand = KConfiguration.getInstance().getConfiguration().getString("dnnt.solr.label.removecommand","removeregex");
        labels.setAttribute("update", addRemoveFlag ? addCommand : deleteCommand);
        labels.setTextContent(label);

        doc.appendChild(labels);

        return doc;
    }




    public static Document createLabelsBatch(List<String> pids, String label, boolean addRemoveFlag) throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance(). newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElm = document.createElement("add");
        document.appendChild(rootElm);
        for (String pid : pids) {
            Element doc = addLabel(document, pid, label, addRemoveFlag);
            rootElm.appendChild(doc);
        }
        return document;
    }



    static Element addContainsLabel(Document document, String pid, String label, boolean addRemoveFlag) {
        Element doc = document.createElement("doc");

        Element fname = document.createElement("field");
        fname.setAttribute("name", SolrFieldsMapping.getInstance().getPidField());
        fname.setTextContent(pid);

        doc.appendChild(fname);


        Element containsLabels = document.createElement("field");
        containsLabels.setAttribute("name",  SolrFieldsMapping.getInstance().getContainsDnntLabelsField());

        String addCommand = KConfiguration.getInstance().getConfiguration().getString("dnnt.solr.label.addcommand","add");
        String deleteCommand = KConfiguration.getInstance().getConfiguration().getString("dnnt.solr.label.removecommand","removeregex");
        containsLabels.setAttribute("update", addRemoveFlag ? addCommand : deleteCommand);
        containsLabels.setTextContent(label);

        doc.appendChild(containsLabels);

        return doc;

    }

    public static Document createContainsLabelsBatch(List<String> parentPids, String label, boolean addRemoveFlag) throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance(). newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElm = document.createElement("add");
        document.appendChild(rootElm);
        for (String pid : parentPids) {
            Element doc = addContainsLabel(document, pid, label, addRemoveFlag);
            rootElm.appendChild(doc);
        }
        return document;
    }

}
