package cz.incad.kramerius.fedora.impl;

import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.repository.RepositoryApiImpl;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.cdk.K7SearchIndexChildrenSupport;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.java.Pair;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.virtualcollections.CollectionException;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;

public class KrameriusRepositoryApiProxyImpl extends KrameriusRepositoryApiImpl {

	
    @Inject
    private OnDemandIngest onDemandIngest;

    @Inject
    private Repository akubra;
    @Inject
    private SolrAccess solrAccess;

//    @Inject
//    public KrameriusRepositoryApiProxyImpl(RepositoryApiImpl repositoryApi, AggregatedAccessLogs accessLog, OnDemandIngest onDemIng, @Named("new-index") SolrAccess solrAccess) {
//        super(repositoryApi, accessLog);
//        this.onDemandIngest = onDemIng;
//        this.akubra = repositoryApi.getStorage();
//        this.solrAccess = solrAccess;
//    }

    @Override
    public RepositoryApi getLowLevelApi() {
        return super.getLowLevelApi();
    }

    @Override
    public boolean isRelsExtAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isRelsExtAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Document getRelsExt(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getRelsExt(pid, namespaceAware);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isModsAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isModsAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Document getMods(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getMods(pid, namespaceAware);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isDublinCoreAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isDublinCoreAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Document getDublinCore(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getDublinCore(pid, namespaceAware);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isOcrTextAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isOcrTextAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getOcrText(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getOcrText(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isOcrAltoAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isOcrAltoAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Document getOcrAlto(String pid, boolean namespaceAware) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getOcrAlto(pid, namespaceAware);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isImgFullAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isImgFullAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getImgFullMimetype(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getImgFullMimetype(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public InputStream getImgFull(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getImgFull(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isImgThumbAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isImgThumbAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getImgThumbMimetype(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getImgThumbMimetype(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public InputStream getImgThumb(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getImgThumb(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isImgPreviewAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isImgPreviewAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getImgPreviewMimetype(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getImgPreviewMimetype(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public InputStream getImgPreview(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getImgPreview(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isAudioMp3Available(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isAudioMp3Available(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getAudioMp3Mimetype(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getAudioMp3Mimetype(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public InputStream getAudioMp3(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getAudioMp3(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isAudioOggAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isAudioOggAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getAudioOggMimetype(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getAudioOggMimetype(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public InputStream getAudioOgg(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getAudioOgg(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isAudioWavAvailable(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isAudioWavAvailable(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getAudioWavMimetype(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getAudioWavMimetype(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public InputStream getAudioWav(String pid) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.getAudioWav(pid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getModel(String objectPid) throws RepositoryException, IOException, SolrServerException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, objectPid);
            return super.getModel(objectPid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Pair<RepositoryApi.Triplet, List<RepositoryApi.Triplet>> getParents(String objectPid) throws RepositoryException, IOException, SolrServerException {
        try {
            // ingest parents - a few
            onDemandIngest.ingestWholePathNecessary(this.akubra, objectPid);
            return super.getParents(objectPid);
        } catch (CollectionException | LexerException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> getChildren(String objectPid) throws RepositoryException, IOException, SolrServerException {
        try {
            // problematic part - all children in repo
            onDemandIngest.ingestIfNecessary(this.akubra, objectPid);

            // prime - model
            List<RepositoryApi.Triplet> ownChildrenTriplets = new ArrayList<>();
            List<RepositoryApi.Triplet> fosterChildrenTriplets = new ArrayList<>();

			K7SearchIndexChildrenSupport.ownChildrenAndFosterChildren(this.solrAccess, objectPid, null, ownChildrenTriplets, fosterChildrenTriplets);

            return new Pair<>(ownChildrenTriplets,fosterChildrenTriplets);

        } catch (CollectionException | LexerException | JAXBException | TransformerException | MigrateSolrIndexException | SAXException
				| InterruptedException | BrokenBarrierException | XPathExpressionException |ParserConfigurationException e) {
            throw new RepositoryException(e);
		}
    }

    // used in admin
    @Override
    public List<String> getPidsOfItemsInCollection(String collectionPid) throws RepositoryException, IOException, SolrServerException {
        return super.getPidsOfItemsInCollection(collectionPid);
    }

    @Override
    public List<String> getPidsOfCollectionsContainingItem(String itemPid) throws RepositoryException, IOException, SolrServerException {
        return super.getPidsOfCollectionsContainingItem(itemPid);
    }
    //

    @Override
    public void updateRelsExt(String pid, Document relsExtDoc) throws IOException, RepositoryException {
        super.updateRelsExt(pid, relsExtDoc);
    }

    @Override
    public void updateMods(String pid, Document modsDoc) throws IOException, RepositoryException {
        super.updateMods(pid, modsDoc);
    }

    @Override
    public void updateDublinCore(String pid, Document dcDoc) throws IOException, RepositoryException {
        super.updateDublinCore(pid, dcDoc);
    }

    @Override
    public boolean isPidAvailable(String pid) throws IOException, RepositoryException {
        // problematic part - all children in repo
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isPidAvailable(pid);
        } catch (CollectionException |  LexerException | JAXBException |TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isStreamAvailable(String pid, String dsId) throws IOException, RepositoryException {
        try {
            onDemandIngest.ingestIfNecessary(this.akubra, pid);
            return super.isStreamAvailable(pid, dsId);
        } catch (CollectionException |  LexerException | JAXBException |TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
    }
}
