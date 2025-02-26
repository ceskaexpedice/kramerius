/*
 * Copyright (C) Nov 29, 2023 Pavel Stastny
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
package cz.inovatika.collections;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

//import static cz.incad.kramerius.utils.XMLUtils.LOGGER;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;


public class Backup {
    
    public static final Logger LOGGER = Logger.getLogger(Backup.class.getName());
    
    public static void main(String[] args) throws TransformerException, ParserConfigurationException, SAXException, IOException {
        LOGGER.log(Level.INFO, "Process parameters: " + Arrays.asList(args).toString());
        if (args.length > 2) {
            Client client = Client.create();
            
            String target = args[1];
            String nameOfBackup = args[2];
            String tmpDirPath = System.getProperty("java.io.tmpdir");

            String subdirectoryPath = tmpDirPath + File.separator +  nameOfBackup;
            FileUtils.forceMkdir(new File(subdirectoryPath));

            
            for (String pid : extractPids(target)) {
                List<String> collectionProcessed = new ArrayList<>();
                Stack<String> processingStack = new Stack<>();
                processingStack.add(pid);
                while(!processingStack.isEmpty()) {
                    String processingPid = processingStack.pop();
                    if (collectionProcessed.contains(processingPid)) {
                        LOGGER.warning(String.format("Found cycle on %s", processingPid));
                        continue;
                    }
                    collectionProcessed.add(processingPid);
                    if (head(client, processingPid) == 200) {
                        Document parsed = foxml(client, processingPid);
                        StringWriter writer = new StringWriter();
                        XMLUtils.print(parsed, writer);
                        LOGGER.info(String.format("Writing to %s", new File(new File(subdirectoryPath), processingPid.replace(":", "_")).getAbsolutePath()));
                        FileUtils.writeByteArrayToFile(new File(new File(subdirectoryPath), processingPid.replace(":", "_")+".xml"), writer.toString().getBytes("UTF-8"));
                        List<Element> recursiveElements = XMLUtils.getElementsRecursive(parsed.getDocumentElement(), new XMLUtils.ElementsFilter() {
                            
                            @Override
                            public boolean acceptElement(Element element) {
                                boolean equals = element.getLocalName().equals("contains");
                                return equals;
                            }
                        });
                        

                        List<String> pids = recursiveElements.stream().map(elm-> {
                            String attributeNS = elm.getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                            if (attributeNS.contains("info:fedora/")) {
                                String containsPid = attributeNS.substring("info:fedora/".length());
                                return containsPid;
                            } else  return null;
                        }).filter(Objects::nonNull).collect(Collectors.toList());
                        
                        
                        if (pids.size() > 0) {

                            int batchSize = 40;
                            int numberOfIteration = pids.size() / batchSize;
                            if (pids.size() % batchSize != 0) {
                                numberOfIteration = numberOfIteration + 1;
                            }
                            for (int iteration = 0; iteration < numberOfIteration; iteration++) {
                                int start = iteration* batchSize;
                                int stop = Math.min((iteration+1)*batchSize, pids.size());
                                List<String> subPids = pids.subList(start, stop);
                                
                                String query = subPids.stream().map(it-> {return '"' + it +'"';}).collect(Collectors.joining(" OR "));
                                String encodedCondition = URLEncoder.encode(" AND pid:(" + query + ")", "UTF-8");

                                
                                String solrSearchHost = KConfiguration.getInstance().getSolrSearchHost()+String.format("/select?fq=model:collection%s&q=*&fl=pid&wt=json", encodedCondition);
                                
                                InputStream inputStream = RESTHelper.inputStream(solrSearchHost, "", "");
                                String string = IOUtils.toString(inputStream, "UTF-8");
                                JSONObject object = new JSONObject(string);
                                JSONObject response = object.getJSONObject("response");
                                JSONArray docs = response.getJSONArray("docs");
                                for (int i = 0; i < docs.length(); i++) {
                                    JSONObject doc = docs.getJSONObject(i);
                                    String collectionPid = doc.optString("pid");
                                    processingStack.push(collectionPid);
                                }
                                
                            }
                            
                        }
                    } else {
                        LOGGER.warning(String.format("Pid %s doesnt exists",processingPid));
                    }
                }
            }
            
            File tmpDir = new File(subdirectoryPath);
            File[] listFiles = tmpDir.listFiles();
            if (listFiles != null) {
                String parentZipFolder = KConfiguration.getInstance().getConfiguration().getString("collections.backup.folder");
                if (parentZipFolder == null) throw new IllegalStateException("configuration property 'collections.backup.folder' must be set ");
                FileUtils.forceMkdir(new File(parentZipFolder));

                String zipFile = parentZipFolder + File.separator + nameOfBackup+".zip";
                try {
                    FileOutputStream fos = new FileOutputStream(zipFile);
                    ZipOutputStream zos = new ZipOutputStream(fos);
                    
                    for (File lF : listFiles) {
                        addFileToZip("",  lF, zos); 
                    }
                    
                    zos.close();
                    fos.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        }
    }


    private static List<String> extractPids(String target) {
        if (target.startsWith("pid:")) {
            String pid = target.substring("pid:".length());
            List<String> result = new ArrayList<>();
            result.add(pid);
            return result;
        } else if (target.startsWith("pidlist:")) {
            List<String> pids = Arrays.stream(target.substring("pidlist:".length()).split(";")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            return pids;
        } else if (target.startsWith("pidlist_file:")) {
            String filePath = target.substring("pidlist_file:".length());
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    return IOUtils.readLines(new FileInputStream(file), Charset.forName("UTF-8"));
                } catch (IOException e) {
                    throw new RuntimeException("IOException " + e.getMessage());
                }
            } else {
                throw new RuntimeException("file " + file.getAbsolutePath() + " doesnt exist ");
            }
        } else {
            throw new RuntimeException("invalid target " + target);
        }
    }
    
    private static int head(Client client, String pid) {
        String url = KConfiguration.getInstance().getConfiguration().getString("api.client.point") + (KConfiguration.getInstance().getConfiguration().getString("api.client.point").endsWith("/") ? "" : "/") + String.format("items/%s/metadata/mods", pid);
        LOGGER.info(String.format("Url %s", url));
        
        WebResource r = client.resource(url);

        WebResource.Builder builder = r.accept(MediaType.APPLICATION_XML);
        ClientResponse head = builder.head();
        int status = head.getStatus();
        return status;
    }


    private static Document foxml(Client client, String processingPid)
            throws ParserConfigurationException, SAXException, IOException {
        String url = KConfiguration.getInstance().getConfiguration().getString("api.client.point") + (KConfiguration.getInstance().getConfiguration().getString("api.client.point").endsWith("/") ? "" : "/") + String.format("items/%s/foxml", processingPid);
        LOGGER.info(String.format( "Requesting url is %s", url));
        WebResource r = client.resource(url);

        WebResource.Builder builder = r.accept(MediaType.APPLICATION_XML);
        InputStream clientResponse = builder.get(InputStream.class);
        Document parsed = XMLUtils.parseDocument(clientResponse, true);
        return parsed;
    }


private static void addFileToZip(String path, File srcFile, ZipOutputStream zipOut) throws IOException {
    FileInputStream fis = new FileInputStream(srcFile);
    ZipEntry zipEntry = new ZipEntry(path + "/" + srcFile.getName());
    zipOut.putNextEntry(zipEntry);

    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
        zipOut.write(bytes, 0, length);
    }
    fis.close();
}

}
