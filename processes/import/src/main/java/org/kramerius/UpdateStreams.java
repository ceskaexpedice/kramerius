/*
 * Copyright (C) 2025  Inovatika
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
package org.kramerius;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.fedoramodel.DatastreamType;
import org.ceskaexpedice.fedoramodel.DatastreamVersionType;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.kramerius.importer.inventory.ImportInventory;
import org.kramerius.importer.inventory.ImportInventoryFactory;
import org.kramerius.importer.inventory.ImportInventoryItem;
import org.kramerius.importer.inventory.ScheduleStrategy;
import org.w3c.dom.Element;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.kramerius.importer.ImporterCommons.marshallingLock;
import static org.kramerius.importer.ImporterCommons.unmarshaller;

public class UpdateStreams {

    public static final Logger LOGGER = Logger.getLogger(UpdateStreams.class.getName());


    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Not enough arguments.");
        }

        List<String> datastreamToUpdate = Arrays.asList(
                FedoraUtils.BIBLIO_MODS_STREAM,
                FedoraUtils.DC_STREAM);

        int argsIndex = 0;
        String authToken = args[argsIndex++]; //auth token always second, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        //process params
        String importDirFromArgs = args.length > argsIndex ? args[argsIndex++] : null;
        LOGGER.info(String.format("Import directory %s", importDirFromArgs));

        Boolean startIndexerFromArgs = args.length > argsIndex ? Boolean.valueOf(args[argsIndex++]) : null;
        Boolean startIndexer = Boolean.valueOf(KConfiguration.getInstance().getConfiguration().getString("ingest.startIndexer", "true"));
        if (startIndexerFromArgs != null) {
            startIndexer = startIndexerFromArgs;
        } else if (System.getProperties().containsKey("ingest.startIndexer")) {
            startIndexer = Boolean.valueOf(System.getProperty("ingest.startIndexer"));
        }

        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule(), new ImportModule());
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));

        ImportInventoryFactory factory = new ImportInventoryFactory(unmarshaller, marshallingLock, akubraRepository.pi());
        ImportInventory importInventory = factory.createIndexMap(new File(importDirFromArgs), ImportInventoryItem.TypeOfInstance.FULL);
        LOGGER.info("- PRINT INVENTORY - ");
        importInventory.printInventory();

        if (datastreamToUpdate.size() > 0) {
            List<ImportInventoryItem> items = importInventory.getIndexationPlanItems();
            items.forEach(item -> {
                DigitalObject digitalObject = item.getDigitalObject();
                Map<String, DatastreamType> datastreamTypes = new HashMap<>();
                digitalObject.getDatastream().stream().forEach(ds-> {
                    datastreamTypes.put(ds.getID().toLowerCase(), ds);
                });
                for (String dsName : datastreamToUpdate) {
                    if (datastreamTypes.containsKey(dsName.toLowerCase())) {
                        DatastreamType type = datastreamTypes.get(dsName.toLowerCase());
                        DatastreamVersionType dsVersion = type.getDatastreamVersion().get(0);
                        if (dsVersion.getXmlContent() != null) {
                            Element element = dsVersion.getXmlContent().getAny().get(0);
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            Source xmlSource = new DOMSource(element);
                            Result outputTarget = new StreamResult(outputStream);
                            try {
                                TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
                            } catch (TransformerException e) {
                                throw new RuntimeException(e);
                            }

                            String mimeType = akubraRepository.getDatastreamMetadata(item.getPid(), dsName).getMimetype();
                            akubraRepository.updateXMLDatastream(item.getPid(), dsName, mimeType, new ByteArrayInputStream(outputStream.toByteArray()));

                            String str = String.format("Datastream '%s' updated for object %s", dsName, item.getPid());
                            LOGGER.info(str);

                        } else if (dsVersion.getBinaryContent() != null) {
                                throw new RuntimeException("Update of managed binary datastream content is not supported.");
                        } else if (dsVersion.getContentLocation() != null) {
                            String mimeType = akubraRepository.getDatastreamMetadata(item.getPid(), dsName).getMimetype();
                            akubraRepository.updateExternalDatastream(item.getPid(), dsName, dsVersion.getContentLocation().getREF(), mimeType);

                            String str = String.format("External referenced datastream '%s' updated for object %s", dsName, item.getPid());
                            LOGGER.info(str);
                        }
                    }
                }
            });

            if (startIndexer) {
                for (ImportInventoryItem scheduleItem :  ScheduleStrategy.indexRoots.scheduleItems(importInventory)) {
                    ImportInventoryItem.TypeOfSchedule schedule = scheduleItem.getIndexationPlanType();
                    // TODO pepo ProcessScheduler.scheduleIndexation(scheduleItem.getPid(), scheduleItem.getTitle(), schedule == ImportInventoryItem.TypeOfSchedule.TREE , authToken);
                }
            }
        }
    }
}
