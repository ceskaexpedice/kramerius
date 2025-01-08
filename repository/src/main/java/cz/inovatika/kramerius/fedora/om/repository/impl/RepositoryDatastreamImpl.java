package cz.inovatika.kramerius.fedora.om.repository.impl;

import com.qbizm.kramerius.imp.jaxb.DatastreamType;
import cz.inovatika.kramerius.fedora.om.repository.RepositoryDatastream;
import cz.inovatika.kramerius.fedora.RepositoryException;
import org.w3c.dom.Document;

import java.io.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by pstastny on 10/13/2017.
 */
class RepositoryDatastreamImpl implements RepositoryDatastream {

    private static final Logger LOGGER = Logger.getLogger(RepositoryDatastreamImpl.class.getName());

    private final AkubraDOManager manager;
    private final DatastreamType datastream;

    private final String name;
    private final Type type;

    private String transactionId;


    RepositoryDatastreamImpl(AkubraDOManager manager, DatastreamType datastream, String name, Type type) {
        super();
        this.manager = manager;
        this.datastream = datastream;
        this.name = name;
        this.type = type;
    }

    RepositoryDatastreamImpl(AkubraDOManager manager, DatastreamType datastream, String name) {
        this(manager,datastream, name, Type.DIRECT);
    }

    @Override
    public String getName() throws RepositoryException {
        return this.name;
    }

    @Override
    public InputStream getContent() throws RepositoryException {
        try {
            return RepositoryUtils.getStreamContent(RepositoryUtils.getLastStreamVersion(datastream), manager);
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Document getMetadata() throws RepositoryException {
        return null;
    }

    @Override
    public String getMimeType() throws RepositoryException {
        return RepositoryUtils.getLastStreamVersion(datastream).getMIMETYPE();
    }

    @Override
    public Type getStreamType() throws RepositoryException {
        return this.type;
    }

    @Override
    public Date getLastModified() throws RepositoryException {
        return RepositoryUtils.getLastStreamVersion(datastream).getCREATED().toGregorianCalendar().getTime();
    }

    @Override
    public void updateSPARQL(String sparql) throws RepositoryException {

    }
}
