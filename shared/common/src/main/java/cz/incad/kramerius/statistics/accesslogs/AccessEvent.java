package cz.incad.kramerius.statistics.accesslogs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AccessEvent {

    // Základní údaje požadavku
    private final String pid;
    private final String streamName;
    private final String actionName;
    private final Date timestamp;

    // HTTP kontext
    private final String sessionToken;
    private final String requestedUrl;
    private final String remoteAddress;
    private final String referer;
    private final String client;

    // Uživatel
    private final String userLogin;
    private final String userSessionAttributes;

    // Informace o vyhodnocení licence
    private final Map<String, String> evaluateInfoMap;

    public AccessEvent(
            String pid,
            String streamName,
            String actionName,
            Date timestamp,
            String sessionToken,
            String requestedUrl,
            String remoteAddress,
            String referer,
            String client,
            String userLogin,
            String userSessionAttributes,
            Map<String, String> evaluateInfoMap) {

        this.pid = pid;
        this.streamName = streamName;
        this.actionName = actionName;
        this.timestamp = new Date(timestamp.getTime());

        this.sessionToken = sessionToken;
        this.requestedUrl = requestedUrl;
        this.remoteAddress = remoteAddress;
        this.referer = referer;
        this.client = client;

        this.userLogin = userLogin;
        this.userSessionAttributes = userSessionAttributes;

        this.evaluateInfoMap = evaluateInfoMap != null ? new HashMap<>(evaluateInfoMap) : new HashMap<>();
    }

    public String getPid() {
        return pid;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getActionName() {
        return actionName;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getRequestedUrl() {
        return requestedUrl;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getReferer() {
        return referer;
    }

    public String getClient() {
        return client;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getUserSessionAttributes() {
        return userSessionAttributes;
    }

    public Map<String, String> getEvaluateInfoMap() {
        return new HashMap<>(evaluateInfoMap);
    }
}