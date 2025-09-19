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
package org.kramerius.importer.inventory;

import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.pid.PIDParser;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexQueryParameters;
import org.ceskaexpedice.fedoramodel.DatastreamType;
import org.ceskaexpedice.fedoramodel.DatastreamVersionType;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.ceskaexpedice.fedoramodel.XmlContentType;
import org.kramerius.utils.DigitalObjectUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportInventoryFactory {

    public static final Logger LOGGER = Logger.getLogger(ImportInventoryFactory.class.getName());

    final Unmarshaller unmarshaller;
    final Object marshallingLock;
    final ProcessingIndex pi;

    public ImportInventoryFactory(Unmarshaller unmarshaller, Object marshallingLock, ProcessingIndex pi) {
        this.unmarshaller = unmarshaller;
        this.marshallingLock = marshallingLock;
        this.pi = pi;
    }

    public ImportInventory createIndexMap(File file, ImportInventoryItem.TypeOfInstance importType) {
        long start = System.currentTimeMillis();
        ImportInventory plan = new ImportInventory();
        Path startPath = file.toPath();
        try {
            LOGGER.info("Loading digital objects from: " + startPath);
            Map<File, DigitalObject> digitalObjectMap = loadDigitalObjects(startPath);
            LOGGER.info("Creating processing map for " + digitalObjectMap.size() + " digital objects.");
            Map<String, ImportInventoryItem> processingMap = createProcessingMap(digitalObjectMap, importType);
            LOGGER.info("Building tree structure for processing map.");
            buildTree(processingMap, plan);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            long end = System.currentTimeMillis();
            LOGGER.info("ImportInventory created in " + (end - start) + " ms.");
        }
        return plan;
    }

    private void buildTree(Map<String, ImportInventoryItem> processingMap, ImportInventory map) {
        processingMap.values().forEach(item-> {
            for (String childPid : item.getChildrenPids()) {
                ImportInventoryItem childItem = processingMap.get(childPid);
                if (childItem != null) {
                    childItem.setParent(item);
                }
                item.addChild(childItem);
            }
        });

        processingMap.values().forEach(item-> {
            map.addIndexationPlanItem(item);
        });
    }

    private Map<String, ImportInventoryItem> createProcessingMap(Map<File, DigitalObject> digitalObjects, ImportInventoryItem.TypeOfInstance importType) {
        Map<String, ImportInventoryItem> processingMap = new HashMap<>();

        digitalObjects.keySet().forEach(f->  {

            DigitalObject digitalObject = digitalObjects.get(f);
            String pid = digitalObject.getPID();
            String model = null;
            List<String> children = new ArrayList<>();
            boolean exists = existsInProcessingIndex(pid);
            String title = DigitalObjectUtils.dcTitle(digitalObject);
            Element relsExtElement = null;

            for (DatastreamType ds : digitalObject.getDatastream()) {
                if ("RELS-EXT".equals(ds.getID())) {
                    List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                    if (versions != null) {
                        DatastreamVersionType ver = versions.get(versions.size() - 1);
                        XmlContentType xmlContent = ver.getXmlContent();
                        relsExtElement = (Element) xmlContent.getAny().get(0).getFirstChild();
                    }
                }
            }
            if (relsExtElement != null) {
                Element modelElm = XMLUtils.findElement(relsExtElement, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getLocalName().equals("hasModel")) {
                            return true;
                        }
                        return false;
                    }
                });
                if (modelElm != null) {
                    Attr attr = modelElm.getAttributeNodeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                    String val = attr.getValue();
                    model = val.substring("info:fedora/model:".length());
                }

                List<Element> relations = XMLUtils.getElementsRecursive(relsExtElement, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        boolean namespace = element.getNamespaceURI().equals(RepositoryNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI);
                        Attr resource = element.getAttributeNodeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                        if (namespace && resource != null) {
                            return true;
                        }
                        return false;
                    }
                });

                relations.forEach(rel-> {
                    Attr resource = rel.getAttributeNodeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                    String val = resource.getValue();
                    try {
                        PIDParser pidParser = new PIDParser(val);
                        pidParser.disseminationURI();
                        children.add(pidParser.getObjectPid());
                    } catch (LexerException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                });
            }
            ImportInventoryItem importItem = new ImportInventoryItem(f, pid, model, title, children, exists);
            if (importType != null && importType.equals(ImportInventoryItem.TypeOfInstance.FULL)) {
                importItem.setDigitalObject(digitalObject);
            }
            processingMap.put(pid, importItem);
        });
        return processingMap;
    }

    private Map<File, DigitalObject> loadDigitalObjects(Path startPath) throws IOException {
        Map<File, DigitalObject> digitalObjectMap = new HashMap<>();
        synchronized (marshallingLock) {
            Files.walk(startPath, 20)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().toLowerCase().endsWith(".xml"))
            .map(f-> {
                try {
                    Object obj = unmarshaller.unmarshal(f.toFile());
                    Pair<Path, DigitalObject> p = Pair.of(f, (DigitalObject) obj);
                    return p;
                } catch (JAXBException e) {
                    LOGGER.info("Skipping file '" + f.toFile().getName() + "'; not an FOXML object.");
                    return null;
                }
            }).filter(Objects::nonNull).forEach(p-> {
                Path path = p.getLeft();
                DigitalObject dobj =  p.getRight();
                digitalObjectMap.put(path.toFile(), dobj);
            });
        }

        return digitalObjectMap;
    }

    boolean existsInProcessingIndex(String pid) {
        AtomicReference<Boolean> exists =  new AtomicReference<>(false);
        String query = "source:\"" + pid + "\"";
        ProcessingIndexQueryParameters params = new ProcessingIndexQueryParameters.Builder()
                .queryString(query)
                .rows(1)
                .fieldsToFetch(Arrays.asList("pid"))
                .build();

        pi.lookAt(params, processingIndexItem -> { exists.set(true);});
        return exists.get();
    }
}
