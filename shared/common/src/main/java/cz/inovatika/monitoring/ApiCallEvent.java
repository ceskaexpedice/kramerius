package cz.inovatika.monitoring;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONObject;
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

/**
 * Represents an API call event, capturing metadata such as query parameters,
 * HTTP method, request duration, user identification, process ID (PID), and IP address.
 * This class also provides functionality to convert the event data into a Solr-compatible XML document.
 */
public class ApiCallEvent {

    /** Detailed snapshot information about event */
    private List<Triple<String, Long,Long>> granularTimeSnapshots = new ArrayList<>();

    /** A list of labels providing additional metadata for the API call. */
    private List<String> labels = new ArrayList<>();

    /** The API resource being accessed. */
    private String resource;

    /** The specific API endpoint being called. */
    private String endpoint;

    /** The query parameters associated with the request. */
    private String queryPart;

    /** The HTTP method used for the request (e.g., GET, POST, PUT, DELETE). */
    private String httpMethod;

    /** The timestamp (in milliseconds) when the request started. */
    private long startTime;

    /** The timestamp (in milliseconds) when the request ended. */
    private long endTime;

    /** The HTTP status code returned from the request. */
    private int statusCode;

    /** The user ID associated with the request. */
    private String userId;

    /** The PID of object. */
    private String pid;

    /** The IP address from which the request originated. */
    private String ipAddress;



    /**
     * Creates a new ApiCallEvent instance with mandatory request details.
     *
     * @param resource The API resource being accessed.
     * @param endpoint The API endpoint being called.
     * @param queryPart The query parameters for the request.
     * @param httpMethod The HTTP method used.
     */
    public ApiCallEvent(String resource, String endpoint, String queryPart, String httpMethod) {
        this.resource = resource;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.queryPart = queryPart;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Creates a new ApiCallEvent instance with additional process ID (PID) information.
     *
     * @param resource The API resource being accessed.
     * @param endpoint The API endpoint being called.
     * @param queryPart The query parameters for the request.
     * @param httpMethod The HTTP method used.
     * @param pid The process ID associated with the request.
     */
    public ApiCallEvent(String resource, String endpoint, String queryPart, String httpMethod, String pid) {
        this.resource = resource;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.queryPart = queryPart;
        this.pid = pid;
        this.startTime = System.currentTimeMillis();
    }



    /**
     * Generates a unique identifier for this API call event.
     *
     * @return A randomly generated UUID as a string.
     */
    public String getUniqueIdentifier() {
        return UUID.randomUUID().toString();
    }


    /**
     * Retrieves the list of labels associated with the API call.
     *
     * @return A list of labels.
     */
    public List<String> getLabels() {
        return labels;
    }

    /**
     * Adds a label to the list if it does not already exist.
     *
     * @param lbl The label to add.
     */
    public void addLabel(String lbl) {
        if (!this.labels.contains(lbl)) {
            this.labels.add(lbl);
        }
    }

    /**
     * Removes a label from the list.
     *
     * @param lbl The label to remove.
     */
    public void removeLabel(String lbl) {
        this.labels.remove(lbl);
    }


    /**
     * Sets the list of labels for this event.
     *
     * @param labels A list of labels to assign.
     */
    public void setLabels(List<String> labels) {
        this.labels = labels;
    }


    /**
     * Returns information about different time snapshots recorded during a request.
     * <p>
     * Each triplet contains the following details:
     * </p>
     * <ul>
     *     <li><b>label</b> – Describes where the time snapshot was taken (e.g., Solr, HttpClient, etc.).</li>
     *     <li><b>startTime</b> – The start date/time of the snapshot.</li>
     *     <li><b>endTime</b> – The end date/time of the snapshot.</li>
     * </ul>
     *
     * @return Detailed information about time spent in different stages of the request.
     */
    public List<Triple<String, Long, Long>> getGranularTimeSnapshots() {
        return granularTimeSnapshots;
    }

    /**
     * Sets detailed information about the time spent during a request.
     * <p>
     * This method records granular time snapshots to track performance metrics
     * for different stages of the request, such as database queries,
     * external API calls, or internal processing.
     * </p>
     *
     * @param granularTimeSnapshots A collection of time snapshots, each containing:
     *                              <ul>
     *                                  <li><b>label</b> – A description of the tracked event (e.g., Solr query, HTTP client request).</li>
     *                                  <li><b>startTime</b> – The timestamp indicating when the event started.</li>
     *                                  <li><b>endTime</b> – The timestamp indicating when the event ended.</li>
     *                              </ul>
     *                              This data helps analyze time distribution and identify potential performance bottlenecks.
     */
    public void setGranularTimeSnapshots(List<Triple<String, Long, Long>> granularTimeSnapshots) {
        this.granularTimeSnapshots = granularTimeSnapshots;
    }


    /** @return The query parameters of the API request. */
    public String getQueryPart() {
        return queryPart;
    }

    /** @return The endpoint of the API request. */
    public String getEndpoint() {
        return endpoint;
    }

    /** @return The HTTP method used for the request. */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Gets the starting time of this event.
     *
     * @return The timestamp representing the start of this event.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the time when this event finished.
     *
     * @return The timestamp representing the end of this event.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of the API request.
     *
     * @param endTime The timestamp when the request ended.
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * Sets the HTTP status code for the request.
     *
     * @param statusCode The HTTP status code to assign.
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Sets the user ID associated with this request.
     *
     * @param userId The user ID to assign.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Calculates the duration of the API request.
     *
     * @return The duration of the request in milliseconds.
     */
    public long getDuration() {
        return endTime - startTime;
    }

    /** @return The HTTP status code of the request. */
    public int getStatusCode() {
        return statusCode;
    }

    /** @return The user ID associated with the request. */
    public String getUserId() {
        return userId;
    }

    /** @return The process ID associated with the request. */
    public String getPid() {
        return pid;
    }

    /** @return The IP address from which the request originated. */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the IP address for the request.
     *
     * @param ipAddress The IP address to assign.
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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
        addField(doc, rootElement, "ipaddress", this.ipAddress);

        if (!this.granularTimeSnapshots.isEmpty()) {

            JSONArray array = new JSONArray();
            this.granularTimeSnapshots.stream().forEach(t-> {
                String str = t.getLeft();
                Long start = t.getMiddle();
                Long stop = t.getRight();
                JSONObject obj = new JSONObject();

                obj.put("name", str);
                obj.put("start", start);
                obj.put("stop",stop);

                array.put(obj);
            });
            addField(doc, rootElement, "granularSnapshot", array.toString());
        }

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