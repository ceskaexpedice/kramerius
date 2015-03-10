package cz.incad.kramerius.impl.fedora;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import cz.incad.kramerius.FedoraAccess;

public class FedoraURLConnection extends URLConnection {

    public static final String IMG_FULL = "IMG_FULL";
    public static final String IMG_THUMB = "IMG_THUMB";

    private FedoraAccess fedoraAccess;

    FedoraURLConnection(URL url, FedoraAccess fedoraAccess) {
        super(url);
        this.fedoraAccess = fedoraAccess;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        String path = getURL().getPath();
        String pid = null;
        String stream = null;
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        if (tokenizer.hasMoreTokens()) {
            pid = tokenizer.nextToken();
        }
        if (tokenizer.hasMoreTokens()) {
            stream = tokenizer.nextToken();
        }
        if (stream.equals(IMG_FULL)) {
            return this.fedoraAccess.getImageFULL(pid);
        } else if (stream.equals(IMG_THUMB)) {
            return this.fedoraAccess.getSmallThumbnail(pid);
        } else {
            return this.fedoraAccess.getDataStream(pid, stream);
        }
    }

    @Override
    public void connect() throws IOException {
    }
}
