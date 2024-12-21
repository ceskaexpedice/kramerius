package cz.incad.kramerius.fedora.impl.tmp;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.google.inject.name.Named;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.fedora.RepositoryAccess;
import cz.incad.kramerius.fedora.om.repository.RepositoryException;
import cz.incad.kramerius.fedora.om.repository.impl.AkubraDOManager;
import cz.incad.kramerius.fedora.om.repository.impl.AkubraRepositoryImpl;
import cz.incad.kramerius.fedora.om.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import org.dom4j.Namespace;
import org.ehcache.CacheManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.fedora.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.fedora.utils.pid.LexerException;

public abstract class TmpAbstractRepositoryAccess implements RepositoryAccess {
    //----------------------
    public static final Logger LOGGER = Logger.getLogger(RepositoryApi.class.getName());

    private static final Namespace NS_FOXML = new Namespace("foxml", "info:fedora/fedora-system:def/foxml#");
    private final AkubraRepositoryImpl akubraRepositoryImpl;
    private final Unmarshaller digitalObjectUnmarshaller;
    //-------------------------------------------------------------------------

    public static final Logger LOGGER = Logger.getLogger(TmpAbstractRepositoryAccess.class.getName());
    protected XPathFactory xPathFactory;
    protected KConfiguration configuration = KConfiguration.getInstance();

    @Inject
    public TmpAbstractRepositoryAccess(@Nullable StatisticsAccessLog accessLog)
            throws IOException {
        super();
        this.xPathFactory = XPathFactory.newInstance();
    }



    @Override
    public String findFirstViewablePid(String pid) throws IOException {
        final List<String> foundPids = new ArrayList<String>();
        try {
            processSubtree(makeSureObjectPid(pid), new TreeNodeProcessor() {
                boolean breakProcess = false;
                int previousLevel = 0;
    
                @Override
                public boolean breakProcessing(String pid, int level) {
                    return breakProcess;
                }
    
                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }
    
                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try {
                        if (previousLevel < level || level == 0) {
                            if (TmpAbstractRepositoryAccess.this.isImageFULLAvailable(pid)) {
                                foundPids.add(pid);
                                breakProcess = true;
                            }
                        } else if (previousLevel > level) {
                            breakProcess = true;
                        } else if ((previousLevel == level) && (level != 0)) {
                            breakProcess = true;
                        }
                        previousLevel = level;
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    
        return foundPids.isEmpty() ? null : foundPids.get(0);
    }

    @Override
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException {
        try {
            pid = makeSureObjectPid(pid);
            Document relsExt = null;
            try {
                // should be from
                if (isStreamAvailable(pid, FedoraUtils.RELS_EXT_STREAM)) {
                    relsExt = getRelsExt(pid);
                } else {
                    LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + ")");
                }
            } catch (Exception ex) {
                LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + "):" + ex);
            }
            if (!processor.skipBranch(pid, 0)) {
                processSubtreeInternal(pid, relsExt, processor, 0, new Stack<String>());
            }
        } catch (LexerException e) {
            LOGGER.warning("Error in pid: " + pid);
            throw new ProcessSubtreeException(e);
        } catch (XPathExpressionException e) {
            throw new ProcessSubtreeException(e);
        }
    }


    // TODO something more generic like getFieldFromStream
    @Override
    public String getDonator(String pid) throws IOException {
        return getDonator(getRelsExt(pid));
    }

    // TODO something more generic like getFieldFromStream
    @Override
    public String getKrameriusModelName(String pid) throws IOException {
        return getKrameriusModelName(getRelsExt(pid));
    }

    // TODO something more generic like getFieldFromStream
    @Override
    public List<Element> getPages(String pid, boolean deep) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getPages(pid, relsExt.getDocumentElement());
    }


    // TODO something more generic like getFieldFromStream
    @Override
    public String getFirstItemPid(String pid) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getFirstItemPid(relsExt);
    }

    // TODO something more generic like getFieldFromStream
    @Override
    public String getFirstVolumePid(String pid) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getFirstVolumePid(relsExt);
    }

    /*
    @Override
    public boolean isImageFULLAvailable(String pid) throws IOException {
        try {
            return isStreamAvailable(makeSureObjectPid(pid), FedoraUtils.IMG_FULL_STREAM);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }*/

    @Override
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException {
        try {
            String pid = pids.get(pids.size() - 1);
            pid = makeSureObjectPid(pid);
            if (isImageFULLAvailable(pid)) {
                return true;
            }
            Document relsExt = getRelsExt(pid);
            Element descEl = XMLUtils.findElement(relsExt.getDocumentElement(), "Description",
                    FedoraNamespaces.RDF_NAMESPACE_URI);
            List<Element> els = XMLUtils.getElements(descEl);
            for (Element el : els) {
                if (getTreePredicates().contains(el.getLocalName())) {
                    if (el.hasAttribute("rdf:resource")) {
                        pid = el.getAttributes().getNamedItem("rdf:resource").getNodeValue();
                        pids.add(pid);
                        models.add(getKrameriusModelName(pid));
                        if (getFirstViewablePath(pids, models)) {
                            return true;
                        } else {
                            pids.remove(pids.size() - 1);
                            models.remove(pids.size() - 1);
                        }
                    }
                }
            }
            return false;
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    /*
    @Override
    public List<String> getModelsOfRel(Document relsExt) {
        try {
            throw new UnsupportedOperationException("still unsupported");
            // Element foundElement =
            // XMLUtils.findElement(relsExt.getDocumentElement(), "hasModel",
            // FedoraNamespaces.FEDORA_MODELS_URI);
            // if (foundElement != null) {
            // String sform =
            // foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI,
            // "resource");
            // PIDParser pidParser = new PIDParser(sform);
            // pidParser.disseminationURI();
            // ArrayList<String> model =
            // RelsExtModelsMap.getModelsOfRelation(pidParser.getObjectId());
            // return model;
            // } else {
            // throw new IllegalArgumentException("cannot find model of ");
            // }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }*/

    /*
    @Override
    public List<String> getModelsOfRel(String pid) throws IOException {
        return getModelsOfRel(getRelsExt(pid));
    }*/
// RepoApiImpl-------------------------------------------------------------------------------------------
@Inject
public RepositoryApiImpl(ProcessingIndexFeeder processingIndexFeeder, @Named("akubraCacheManager") CacheManager cacheManager) throws RepositoryException {
    try {
        AkubraDOManager akubraDOManager = new AkubraDOManager(cacheManager);
        this.akubraRepositoryImpl = (AkubraRepositoryImpl) AkubraRepositoryImpl.build(processingIndexFeeder, akubraDOManager);
        this.digitalObjectUnmarshaller = JAXBContext.newInstance(DigitalObject.class).createUnmarshaller();
    } catch (IOException e) {
        throw new RepositoryException(e);
    } catch (JAXBException e) {
        throw new RepositoryException("Error initializing JAXB unmarshaller for " + DigitalObject.class.getName());
    }
}


    //--------- KraRepositoryAPIIMPl
    @javax.inject.Inject
    private RepositoryApiImpl repositoryApi;

    @javax.inject.Inject
    private AggregatedAccessLogs accessLog;


    @Override
    public RepositoryApi getLowLevelApi() {
        return repositoryApi;
    }


}
