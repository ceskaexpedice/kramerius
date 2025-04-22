package cz.inovatika.monitoring;

/**
 * Interface for monitoring REST API calls.
 * Provides methods to track the start and end of API calls
 * and allows recording metadata about each call.
 */
public interface APICallMonitor {

    /**
     * Starts tracking a new API call.
     *
     * @param resource Requesting rest resource
     * @param endpoint    The target URL endpoint of the API call.
     * @param queryString
     * @param httpMethod  The HTTP method used for the call (e.g., GET, POST, PUT, DELETE).
     * @return An ApiCallEvent object representing the started API call.
     */
    ApiCallEvent start(String resource, String endpoint, String queryString, String httpMethod);

    /**
     * Starts tracking a new API call.
     *
     * @param resource Requesting rest resource
     * @param endpoint    The target URL endpoint of the API call.
     * @param queryString
     * @param httpMethod  The HTTP method used for the call (e.g., GET, POST, PUT, DELETE).
     * @param pid Requesting pid
     * @return An ApiCallEvent object representing the started API call.
     */
    ApiCallEvent start(String resource, String endpoint, String queryString, String httpMethod, String pid);

    /**
     * Stops tracking an API call and records its completion.
     * This method is typically called after the API call has finished executing.
     *
     * @param event  The ApiCallEvent object representing the API call to stop.
     * @param userId The ID of the user who initiated the API call (optional, can be null).
     */
    void stop(ApiCallEvent event, String userId);

    /**
     * Commit uncommited events
     */
    void commit();


    /**
     * Queries the monitoring system using Solr syntax and returns results in JSON format.
     * <p>
     * This method allows retrieving recorded API call data from the monitoring system,
     * formatted as a JSON response.
     * </p>
     *
     * @param solrQuery The Solr query string to filter monitoring results.
     * @return A JSON-formatted string containing the API monitoring data.
     */
    String apiMonitorRequestJson(String solrQuery);


    /**
     * Queries the monitoring system using Solr syntax and returns results in XML format.
     * <p>
     * Similar to {@link #apiMonitorRequestJson(String)}, but returns the monitoring data in XML format.
     * </p>
     *
     * @param solrQuery The Solr query string to filter monitoring results.
     * @return An XML-formatted string containing the API monitoring data.
     */
    String apiMonitorRequestXML(String solrQuery);
}