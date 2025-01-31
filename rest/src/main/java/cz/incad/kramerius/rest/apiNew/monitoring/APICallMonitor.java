package cz.incad.kramerius.rest.apiNew.monitoring;

/**
 * Interface for monitoring REST API calls.
 * Provides methods to track the start and end of API calls
 * and allows recording metadata about each call.
 */
public interface APICallMonitor {

    /**
     * Starts tracking a new API call.
     *
     * @param resource
     * @param endpoint    The target URL endpoint of the API call.
     * @param queryString
     * @param httpMethod  The HTTP method used for the call (e.g., GET, POST, PUT, DELETE).
     * @return An ApiCallEvent object representing the started API call.
     */
    ApiCallEvent start(String resource, String endpoint, String queryString, String httpMethod);

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

    String apiMonitorRequestJson(String solrQuery);

    String apiMonitorRequestXML(String solrQuery);
}