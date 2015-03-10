package cz.incad.kramerius.impl.fedora;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.google.inject.Inject;

import cz.incad.kramerius.FedoraAccess;

public class Handler extends URLStreamHandler {

    private FedoraAccess fedoraAccess;

    @Inject
    public Handler(FedoraAccess fedoraAccess) {
        super();
        this.fedoraAccess = fedoraAccess;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new FedoraURLConnection(u, fedoraAccess);
    }

    @Override
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        // TODO Auto-generated method stub
        return super.openConnection(u, p);
    }

}
