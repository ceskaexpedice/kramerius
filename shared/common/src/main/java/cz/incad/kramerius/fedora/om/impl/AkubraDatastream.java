package cz.incad.kramerius.fedora.om.impl;

import com.qbizm.kramerius.imp.jaxb.DatastreamType;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.NotFoundInRepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static cz.incad.kramerius.fedora.utils.Fedora4Utils.endpoint;

/**
 * Created by pstastny on 10/13/2017.
 */
public class AkubraDatastream implements RepositoryDatastream {

    public static enum Type {
        DIRECT,
        INDIRECT;
    }

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
    public Date getLastModified() throws RepositoryException {
        return AkubraUtils.getLastStreamVersion(datastream).getCREATED().toGregorianCalendar().getTime();
    }

    @Override
    public void updateSPARQL(String sparql) throws RepositoryException {

    }
}
