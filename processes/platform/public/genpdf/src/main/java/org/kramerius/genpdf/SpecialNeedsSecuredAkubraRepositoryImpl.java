package org.kramerius.genpdf;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.misc.MiscHelper;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.relsext.RelsExtHelper;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.io.InputStream;
import java.util.List;

public class SpecialNeedsSecuredAkubraRepositoryImpl implements SecuredAkubraRepository {


    private final AkubraRepository akubraRepository;
    private final SolrAccess solrAccess;

    @Inject
    public SpecialNeedsSecuredAkubraRepositoryImpl(
            AkubraRepository akubraRepository,
            @Named("new-index") SolrAccess solrAccess
    ) {
        this.akubraRepository = akubraRepository;
        this.solrAccess = solrAccess;
    }

    @Override
    public boolean exists(String s) {
        return akubraRepository.exists(s);
    }

    @Override
    public void ingest(DigitalObject digitalObject) {
        akubraRepository.ingest(digitalObject);
    }

    @Override
    public DigitalObjectWrapper get(String s) {
        return akubraRepository.get(s);
    }

    @Override
    public DigitalObjectWrapper export(String s) {
        return akubraRepository.export(s);
    }

    @Override
    public DigitalObjectMetadata getMetadata(String s) {
        return akubraRepository.getMetadata(s);
    }

    @Override
    public void delete(String s) {
        akubraRepository.delete(s);
    }

    @Override
    public void delete(String s, boolean b, boolean b1) {
        akubraRepository.delete(s, b, b1);
    }

    @Override
    public InputStream marshall(DigitalObject digitalObject) {
        return akubraRepository.marshall(digitalObject);
    }

    @Override
    public DigitalObject unmarshall(InputStream inputStream) {
        return akubraRepository.unmarshall(inputStream);
    }

    @Override
    public void createXMLDatastream(String s, String s1, String s2, InputStream inputStream) {
        akubraRepository.createXMLDatastream(s, s1, s2, inputStream);
    }

    @Override
    public void createXMLDatastream(String s, KnownDatastreams knownDatastreams, String s1, InputStream inputStream) {
        akubraRepository.createXMLDatastream(s, knownDatastreams, s1, inputStream);
    }

    @Override
    public void updateXMLDatastream(String s, String s1, String s2, InputStream inputStream) {
        akubraRepository.updateXMLDatastream(s, s1, s2, inputStream);
    }

    @Override
    public void updateXMLDatastream(String s, KnownDatastreams knownDatastreams, String s1, InputStream inputStream) {
        akubraRepository.updateXMLDatastream(s, knownDatastreams, s1, inputStream);
    }

    @Override
    public void createManagedDatastream(String s, String s1, String s2, InputStream inputStream) {
        akubraRepository.createManagedDatastream(s, s1, s2, inputStream);
    }

    @Override
    public void createManagedDatastream(String s, KnownDatastreams knownDatastreams, String s1, InputStream inputStream) {
        akubraRepository.createManagedDatastream(s, knownDatastreams, s1, inputStream);
    }

    @Override
    public void updateManagedDatastream(String s, String s1, String s2, InputStream inputStream) {
        akubraRepository.updateManagedDatastream(s, s1, s2, inputStream);
    }

    @Override
    public void updateManagedDatastream(String s, KnownDatastreams knownDatastreams, String s1, InputStream inputStream) {
        akubraRepository.updateManagedDatastream(s, knownDatastreams, s1, inputStream);
    }

    @Override
    public void createExternalDatastream(String s, String s1, String s2, String s3) {
        akubraRepository.createExternalDatastream(s, s1, s2, s3);
    }

    @Override
    public void createExternalDatastream(String s, KnownDatastreams knownDatastreams, String s1, String s2) {
        akubraRepository.createExternalDatastream(s, knownDatastreams, s1, s2);
    }

    @Override
    public void updateExternalDatastream(String s, String s1, String s2, String s3) {
        akubraRepository.updateExternalDatastream(s, s1, s2, s3);
    }

    @Override
    public void updateExternalDatastream(String s, KnownDatastreams knownDatastreams, String s1, String s2) {
        akubraRepository.updateExternalDatastream(s, knownDatastreams, s1, s2);
    }

    @Override
    public boolean datastreamExists(String s, String s1) {
        return akubraRepository.datastreamExists(s, s1);
    }

    @Override
    public boolean datastreamExists(String s, KnownDatastreams knownDatastreams) {
        return akubraRepository.datastreamExists(s, knownDatastreams);
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String s, String s1) {
        return akubraRepository.getDatastreamMetadata(s, s1);
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String s, KnownDatastreams knownDatastreams) {
        return akubraRepository.getDatastreamMetadata(s, knownDatastreams);
    }

    @Override
    public DatastreamContentWrapper getDatastreamContent(String s, String s1) {
        return akubraRepository.getDatastreamContent(s, s1);
    }

    @Override
    public DatastreamContentWrapper getDatastreamContent(String s, KnownDatastreams knownDatastreams) {
        return akubraRepository.getDatastreamContent(s, knownDatastreams);
    }

    @Override
    public void deleteDatastream(String s, String s1) {
        akubraRepository.deleteDatastream(s, s1);
    }

    @Override
    public void deleteDatastream(String s, KnownDatastreams knownDatastreams) {
        akubraRepository.deleteDatastream(s, knownDatastreams);
    }

    @Override
    public List<String> getDatastreamNames(String s) {
        return akubraRepository.getDatastreamNames(s);
    }

    @Override
    public ProcessingIndex pi() {
        return akubraRepository.pi();
    }

    @Override
    public RelsExtHelper re() {
        return akubraRepository.re();
    }

    @Override
    public MiscHelper mi() {
        return akubraRepository.mi();
    }

    @Override
    public <T> T doWithLock(String s, LockOperation<T> lockOperation) {
        return akubraRepository.doWithLock(s, lockOperation);
    }

    @Override
    public void shutdown() {
        akubraRepository.shutdown();
    }

    @Override
    public boolean isContentAccessible(String pid) {
        return this.exists(pid);
    }
}
