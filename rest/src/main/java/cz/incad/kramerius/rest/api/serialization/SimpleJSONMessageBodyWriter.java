package cz.incad.kramerius.rest.api.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

import com.google.inject.Singleton;

import cz.incad.kramerius.utils.IOUtils;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Provider
public class SimpleJSONMessageBodyWriter implements MessageBodyWriter<JSONObject> {

    @Override
    public long getSize(JSONObject obj, Class<?> arg1, Type arg2,
            Annotation[] arg3, MediaType arg4) {
        byte[] bytes = getBytes(obj);
        return bytes.length;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type arg1, Annotation[] arg2,
            MediaType arg3) {
        return type == JSONObject.class;
    }

    @Override
    public void writeTo(JSONObject obj, Class<?> arg1, Type arg2,
            Annotation[] arg3, MediaType arg4,
            MultivaluedMap<String, Object> arg5, OutputStream os)
            throws IOException, WebApplicationException {
        byte[] bytes = getBytes(obj);
        IOUtils.copyStreams(new ByteArrayInputStream(bytes), os);
    }

    protected byte[] getBytes(JSONObject obj) {
        String string = obj.toString();
        byte[] bytes = string.getBytes(Charset.forName("UTF-8"));
        return bytes;
    }

}
