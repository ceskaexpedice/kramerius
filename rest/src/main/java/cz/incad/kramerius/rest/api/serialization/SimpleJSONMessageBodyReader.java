package cz.incad.kramerius.rest.api.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

import com.google.inject.Singleton;

import cz.incad.kramerius.utils.IOUtils;

@Singleton
@Consumes(MediaType.APPLICATION_JSON)
@Provider
public class SimpleJSONMessageBodyReader implements MessageBodyReader<JSONObject>{

    @Override
    public boolean isReadable(Class<?> type, Type arg1, Annotation[] arg2,
            MediaType arg3) {
        return type == JSONObject.class;
    }

    @Override
    public JSONObject readFrom(Class<JSONObject> arg0, Type arg1,
            Annotation[] arg2, MediaType arg3,
            MultivaluedMap<String, String> arg4, InputStream is)
            throws IOException, WebApplicationException {
        String readAsString = IOUtils.readAsString(is, Charset.forName("UTF-8"), false);
        JSONObject json = new JSONObject(readAsString);
        return json;
    }

    
}
