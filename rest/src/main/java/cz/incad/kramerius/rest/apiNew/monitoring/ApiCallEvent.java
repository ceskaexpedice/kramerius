package cz.incad.kramerius.rest.apiNew.monitoring;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApiCallEvent {

    private List<String> labels = new ArrayList<>();

    private String resource;
    private String endpoint;
    private String queryPart;
    private String httpMethod;
    private long startTime;
    private long endTime;
    private int statusCode;
    private String userId;
    private String pid;


    public ApiCallEvent(String resource, String endpoint, String queryPart, String httpMethod) {
        this.resource = resource;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.queryPart = queryPart;
        this.startTime = System.currentTimeMillis();
    }

    public ApiCallEvent(String resource, String endpoint, String queryPart, String httpMethod, String pid) {
        this.resource = resource;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.queryPart = queryPart;
        this.pid = pid;
        this.startTime = System.currentTimeMillis();
    }



    public String getUniqueIdentifier() {
        return UUID.randomUUID().toString();
    }

    public List<String> getLabels() {
        return labels;
    }

    public void addLabel(String lbl) {
        if (!this.labels.contains(lbl)) {
            this.labels.add(lbl);
        }
    }

    public void removeLabel(String lbl) {
        this.labels.remove(lbl);
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getQueryPart() {
        return queryPart;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getDuration() {
        return endTime - startTime;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getUserId() {
        return userId;
    }

    public String getPid() {
        return pid;
    }

    /**
     * Converts the ApiCallEvent object into a Solr-compatible XML document.
     *
     * @return A String containing the XML document.
     * @throws javax.xml.parsers.ParserConfigurationException If there is an issue creating the XML document.
     * @throws javax.xml.transform.TransformerException If there is an issue transforming the document to a string.
     */
    public Document toSolrDocument(DocumentBuilderFactory builderFactory) throws ParserConfigurationException, TransformerException {

        DocumentBuilder document = builderFactory.newDocumentBuilder();
        Document doc = document.newDocument();
        Element add = doc.createElement("add");
        doc.appendChild(add);

        Element rootElement = doc.createElement("doc");
        add.appendChild(rootElement);

        addField(doc, rootElement, "identifier", getUniqueIdentifier());
        addField(doc, rootElement, "endpoint", endpoint);
        addField(doc, rootElement, "resource", resource);
        addField(doc, rootElement, "querypart", queryPart);
        addField(doc, rootElement, "pid", pid);

        addField(doc, rootElement, "httpMethod", httpMethod);


        addField(doc, rootElement, "startTime", formatAsIso8601(startTime));
        addField(doc, rootElement, "endTime", formatAsIso8601(endTime));

        addField(doc, rootElement, "duration", String.valueOf(getDuration()));
        addField(doc, rootElement, "statusCode", String.valueOf(statusCode));
        addField(doc, rootElement, "userId", userId);

        if (!labels.isEmpty()) {
            for (String label : labels) {
                addField(doc, rootElement, "labels", label);
            }
        }

        return doc;
    }

    private String formatAsIso8601(long timestamp) {
        return Instant.ofEpochMilli(timestamp).toString();
    }

    /**
     * Helper method to add a field to the XML document.
     * @param doc The XML document.
     * @param root The root element of the document.
     * @param fieldName The name of the field.
     * @param fieldValue The value of the field.
     */
    private void addField(Document doc, Element root, String fieldName, String fieldValue) {
        if (fieldValue != null) {
            Element field = doc.createElement("field");
            field.setAttribute("name", fieldName);
            field.appendChild(doc.createTextNode(fieldValue));
            root.appendChild(field);
        }
    }
}