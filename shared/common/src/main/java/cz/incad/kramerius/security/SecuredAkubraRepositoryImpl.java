/*
 * Copyright (C) 2025 Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.security;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.CollectionPidUtils;
import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.misc.MiscHelper;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.akubra.relsext.RelsExtHelper;
import org.ceskaexpedice.fedoramodel.DigitalObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecuredAkubraRepositoryImpl implements AkubraRepository {

    private final AkubraRepository akubraRepository;
    private final SolrAccess solrAccess;
    private final RightsResolver rightsResolver;

    @Inject
    public SecuredAkubraRepositoryImpl(AkubraRepository akubraRepository, @Named("new-index") SolrAccess solrAccess, RightsResolver rightsResolver) {
        this.akubraRepository = akubraRepository;
        this.solrAccess = solrAccess;
        this.rightsResolver = rightsResolver;
    }

    @Override
    public boolean exists(String pid) {
        return akubraRepository.exists(pid);
    }

    @Override
    public void ingest(DigitalObject digitalObject) {
        akubraRepository.ingest(digitalObject);
    }

    @Override
    public DigitalObjectWrapper get(String pid) {
        return getterHelper(pid, false);
    }

    @Override
    public DigitalObjectWrapper export(String pid) {
        return getterHelper(pid, true);
    }

    @Override
    public ObjectProperties getProperties(String pid) {
        return akubraRepository.getProperties(pid);
    }

    @Override
    public void delete(String pid) {
        akubraRepository.delete(pid);
    }

    @Override
    public void delete(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget) {
        akubraRepository.delete(pid, deleteDataOfManagedDatastreams, deleteRelationsWithThisAsTarget);
    }

    @Override
    public InputStream marshall(DigitalObject obj) {
        return akubraRepository.marshall(obj);
    }

    @Override
    public DigitalObject unmarshall(InputStream inputStream) {
        return akubraRepository.unmarshall(inputStream);
    }

    @Override
    public void createXMLDatastream(String pid, String dsId, String mimeType, InputStream xmlContent) {
        akubraRepository.createXMLDatastream(pid, dsId, mimeType, xmlContent);
    }

    @Override
    public void createXMLDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream xmlContent) {
        akubraRepository.createXMLDatastream(pid, dsId, mimeType, xmlContent);
    }

    @Override
    public void updateXMLDatastream(String pid, String dsId, String mimeType, InputStream xmlContent) {
        akubraRepository.updateXMLDatastream(pid, dsId, mimeType, xmlContent);
    }

    @Override
    public void updateXMLDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream xmlContent) {
        akubraRepository.updateXMLDatastream(pid, dsId, mimeType, xmlContent);
    }

    @Override
    public void createManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent) {
        akubraRepository.createManagedDatastream(pid, dsId, mimeType, binaryContent);
    }

    @Override
    public void createManagedDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent) {
        akubraRepository.createManagedDatastream(pid, dsId, mimeType, binaryContent);
    }

    @Override
    public void updateManagedDatastream(String pid, String dsId, String mimeType, InputStream binaryContent) {
        akubraRepository.updateManagedDatastream(pid, dsId, mimeType, binaryContent);
    }

    @Override
    public void updateManagedDatastream(String pid, KnownDatastreams dsId, String mimeType, InputStream binaryContent) {
        akubraRepository.updateManagedDatastream(pid, dsId, mimeType, binaryContent);
    }

    @Override
    public void createRedirectedDatastream(String pid, String dsId, String url, String mimeType) {
        akubraRepository.createRedirectedDatastream(pid, dsId, url, mimeType);
    }

    @Override
    public void createRedirectedDatastream(String pid, KnownDatastreams dsId, String url, String mimeType) {
        akubraRepository.createRedirectedDatastream(pid, dsId, url, mimeType);
    }

    @Override
    public void updateRedirectedDatastream(String pid, String dsId, String url, String mimeType) {
        akubraRepository.updateRedirectedDatastream(pid, dsId, url, mimeType);
    }

    @Override
    public void updateRedirectedDatastream(String pid, KnownDatastreams dsId, String url, String mimeType) {
        akubraRepository.updateRedirectedDatastream(pid, dsId, url, mimeType);
    }

    @Override
    public boolean datastreamExists(String pid, String dsId) {
        return akubraRepository.datastreamExists(pid, dsId);
    }

    @Override
    public boolean datastreamExists(String pid, KnownDatastreams dsId) {
        return akubraRepository.datastreamExists(pid, dsId);
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String pid, String dsId) {
        return akubraRepository.getDatastreamMetadata(pid, dsId);
    }

    @Override
    public DatastreamMetadata getDatastreamMetadata(String pid, KnownDatastreams dsId) {
        return akubraRepository.getDatastreamMetadata(pid, dsId);
    }

    @Override
    public DatastreamContentWrapper getDatastreamContent(String pid, String dsId) {
        try {
            if (KnownDatastreams.IMG_PREVIEW.toString().equals(dsId)) {
                return getFullThumbnail(pid);
            }
            if (isDefaultSecuredStream(dsId)) {
                ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
                paths = ensurePidPathForUnindexedObjects(pid, paths);
                for (int i = 0; i < paths.length; i++) {
                    if (this.rightsResolver.isActionAllowed(SecuredActions.A_READ.getFormalName(), pid, dsId, paths[i]).flag()) {
                        return akubraRepository.getDatastreamContent(pid, dsId);
                    }
                }
                throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.A_READ, pid, dsId));
            } else {
                String[] securedStreamsExtension = KConfiguration.getInstance().getSecuredAditionalStreams();
                int indexOf = Arrays.asList(securedStreamsExtension).indexOf(dsId);
                if (indexOf >= 0) {
                    ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid + "/" + dsId);
                    paths = ensurePidPathForUnindexedObjects(pid, paths);
                    for (int i = 0; i < paths.length; i++) {
                        if (this.rightsResolver.isActionAllowed(SecuredActions.A_READ.getFormalName(), pid, dsId, paths[i]).flag()) {
                            return akubraRepository.getDatastreamContent(pid, dsId);
                        }
                    }
                    throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.A_READ, pid, dsId));
                } else {
                    return akubraRepository.getDatastreamContent(pid, dsId);
                }
            }
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public DatastreamContentWrapper getDatastreamContent(String pid, KnownDatastreams dsId) {
        return getDatastreamContent(pid, dsId.toString());
    }

    @Override
    public void deleteDatastream(String pid, String dsId) {
        akubraRepository.deleteDatastream(pid, dsId);
    }

    @Override
    public void deleteDatastream(String pid, KnownDatastreams dsId) {
        akubraRepository.deleteDatastream(pid, dsId);
    }

    @Override
    public List<String> getDatastreamNames(String pid) {
        return akubraRepository.getDatastreamNames(pid);
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
    public <T> T doWithReadLock(String pid, LockOperation<T> operation) {
        return akubraRepository.doWithReadLock(pid, operation);
    }

    @Override
    public <T> T doWithWriteLock(String pid, LockOperation<T> operation) {
        return akubraRepository.doWithWriteLock(pid, operation);
    }

    @Override
    public void shutdown() {
        akubraRepository.shutdown();
    }

    // TODO AK_NEW contentAccessible
    private boolean isContentAccessible(String pid) throws IOException {
        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
        paths = ensurePidPathForUnindexedObjects(pid, paths);
        for (ObjectPidsPath path : paths) {
            if (this.rightsResolver.isActionAllowed(SecuredActions.A_READ.getFormalName(), pid, FedoraUtils.IMG_FULL_STREAM, path).flag()) {
                return true;
            }
        }
        return false;
    }

    private ObjectPidsPath[] ensurePidPathForUnindexedObjects(String pid, ObjectPidsPath[] paths) {
        if (paths.length == 0) {
            paths = getPaths(pid);
        }
        return paths;
    }

    private static boolean isDefaultSecuredStream(String streamName) {
        return KnownDatastreams.IMG_FULL.toString().equals(streamName)
                || KnownDatastreams.IMG_PREVIEW.toString().equals(streamName)
                || KnownDatastreams.OCR_TEXT.toString().equals(streamName)
                || KnownDatastreams.AUDIO_MP3.toString().equals(streamName)
                || KnownDatastreams.AUDIO_WAV.toString().equals(streamName)
                || KnownDatastreams.OCR_ALTO.toString().equals(streamName)
                || KnownDatastreams.AUDIO_OGG.toString().equals(streamName);
    }

    private DigitalObjectWrapper getterHelper(String pid, boolean export) {
        try {
            ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
            paths = ensurePidPathForUnindexedObjects(pid, paths);
            for (int i = 0; i < paths.length; i++) {
                if (this.rightsResolver.isActionAllowed(SecuredActions.A_READ.getFormalName(), pid, null, paths[i]).flag()) {
                    return export ? akubraRepository.export(pid) : akubraRepository.get(pid);
                }
            }
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
        throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.A_READ, pid, null));
    }

    private DatastreamContentWrapper getFullThumbnail(String pid) throws IOException {
        boolean accessed = false;
        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
        paths = ensurePidPathForUnindexedObjects(pid, paths);
        for (ObjectPidsPath path : paths) {
            if (this.rightsResolver.isActionAllowed(SecuredActions.A_READ.getFormalName(), pid, FedoraUtils.IMG_PREVIEW_STREAM, path).flag()) {
                accessed = true;
                break;
            }
        }
        if (accessed) {
            if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_PREVIEW)) {
                return akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_PREVIEW);
            } else {
                throw new IOException("preview not found");
            }
        } else {
            throw new SecurityException(new SecurityException.SecurityExceptionInfo(SecuredActions.A_READ, pid, FedoraUtils.IMG_PREVIEW_STREAM));
        }
    }

    private List<String> getParentsPids(String targetPid) {
        List<String> pids = new ArrayList<>();
        String query = String.format("type:relation AND targetPid:%s", targetPid.replace(":", "\\:"));
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .sortField("date")
                .ascending(true)
                .cursorMark(ProcessingIndex.CURSOR_MARK_START)
                .rows(Integer.MAX_VALUE)
                .fieldsToFetch(List.of("source"))
                .build();
        akubraRepository.pi().iterate(params, processingIndexItem -> {
            pids.add(processingIndexItem.source());
        });
        return pids;
    }

    private ObjectPidsPath[] getPaths(String pid) {
        if (SpecialObjects.isSpecialObject(pid))
            return new ObjectPidsPath[]{ObjectPidsPath.REPOSITORY_PATH};
        if (CollectionPidUtils.isCollectionPid(pid)) {
            return new ObjectPidsPath[]{new ObjectPidsPath(pid)};
        }
        List<String> parentsPids = getParentsPids(pid);
        return new ObjectPidsPath[]{new ObjectPidsPath(parentsPids.toArray(new String[]{}))};
    }

}
