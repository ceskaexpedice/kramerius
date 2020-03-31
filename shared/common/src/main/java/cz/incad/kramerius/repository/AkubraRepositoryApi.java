package cz.incad.kramerius.repository;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.om.impl.AkubraDOManager;
import cz.incad.kramerius.fedora.om.impl.AkubraRepository;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.dom4j.Document;
import org.dom4j.Node;
import org.ehcache.CacheManager;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;

public class AkubraRepositoryApi implements RepositoryApi {

    private final AkubraRepository akubraRepository;
    private final Unmarshaller digitalObjectUnmarshaller;

    @Inject
    public AkubraRepositoryApi(KConfiguration configuration, ProcessingIndexFeeder processingIndexFeeder, @Named("akubraCacheManager") CacheManager cacheManager) throws RepositoryException {
        try {
            AkubraDOManager akubraDOManager = new AkubraDOManager(configuration, cacheManager);
            this.akubraRepository = (AkubraRepository) AkubraRepository.build(processingIndexFeeder, akubraDOManager);
            this.digitalObjectUnmarshaller = JAXBContext.newInstance(DigitalObject.class).createUnmarshaller();
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (JAXBException e) {
            throw new RepositoryException("Error initializing JAXB unmarshaller for " + DigitalObject.class.getName());
        }
    }

    @Override
    public void ingestObject(Document foxmlDoc) throws RepositoryException {
        try {
            DigitalObject digitalObject = (DigitalObject) digitalObjectUnmarshaller.unmarshal(new StringReader(foxmlDoc.asXML()));
            akubraRepository.ingestObject(digitalObject);
        } catch (JAXBException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean objectExists(String pid) throws RepositoryException {
        return akubraRepository.objectExists(pid);
    }

    @Override
    public String getObjectProperty(String pid, String propertyName) throws IOException, RepositoryException {
        Document objectFoxml = getObjectFoxml(pid);
        return objectFoxml == null ? null : extractProperty(objectFoxml, propertyName);
    }

    @Override
    public Document getObjectFoxml(String pid) throws RepositoryException, IOException {
        RepositoryObject object = akubraRepository.getObject(pid);
        return Utils.inputstreamToDocument(object.getFoxml(), true);
    }

    @Override
    public boolean datastreamExists(String pid, String dsId) throws RepositoryException, IOException {
        RepositoryObject object = akubraRepository.getObject(pid);
        return object == null ? false : object.streamExists(dsId);
    }

    @Override
    public Document getLatestVersionOfInlineXmlDatastream(String pid, String dsId) throws RepositoryException, IOException {
        RepositoryObject object = akubraRepository.getObject(pid);
        if (object.streamExists(dsId)) {
            RepositoryDatastream stream = object.getStream(dsId);
            return Utils.inputstreamToDocument(stream.getContent(), true);
        } else {
            return null;
        }
    }

    @Override
    public void deleteObject(String pid) throws RepositoryException {
        akubraRepository.deleteobject(pid);
    }

    private String extractProperty(Document foxmlDoc, String name) {
        Node node = Dom4jUtils.buildXpath("/foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='" + name + "']/@VALUE").selectSingleNode(foxmlDoc);
        return node == null ? null : Dom4jUtils.toStringOrNull(node);
    }
}
