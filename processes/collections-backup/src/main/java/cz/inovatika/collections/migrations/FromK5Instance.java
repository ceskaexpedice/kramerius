/*
 * Copyright (C) Dec 3, 2023 Pavel Stastny
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
package cz.inovatika.collections.migrations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kramerius.Import;
import org.kramerius.ImportModule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.IterationUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.IterationUtils.IterationCallback;
import cz.incad.kramerius.utils.IterationUtils.IterationEndCallback;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.collections.Restore;

public class FromK5Instance {
    
    
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public static Logger LOGGER = Logger.getLogger(FromK5Instance.class.getName());
    
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException, JAXBException, InterruptedException, SolrServerException, BrokenBarrierException {
        LOGGER.log(Level.INFO, "Process parameters: " + Arrays.asList(args).toString());
        if (args.length > 1) {
            Client client = Client.create();

            LOGGER.info("Reloading cloection");
            String authToken = args[0];
            String url = args[1];
            if (url.endsWith("/")) {
                url = url.substring(0,url.length()-1);
            }
            
            String tmpDirPath = System.getProperty("java.io.tmpdir");

            String subdirectoryPath = tmpDirPath + File.separator +  generateRandomString(5);
            FileUtils.forceMkdir(new File(subdirectoryPath));
            
            LOGGER.info(String.format("Generating temp folder %s", subdirectoryPath));
            
            
            JSONArray collections = collections(client, url);
            for (int i = 0; i < collections.length(); i++) {
                JSONObject col = collections.getJSONObject(i);
                String pid = col.getString("pid");
                JSONObject desc = col.getJSONObject("descs");
                
                List<String> pids = pids(client, pid, url);
                LOGGER.info(String.format("Found root pids %d", pids.size()));
                
                Document foxml = foxml(client, pid, url);
                createBiblioMods(foxml, desc);

                // remove dc 
                removeStream(foxml,"DC");
                removeStream(foxml,"AUDIT");
                removeStream(foxml,"TEXT");
                removeStream(foxml,"TEXT_cs");
                removeStream(foxml,"TEXT_en");
                addContainsRelations(foxml, pids);
                setStandalone(foxml);
                
                File vcFile = new File(subdirectoryPath,  pid.replace(':', '_')+".xml");
                try (FileOutputStream fos = new FileOutputStream(vcFile)) {
                    XMLUtils.print(foxml, new FileOutputStream(vcFile));
                }
            }

            LOGGER.info("Scheduling import "+subdirectoryPath);
            FromK5Instance.importTmpDir(subdirectoryPath, true, authToken);

        }  else {
            throw new IllegalArgumentException("");
        }
    }

    public static void setStandalone(Document foxml) {
        Element relsExt = XMLUtils.findElement(foxml.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String localName = element.getLocalName();
                String attribute = element.getAttribute("ID");
                return localName != null && localName.equals("datastream") && attribute != null && attribute.equals("RELS-EXT");
            }
        });
        
        if (relsExt != null) {
            Element rdfDecription = XMLUtils.findElement(relsExt, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String localName = element.getLocalName();
                    String nameSpace = element.getNamespaceURI();
                    return localName != null && localName.equals("Description") && nameSpace.equals(RepositoryNamespaces.RDF_NAMESPACE_URI);
                }
            });

            if (rdfDecription != null) {
                Element contains = foxml.createElementNS(RepositoryNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI, "rel:standalone");
                contains.setPrefix("rel");
                contains.setTextContent("true");
                rdfDecription.appendChild(contains);
            }
        }
        
    }

    private static void addContainsRelations(Document foxml, List<String> pids) {
        //<rel:contains xmlns:rel="http://www.nsdl.org/ontologies/relationships#" rdf:resource="info:fedora/uuid:fe91d1d0-fc5c-4ea7-9300-3504922a08d4"/>
        Element relsExt = XMLUtils.findElement(foxml.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String localName = element.getLocalName();
                String attribute = element.getAttribute("ID");
                return localName != null && localName.equals("datastream") && attribute != null && attribute.equals("RELS-EXT");
            }
        });
        
        if (relsExt != null) {
            Element rdfDecription = XMLUtils.findElement(relsExt, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String localName = element.getLocalName();
                    String nameSpace = element.getNamespaceURI();
                    return localName != null && localName.equals("Description") && nameSpace.equals(RepositoryNamespaces.RDF_NAMESPACE_URI);
                }
            });

            if (rdfDecription != null) {
                for (String pid : pids) {
                    Element contains = foxml.createElementNS(RepositoryNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI, "rel:contains");
                    contains.setPrefix("rel");
                    contains.setAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "rdf:resource", String.format("info:fedora/%s", pid));
                    rdfDecription.appendChild(contains);
                }
            }
        }
    }

    private static void createBiblioMods(Document foxml, JSONObject desc) {
        Element dataStream = foxml.createElementNS(RepositoryNamespaces.FEDORA_FOXML_URI, "datastream");
        dataStream.setAttribute("ID", "BIBLIO_MODS");
        dataStream.setAttribute("CONTROL_GROUP", "X");
        dataStream.setAttribute("STATE", "A");
        dataStream.setAttribute("VERSIONABLE", "false");
        foxml.getDocumentElement().appendChild(dataStream);
        
        
        Element datastreamVersion = foxml.createElementNS(RepositoryNamespaces.FEDORA_FOXML_URI, "datastreamVersion");
        datastreamVersion.setAttribute("ID", "BIBLIO_MODS.0");

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        // Získejte aktuální datum a čas jako LocalDateTime
        LocalDateTime currentDateTime = LocalDateTime.now();
        // Naformátujte aktuální datum a čas podle zadaného formátu
        String formattedDateTime = currentDateTime.format(formatter);
        datastreamVersion.setAttribute("CREATED", formattedDateTime);
        datastreamVersion.setAttribute("MIMETYPE", "text/xml");
        datastreamVersion.setAttribute("FORMAT_URI", "http://www.loc.gov/mods/v3");
        dataStream.appendChild(datastreamVersion);
        
        Element xmlContent = foxml.createElementNS(RepositoryNamespaces.FEDORA_FOXML_URI, "xmlContent");
        datastreamVersion.appendChild(xmlContent);
        
        Element modsCollection = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "modsCollection");
        xmlContent.appendChild(modsCollection);

        Element mods = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "mods");
        mods.setAttribute("version", "3.4");
        modsCollection.appendChild(mods);
        
        if (desc.has("cs")) {
            String csAbstract = null;
            
            // TEXT_cs - stream
            Element textcs = XMLUtils.findElement(foxml.getDocumentElement(), new XMLUtils.ElementsFilter() {
                
                @Override
                public boolean acceptElement(Element element) {
                    String id = element.getAttribute("ID");
                    return id != null && id.equals("TEXT_cs");
                }
            });
            if (textcs != null) {
                Element binary = XMLUtils.findElement(textcs, new XMLUtils.ElementsFilter() {
                    
                    @Override
                    public boolean acceptElement(Element element) {
                        String lname = element.getLocalName();
                        return lname != null && lname.equals("binaryContent");
                    }
                });
                
                csAbstract= new String(Base64.decodeBase64(binary.getTextContent()));
                Element abstractElm = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "abstract");
                abstractElm.setAttribute("lang", "cze");
                abstractElm.setTextContent(csAbstract);
                mods.appendChild(abstractElm);
            }
            
            
            
            Element titleInfo = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "titleInfo");
            titleInfo.setAttribute("lang", "cze");
            mods.appendChild(titleInfo);

            Element title = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "title");
            title.setTextContent(desc.getString("cs"));
            titleInfo.appendChild(title);
            
        }
        
        if (desc.has("en")) {
            String csAbstract = null;
            
            // TEXT_cs - stream
            Element textcs = XMLUtils.findElement(foxml.getDocumentElement(), new XMLUtils.ElementsFilter() {
                
                @Override
                public boolean acceptElement(Element element) {
                    String id = element.getAttribute("ID");
                    return id != null && id.equals("TEXT_en");
                }
            });
            if (textcs != null) {
                Element binary = XMLUtils.findElement(textcs, new XMLUtils.ElementsFilter() {
                    
                    @Override
                    public boolean acceptElement(Element element) {
                        String lname = element.getLocalName();
                        return lname != null && lname.equals("binaryContent");
                    }
                });
                
                csAbstract= new String(Base64.decodeBase64(binary.getTextContent()));
                Element abstractElm = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "abstract");
                abstractElm.setAttribute("lang", "eng");
                abstractElm.setTextContent(csAbstract);
                mods.appendChild(abstractElm);
            }

            Element titleInfo = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "titleInfo");
            titleInfo.setAttribute("lang", "eng");
            mods.appendChild(titleInfo);

            Element title = foxml.createElementNS(RepositoryNamespaces.BIBILO_MODS_URI, "title");
            title.setTextContent(desc.getString("en"));
            titleInfo.appendChild(title);
        }
    }

    private static void removeStream(Document foxml, String id) {
        Element elm = XMLUtils.findElement(foxml.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String idAttrVal = element.getAttribute("ID");
                return idAttrVal != null && idAttrVal.equals(id);
            }
        });
        if (elm != null) elm.getParentNode().removeChild(elm);
    }

    //https://kramerius.lib.cas.cz/search/api/v5.0/vc
    private static JSONArray collections(Client client,  String api) throws IOException {
        String url = String.format("%s/search/api/v5.0/vc", api);
        LOGGER.info(String.format( "Requesting url is %s", url));
        WebResource r = client.resource(url);

        WebResource.Builder builder = r.accept(MediaType.APPLICATION_JSON);
        InputStream clientResponse = builder.get(InputStream.class);
        String string = IOUtils.toString(clientResponse, "UTF-8");
        return new JSONArray(string);
        
    }
    
    private static Document foxml(Client client, String vcPid, String api)
            throws ParserConfigurationException, SAXException, IOException {
        String url = String.format("%s/search/api/v5.0/item/%s/foxml",api, vcPid);
        LOGGER.info(String.format( "Requesting url is %s", url));
        WebResource r = client.resource(url);

        WebResource.Builder builder = r.accept(MediaType.APPLICATION_XML);
        InputStream clientResponse = builder.get(InputStream.class);
        String document = IOUtils.toString(clientResponse, "UTF-8");
        String replaced = document.replaceAll("vc\\:", "uuid:");
        Document parsed = XMLUtils.parseDocument(new StringReader(replaced), true);
        return parsed;
    }

    //https://kramerius.lib.cas.cz/search/api/v5.0/search?q=*:*&fq=(fedora.model:monograph%20OR%20fedora.model:periodical%20OR%20fedora.model:sheetmusic%20OR%20fedora.model:monographunit)%20AND%20(collection:%22vc:5c2321df-2d8d-4a87-a262-15ffff990d81%22)&fl=PID,dostupnost,fedora.model,dc.creator,dc.title,dc.title,root_title,datum_str,dnnt-labels&facet=true&facet.mincount=1&facet.field=keywords&facet.field=language&facet.field=dnnt-labels&facet.field=mods.physicalLocation&facet.field=geographic_names&facet.field=facet_autor&facet.field=model_path&sort=created_date%20desc&rows=60&start=0

    public static void collectionIterations(Client client,String address, String masterQuery,IterationCallback callback, IterationEndCallback endCallback) throws ParserConfigurationException,  SAXException, IOException, InterruptedException, BrokenBarrierException {
        String cursorMark = null;
        String queryCursorMark = null;
        do {
            Element element =  pidsCursorQuery(client, address, masterQuery, cursorMark);
            cursorMark =  IterationUtils.findCursorMark(element);
            queryCursorMark = IterationUtils.findQueryCursorMark(element);
            callback.call(element, cursorMark);
        } while((cursorMark != null && queryCursorMark != null) && !cursorMark.equals(queryCursorMark));
        endCallback.end();
    }

    static Element pidsCursorQuery(Client client, String url, String mq,  String cursor)  throws ParserConfigurationException, SAXException, IOException{
        int rows = 1000;
        String query = "search" + "?q="+mq + (cursor!= null ? String.format("&rows=%d&cursorMark=%s", rows, cursor) : String.format("&rows=%d&cursorMark=*", rows))+"&sort=" + URLEncoder.encode("PID desc", "UTF-8")+"&fl=PID";
        return IterationUtils.executeQuery(client, url, query);
    }

    
    private static List<String> pids(Client client, String vcPid, String api)
            throws ParserConfigurationException, SAXException, IOException, InterruptedException, BrokenBarrierException {
        
        final List<String> returnPids = new ArrayList<>();
        
        String url = null;
        if (api.endsWith("/")) {
            url = String.format("%ssearch/api/v5.0/",api);
             
        } else {
            url = String.format("%s/search/api/v5.0/",api);
        }
        

        List<String> models = Arrays.asList(
                "monograph",
                "periodical",
                "sheetmusic",
                "monographunit"
                );
        
        String fqModel = "fedora.model:("+models.stream().collect(Collectors.joining(" OR "))+") AND ";
        String collectionPid="(collection:\""+vcPid+"\")";
        String masterQuery= URLEncoder.encode(fqModel + collectionPid, "UTF-8");
        
        collectionIterations(client, url, masterQuery , (elm, i) -> {

            Element result = XMLUtils.findElement(elm, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String nodeName = element.getNodeName();
                    return nodeName.equals("result");
                }
            });
            if (result != null) {
                List<Element> elements = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        String nodeName = element.getNodeName();
                        return nodeName.equals("doc");
                    }
                });

                List<String> idents = elements.stream().map(item -> {
                            Element str = XMLUtils.findElement(item, new XMLUtils.ElementsFilter() {
                                        @Override
                                        public boolean acceptElement(Element element) {
                                            return element.getNodeName().equals("str");
                                        }
                                    }
                            );
                            return str.getTextContent();
                        }
                ).collect(Collectors.toList());

                returnPids.addAll(idents);
                
            } 
        }, ()->{});
        return returnPids;
    }

    
    public static void importTmpDir(String exportRoot, boolean startIndexer, String authToken) throws JAXBException, IOException, InterruptedException, SAXException, SolrServerException {
        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule(), new ImportModule());
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
        SortingService sortingServiceLocal = injector.getInstance(SortingService.class);

        Import.run(akubraRepository, akubraRepository.pi(), sortingServiceLocal,
                KConfiguration.getInstance().getProperty("ingest.url"),
                KConfiguration.getInstance().getProperty("ingest.user"),
                KConfiguration.getInstance().getProperty("ingest.password"),
                exportRoot, startIndexer, authToken, null);
    
        Restore.LOGGER.info(String.format("Deleting directory %s", exportRoot));
        File exportFolder = new File(exportRoot);
        FileUtils.deleteDirectory(exportFolder);
    }
    
}


