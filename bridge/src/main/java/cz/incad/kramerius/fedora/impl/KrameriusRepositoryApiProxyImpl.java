package cz.incad.kramerius.fedora.impl;

import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.repository.RepositoryApiImpl;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.IterationUtils;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.java.Pair;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.virtualcollections.CollectionException;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.stream.Collectors;

public class KrameriusRepositoryApiProxyImpl extends KrameriusRepositoryApiImpl {

    private OnDemandIngest onDemandIngest;
    private Repository akubra;
    private SolrAccess solrAccess;

    @Inject
    public KrameriusRepositoryApiProxyImpl(RepositoryApiImpl repositoryApi, AggregatedAccessLogs accessLog, OnDemandIngest onDemIng, Repository akubra, @Named("new-index") SolrAccess solrAccess) {
        super(repositoryApi, accessLog);
        this.onDemandIngest = onDemIng;
        this.akubra = akubra;
        this.solrAccess = solrAccess;
    }

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

            // prime prime - model
            List<RepositoryApi.Triplet> ownChildrenTriplets = new ArrayList<>();
            List<RepositoryApi.Triplet> fosterChildrenTriplets = new ArrayList<>();

            ownChildrenAndFosterChildren(this.solrAccess, objectPid, ownChildrenTriplets, fosterChildrenTriplets);

            //return super.getChildren(objectPid);
        } catch (CollectionException | LexerException | MigrateSolrIndexException | SAXException | BrokenBarrierException | ParserConfigurationException |  InterruptedException | JAXBException | TransformerException | XPathExpressionException e) {
            throw new RepositoryException(e);
        }
        return null;
    }

    static void ownChildrenAndFosterChildren(SolrAccess solrAccess, String objectPid, List<RepositoryApi.Triplet> ownChildrenTriplets, List<RepositoryApi.Triplet> fosterChildrenTriplets) throws IOException, ParserConfigurationException, MigrateSolrIndexException, SAXException, InterruptedException, BrokenBarrierException {
        org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(objectPid);
        List<Element> parentModel = XMLUtils.getElementsRecursive(solrDataByPid.getDocumentElement(), (elm) -> {
            String name = elm.getAttribute("name");
            return (elm.getNodeName().equals("str") && name.equals("model"));
        });

        IterationUtils.IterationContext ownParentContext = new IterationUtils.IterationContext("pid", 100, Arrays.asList("pid","own_parent","model", "foster_parents.pids"));
        String ownparentQuery =  "own_parent.pid:" +URLEncoder.encode("\""+objectPid+"\"", "UTF-8");
        IterationUtils.cursorIteration(solrAccess, ownparentQuery, (results,iterationToken)-> {
            List<Map<String, Object>> collectedDocuments = resultsToMap(results);
            collectedDocuments.stream().filter(hmap -> {
                return (hmap.containsKey("pid") && !hmap.get("pid").equals(objectPid) && hmap.containsKey("model"));
            }).forEach(hmap -> {
                Object model = hmap.get("model");
                OwnRelationsMapping mapping = OwnRelationsMapping.find(model.toString());
                String relationName = mapping.relation().toString();
                ownChildrenTriplets.add(new RepositoryApi.Triplet(objectPid, relationName, hmap.get("pid").toString()));
            });
        }, ()->{}, ownParentContext);

        if (parentModel != null) {
            IterationUtils.IterationContext fosterContext = new IterationUtils.IterationContext("pid", 100, Arrays.asList("pid","own_parent","model", "foster_parents.pids"));
            String fosterQuery = "foster_parents.pids:"+ URLEncoder.encode("\""+objectPid+"\"", "UTF-8");;
            IterationUtils.cursorIteration(solrAccess, fosterQuery, (results,iterationToken)-> {
                List<Map<String, Object>> collectedDocuments = resultsToMap(results);
                collectedDocuments.stream().filter(hmap -> {
                    return (hmap.containsKey("pid") && !hmap.get("pid").equals(objectPid) && hmap.containsKey("model"));
                }).forEach(hmap -> {
                    Object model = hmap.get("model");
                    // pokud je parent article nebo internal part, pak
                    FosterRelationsMapping mapping = FosterRelationsMapping.find(model.toString());
                    fosterChildrenTriplets.add(new RepositoryApi.Triplet(objectPid, mapping.relation(parentModel.get(0).getTextContent()).toString(), hmap.get("pid").toString()));
                });

            }, ()->{}, fosterContext);
        }
    }


    static List<Map<String, Object>> resultsToMap(Element results) {


        return XMLUtils.getElementsRecursive(results, (elm) -> {
                return elm.getNodeName().equals("doc");
            }).stream().map(doc -> {
                Map<String, Object> hashMapDocument = new HashMap<>();
                XMLUtils.getElements(doc, (field) -> {
                    String name = field.getAttribute("name");
                    if (name.equals("foster_parents.pids")) {
                        List<String> values = XMLUtils.getElements(field).stream().map(arrayElm -> {
                            return arrayElm.getTextContent();
                        }).collect(Collectors.toList());
                        hashMapDocument.put("foster_parents.pids", values);
                    } else {
                        hashMapDocument.put(name, field.getTextContent());
                    }
                    return false;
                });
                return hashMapDocument;
        }).collect(Collectors.toList());
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
        return super.isPidAvailable(pid);
    }

    @Override
    public boolean isStreamAvailable(String pid, String dsId) throws IOException, RepositoryException {
        return super.isStreamAvailable(pid, dsId);
    }
}
