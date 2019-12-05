package cz.incad.kramerius.audio;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

public abstract class AbstractAudioHttpRequestForwarder<T> implements AudioHttpRequestForwarder<T> {

    protected static final String CONNECTION_RESET = "Connection reset";
    protected static final String BROKEN_PIPE = "Broken pipe";
    protected static int BUFFER_SIZE = 10240;

    protected static final DefaultHttpClient httpClient = initClient();

    static DefaultHttpClient initClient() {
        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager();
        return new DefaultHttpClient(manager);
    }

    public static void destroy() {
        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }

}
