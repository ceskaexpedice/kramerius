package cz.incad.kramerius.fedora.utils;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.google.inject.Inject;

import cz.incad.kramerius.fedora.RepositoryAccess;

public class Handler extends URLStreamHandler {

    private RepositoryAccess fedoraAccess;

    @Inject
    public Handler(RepositoryAccess fedoraAccess) {
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
