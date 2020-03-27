package cz.incad.kramerius.rest.api.client.v60.client.exceptions;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

//TODO: use in api v6+ endpoints, not just client
public abstract class ApiException extends WebApplicationException {

    public ApiException(Response response) {
        super(response);
    }

    static String buildErrorJson(String errorMessage) {
        JSONObject json = new JSONObject();
        try {
            json.put("error", errorMessage);
        } catch (JSONException e) {
            //noting
        }
        return json.toString();
    }
}
