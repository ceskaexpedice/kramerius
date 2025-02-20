/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.imaging.impl;

import com.google.inject.Inject;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.imaging.DeepZoomFlagService;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.ProcessSubtreeException;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.ceskaexpedice.akubra.utils.TreeNodeProcessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;

public class DeepZoomFlagServiceImpl implements DeepZoomFlagService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DeepZoomFlagServiceImpl.class.getName());

    /* TODO AK_NEW
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

     */
    @Inject
    AkubraRepository akubraRepository;

    public void deleteFlagToPID(final String pid) throws IOException {
        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL.toString())) {
            deleteFlagToPIDInternal(pid);
        } else {

            try {

                RelsExtUtils.processSubtree(pid, new TreeNodeProcessor() {

                    @Override
                    public void process(String pid, int level) throws ProcessSubtreeException {
                        deleteFlagToPIDInternal(pid);
                    }

                    @Override
                    public boolean skipBranch(String pid, int level) {
                        return false;
                    }


                    @Override
                    public boolean breakProcessing(String pid, int level) {
                        return false;
                    }
                }, akubraRepository);


            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException) e.getCause();
                } else throw new RuntimeException(e);
            }
        }

    }


    @Override
    public void setFlagToPID(final String pid, final String tilesUrl) throws IOException {
        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL.toString())) {
            setFlagToPIDInternal(pid, tilesUrl);
        } else {
            try {
                RelsExtUtils.processSubtree(pid, new TreeNodeProcessor() {
                    @Override
                    public void process(String pid, int level) throws ProcessSubtreeException {
                        setFlagToPIDInternal(pid, tilesUrl);
                    }

                    @Override
                    public boolean skipBranch(String pid, int level) {
                        return false;
                    }


                    @Override
                    public boolean breakProcessing(String pid, int level) {
                        return false;
                    }
                }, akubraRepository);

            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException) e.getCause();
                } else throw new RuntimeException(e);
            }
        }
    }


    void deleteFlagToPIDInternal(String pid) {
        LOGGER.info("deleting deep zoom url for '" + pid + "'");
        Fedora4Utils.doWithProcessingIndexCommit(akubraRepository, (repo) -> {
            if (repo.objectExists(pid)) {
                akubraRepository.doWithWriteLock(pid, () -> {
                    boolean flag = akubraRepository.relsExtRelationExists(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    if (flag) {
                        akubraRepository.relsExtRemoveRelationsByNameAndNamespace(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    }
                    return null;
                });
            }
        });
    }

    void setFlagToPIDInternal(String pid, String tilesUrl) {
        Fedora4Utils.doWithProcessingIndexCommit(akubraRepository, (repo) -> {
            if (repo.objectExists(pid)) {
                akubraRepository.doWithWriteLock(pid, () -> {
                    boolean flag = akubraRepository.relsExtRelationExists(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    if (flag) {
                        akubraRepository.relsExtRemoveRelationsByNameAndNamespace(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    }
                    akubraRepository.relsExtAddLiteral(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI, tilesUrl);
                    return null;
                });

            }
        });
    }


}
