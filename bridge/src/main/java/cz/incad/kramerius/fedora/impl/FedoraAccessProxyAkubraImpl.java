package cz.incad.kramerius.fedora.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.fedora.AbstractFedoraAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.CollectionException;
import org.ceskaexpedice.akubra.utils.pid.LexerException;
import org.w3c.dom.Document;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

public class FedoraAccessProxyAkubraImpl extends AbstractFedoraAccess {


    public static final Logger LOGGER = Logger.getLogger(FedoraAccessProxyAkubraImpl.class.getName());

    private FedoraAccess akubra;
    private SolrAccess solrAccess;
    private Client client;
    OnDemandIngest onDemandIngest;



    @Inject
    public FedoraAccessProxyAkubraImpl(@Nullable AggregatedAccessLogs accessLog, @Named("akubraFedoraAccess")FedoraAccess acc, @Named("new-index")SolrAccess solrAccess, Provider<HttpServletRequest> provider, OnDemandIngest onDemIng) throws IOException {
        super( accessLog);
        this.akubra = acc;
        this.solrAccess = solrAccess;
        this.client = Client.create();
        this.onDemandIngest = onDemIng;
    }


    @Override
    public Document getRelsExt(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return akubra.getRelsExt(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }


    @Override
    public Document getBiblioMods(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getBiblioMods(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getDC(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getDC(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getSmallThumbnail(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getSmallThumbnailProfile(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getSmallThumbnailProfile(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getSmallThumbnailMimeType(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.isFullthumbnailAvailable(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getFullThumbnail(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getFullThumbnailMimeType(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getImageFULL(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getImageFULLProfile(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getImageFULLProfile(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getImageFULLMimeType(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.isStreamAvailable(pid, streamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.isObjectAvailable(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public boolean isContentAccessible(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.isContentAccessible(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Repository getInternalAPI() throws RepositoryException {
        return this.akubra.getInternalAPI();
    }

    @Override
    public Repository getTransactionAwareInternalAPI() throws RepositoryException {
        return this.akubra.getTransactionAwareInternalAPI();
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getDataStream(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public void observeStreamHeaders(String pid, String datastreamName, StreamHeadersObserver streamObserver) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            this.akubra.observeStreamHeaders(pid,datastreamName, streamObserver);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }

    }

    @Override
    public String getExternalStreamURL(String pid, String datastreamName) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getExternalStreamURL(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getDataStreamXml(String pid, String datastreamName) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getDataStreamXml(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getDataStreamXmlAsDocument(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getMimeTypeForStream(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getFedoraVersion() throws IOException {
        return this.akubra.getFedoraVersion();
    }

    @Override
    public Document getStreamProfile(String pid, String stream) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getStreamProfile(pid,stream);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getObjectProfile(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getObjectProfile(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getFedoraDataStreamsList(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getFedoraDataStreamsList(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getFedoraDataStreamsListAsDocument(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getFedoraDataStreamsListAsDocument(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Date getStreamLastmodifiedFlag(String pid, String stream) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getStreamLastmodifiedFlag(pid,stream);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Date getObjectLastmodifiedFlag(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getObjectLastmodifiedFlag(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public List<Map<String, String>> getStreamsOfObject(String pid) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getStreamsOfObject(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getFoxml(String pid, boolean archive) throws IOException {
        try {
            onDemandIngest.ingestIfNecessary(this.getInternalAPI(), pid);
            return this.akubra.getFoxml(pid, archive);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }


    /*
    public synchronized Repository getSynchronizedInternalRepository() throws RepositoryException {
        if (this.internalRepository  == null ) {
            this.internalRepository = new SynchronizedRepository(this.akubra.getInternalAPI());
        }
        return this.internalRepository;
    }*/


}
