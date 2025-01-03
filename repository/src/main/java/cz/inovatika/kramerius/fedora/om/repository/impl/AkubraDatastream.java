package cz.inovatika.kramerius.fedora.om.repository.impl;

import com.qbizm.kramerius.imp.jaxb.DatastreamType;
import cz.inovatika.kramerius.fedora.om.repository.RepositoryDatastream;
import cz.inovatika.kramerius.fedora.om.repository.RepositoryException;
import cz.inovatika.kramerius.fedora.utils.AkubraUtils;
import org.w3c.dom.Document;

import java.io.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by pstastny on 10/13/2017.
 */
public class AkubraDatastream implements RepositoryDatastream {

    public static final Logger LOGGER = Logger.getLogger(AkubraDatastream.class.getName());

    private final AkubraDOManager manager;
    private final DatastreamType datastream;

    private final String name;
    private final Type type;

    private String transactionId;


    public AkubraDatastream(AkubraDOManager manager, DatastreamType datastream, String name, Type type) {
        super();
        this.manager = manager;
        this.datastream = datastream;
        this.name = name;
        this.type = type;
    }

    public AkubraDatastream(AkubraDOManager manager, DatastreamType datastream, String name) {
        this(manager,datastream, name, Type.DIRECT);
    }



    @Override
    public String getName() throws RepositoryException {
        return this.name;
    }

    public InputStream getContent() throws RepositoryException {
        try {
            return AkubraUtils.getStreamContent(AkubraUtils.getLastStreamVersion(datastream), manager);
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
        return AkubraUtils.getLastStreamVersion(datastream).getMIMETYPE();
    }



    @Override
    public Type getStreamType() throws RepositoryException {
        return this.type;
    }

    @Override
    public Date getLastModified() throws RepositoryException {
        return AkubraUtils.getLastStreamVersion(datastream).getCREATED().toGregorianCalendar().getTime();
    }

    @Override
    public void updateSPARQL(String sparql) throws RepositoryException {

    }
}
