package cz.incad.kramerius.audio;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Iplementations is able to forward request 
 * @author pavels
 *
 * @param <T>
 */
public interface AudioHttpRequestForwarder<T> {

 
    /**
     * Forward GET HTTP method
     * @param url 
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract T forwardGetRequest(URL url) throws IOException, URISyntaxException;

    /**
     * Forward HEAD HTTP method 
     * @param url
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public abstract T forwardHeadRequest(URL url) throws IOException, URISyntaxException;

}
