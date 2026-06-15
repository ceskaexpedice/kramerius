package cz.incad.kramerius.rest.apiNew.exceptions;


import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class ApiException extends WebApplicationException {

    public ApiException(Response response) {
        super(response);
    }

    public ApiException(int errorCode) {
        super(errorCode);
    }

    public ApiException(Response.Status status) {
        super(status);
    }

    public ApiException(int errorCode, String message) {
        this(Response.status(errorCode)
                .type(MediaType.APPLICATION_JSON)
                .entity(buildErrorJson(message))
                .build());
    }

    public ApiException(int errorCode, String messageTemplate, Object... messageArgs) {
        this(errorCode, String.format(messageTemplate, messageArgs));
    }

    public ApiException(Response.Status status, String message) {
        this(status.getStatusCode(), message);
    }

    public ApiException(Response.Status status, String messageTemplate, Object... messageArgs) {
        this(status, String.format(messageTemplate, messageArgs));
    }

    private static String buildErrorJson(String errorMessage) {
        JSONObject json = new JSONObject();
        try {
            //json.put("error", errorMessage);
            json.put("message", errorMessage);
        } catch (JSONException e) {
            //noting
        }
        return json.toString();
    }
}
