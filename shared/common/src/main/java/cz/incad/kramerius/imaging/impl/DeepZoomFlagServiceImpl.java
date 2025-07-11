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
import com.google.inject.name.Named;
import cz.incad.kramerius.imaging.DeepZoomFlagService;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.processingindex.OperationsHandler;
import org.ceskaexpedice.akubra.relsext.TreeNodeProcessor;

import java.io.IOException;

public class DeepZoomFlagServiceImpl implements DeepZoomFlagService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DeepZoomFlagServiceImpl.class.getName());

    @Inject
    SecuredAkubraRepository akubraRepository;

    public void deleteFlagToPID(final String pid) throws IOException {
        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
            deleteFlagToPIDInternal(pid);
        } else {

            try {

                akubraRepository.re().processInTree(pid, new TreeNodeProcessor() {

                    @Override
                    public void process(String pid, int level) {
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
                });


            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException) e.getCause();
                } else throw new RuntimeException(e);
            }
        }

    }


    @Override
    public void setFlagToPID(final String pid, final String tilesUrl) throws IOException {
        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
            setFlagToPIDInternal(pid, tilesUrl);
        } else {
            try {
                akubraRepository.re().processInTree(pid, new TreeNodeProcessor() {
                    @Override
                    public void process(String pid, int level) {
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
                });

            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException) e.getCause();
                } else throw new RuntimeException(e);
            }
        }
    }


    void deleteFlagToPIDInternal(String pid) {
        LOGGER.info("deleting deep zoom url for '" + pid + "'");
        akubraRepository.pi().doWithCommit(() -> {
            if (akubraRepository.exists(pid)) {
                akubraRepository.doWithWriteLock(pid, () -> {
                    boolean flag = akubraRepository.re().relationExists(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    if (flag) {
                        akubraRepository.re().removeRelationsByNameAndNamespace(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    }
                    return null;
                });
            }
        });
    }

    void setFlagToPIDInternal(String pid, String tilesUrl) {
        akubraRepository.pi().doWithCommit(() -> {
            if (akubraRepository.exists(pid)) {
                akubraRepository.doWithWriteLock(pid, () -> {
                    boolean flag = akubraRepository.re().relationExists(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    if (flag) {
                        akubraRepository.re().removeRelationsByNameAndNamespace(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    }
                    akubraRepository.re().addLiteral(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI, tilesUrl);
                    return null;
                });

            }
        });
    }


}
