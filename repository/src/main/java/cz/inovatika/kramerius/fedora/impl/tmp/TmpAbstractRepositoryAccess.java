package cz.inovatika.kramerius.fedora.impl.tmp;

import com.google.inject.Inject;
import cz.incad.kramerius.fedora.RepositoryAccess;
import cz.incad.kramerius.fedora.om.repository.impl.AkubraRepositoryImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.dom4j.Namespace;

import javax.annotation.Nullable;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.logging.Logger;

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
/*
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
*/
/*
    //--------- KraRepositoryAPIIMPl
    @javax.inject.Inject
    private RepositoryApiImpl repositoryApi;

    @javax.inject.Inject
    private AggregatedAccessLogs accessLog;


    @Override
    public RepositoryApi getLowLevelApi() {
        return repositoryApi;
    }
*/

}
