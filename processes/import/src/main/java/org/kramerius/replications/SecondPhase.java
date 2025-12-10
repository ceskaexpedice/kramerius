/*
 * Copyright (C) 2012 Pavel Stastny
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
package org.kramerius.replications;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.RepositoryNamespaceContext;
import org.ceskaexpedice.akubra.relsext.RelsExtUtils;
import org.kramerius.Import;
import org.kramerius.replications.pidlist.PIDsListLexer;
import org.kramerius.replications.pidlist.PIDsListParser;
import org.kramerius.replications.pidlist.PidsListCollect;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.*;

public class SecondPhase extends AbstractPhase  {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SecondPhase.class.getName());


    //TODO: move it to configuration
    static int NUMBER_OF_THREADS = 1;
    static int MAX_RUNNING_DAYS = 365;

    private boolean findPid = false;
    private String replicationCollections;
    private Injector injector;

    private ExecutorService executorService = null;
    private boolean replicationImages = false;

    private List<PhaseException> exceptions = new ArrayList<>();


    @Override
    public void start(String url, String userName, String pswd, String replicationCollections,String replicationImages) throws PhaseException {
        try {
            this.executorService = newFixedThreadPool(NUMBER_OF_THREADS);
            this.findPid = false;
            this.replicationCollections = replicationCollections;
            this.replicationImages = Boolean.parseBoolean(replicationImages);

            // initalize import
            Import.initialize(KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"));
            this.injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
            this.processIterate(url, userName, pswd);
            this.executorService.awaitTermination(60, TimeUnit.SECONDS);
            if (!this.exceptions.isEmpty()) {
                // phase failed
                throw new PhaseException(this, this.exceptions.get(0));
            }
        } catch (InterruptedException e) {
            throw new PhaseException(this, e);
        } finally {
            try {
                AkubraRepository akubraRepository = this.injector.getInstance(AkubraRepository.class);
                if (akubraRepository != null) akubraRepository.pi().commit();
            } catch (Exception e) {
                throw new PhaseException(this, e);
            }

        }
    }

    public void pidEmitted(String pid, String url, String userName, String pswd) throws PhaseException {
        try {
            LOGGER.info("processing pid '"+pid+"'");

            boolean shouldSkip = (findPid && findPid(pid));
            if (!shouldSkip) {
                File foxmlfile = null;
                InputStream inputStream = null;
                try {
                    long foxmlStart = System.currentTimeMillis();
                   // TODO  inputStream = rawFOXMLData(pid, url, userName, pswd);
                    foxmlfile = pidParseAndGetObjectId(inputStream, pid);
                    long foxmlStop = System.currentTimeMillis();
                    if (this.replicationImages) {
                        replicateImg(pid, url, foxmlfile);
                    }
                    LOGGER.fine("\t downloading foxml took "+(foxmlStop - foxmlStart)+" ms");
                    ingest(foxmlfile);
                } catch (LexerException e) {
                    throw new PhaseException(this,e);
                } catch (IOException e) {
                    throw new PhaseException(this,e);
                } catch(PhaseException e){
                    // forward
                    throw e;
                }catch(RuntimeException e) {
                    if (e.getCause() != null) throw new PhaseException(this,e.getCause());
                    else throw new PhaseException(this,e);
                } catch (Exception e) {
                    if (e.getCause() != null) throw new PhaseException(this,e.getCause());
                    else throw new PhaseException(this,e);
                } finally {
                    if (inputStream != null) IOUtils.closeQuietly(inputStream);
                    if (foxmlfile != null) foxmlfile.delete();
                }
            } else {
                LOGGER.info("skipping pid '"+pid+"'");
            }
        } catch (LexerException e) {
            throw new PhaseException(this,e);
        } catch (IOException e) {
            throw new PhaseException(this,e);
        }
    }

    private void replicateImg(String pid, String url, File foxml) throws PhaseException {
        /* TODO
        try {
            String handlePid = K4ReplicationProcess.pidFrom(url);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(foxml);
            String relsExt = RelsExtUtils.getTilesUrl(document.getDocumentElement()); // url of tiles

            if (relsExt != null) {
                InputStream stream = orignalImgData(pid, url);
                String imageserverDir = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerDirectory");
                String path = imageserverDir + File.separator + handlePid.substring(5) + File.separator;
                FileUtils.forceMkdir(new File(path));
                File replicatedImage = new File(path + pid.substring(5) + ".jp2");
                FileUtils.copyInputStreamToFile(stream, replicatedImage);

                XPathFactory xpfactory = XPathFactory.newInstance();
                XPath xpath = xpfactory.newXPath();
                xpath.setNamespaceContext(new RepositoryNamespaceContext());

                Node nodeTilesUrl = (Node) xpath.evaluate("//kramerius:tiles-url", document, XPathConstants.NODE);
                String imageServerTilesUrl = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerTilesURLPrefix");

                String suffixTiles = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerSuffix.tiles");
                String imageTilesUrl;
                if (KConfiguration.getInstance().getConfiguration().getBoolean("convert.imageServerSuffix.removeFilenameExtensions", false)) {
                    imageTilesUrl = imageServerTilesUrl + "/" +  handlePid.substring(5) + pid.substring(5) + suffixTiles;
                } else {
                    imageTilesUrl = imageServerTilesUrl + "/" +  handlePid.substring(5) + pid.substring(5) + ".jp2" + suffixTiles;
                }
                nodeTilesUrl.setTextContent(imageTilesUrl);

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource(document), new StreamResult(foxml));
            }
        } catch (ParserConfigurationException | IOException | XPathExpressionException | SAXException
                | TransformerException e) {
            throw new PhaseException(this, e);
        }

         */
    }

    /**
     * @param path
     * @param url
     * @param userName
     * @param pswd
     */
    public void pathEmmited(String path, String url, String userName, String pswd) {
        // Not important at the moment
    }


    public void ingest(File foxmlfile) throws PhaseException{
        LOGGER.info("ingesting '"+foxmlfile.getAbsolutePath()+"'");
        //Import.initialize(KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"));
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
        try {
            Import.ingest(akubraRepository, foxmlfile, null, null, false);  //TODO třetí parametr má být List<String>, inicializovaný na začátku této fáze a předaný třetí fázi, kde se budou třídit vazby
        } catch (RepositoryException e) {
            if (e.getCause() != null) throw new PhaseException(this, e.getCause());
            else throw new PhaseException(this,e);
        } catch (RuntimeException e) {
            if (e.getCause() != null) throw new PhaseException(this, e.getCause());
            else throw new PhaseException(this,e);
        }finally {
            akubraRepository.shutdown();
        }
    }
    
    public File pidParseAndGetObjectId(InputStream foxmlStream, String pid) throws LexerException, IOException, PhaseException {
        FileOutputStream fos = null;
        File foxml = createFOXMLFile(pid);
        try {
            fos = new FileOutputStream(foxml);
            IOUtils.copy(foxmlStream, fos );
            return foxml;
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    /* TODO
    public InputStream rawFOXMLData(String pid, String url, String userName, String pswd) throws PhaseException {
        Client c = Client.create();
        WebResource r = c.resource(K4ReplicationProcess.foxmlURL(url, pid, this.replicationCollections));
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));
        InputStream t = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        return t;
    }

    public InputStream orignalImgData(String pid, String url) {
        Client c = Client.create();
        WebResource r = c.resource(K4ReplicationProcess.imgOriginalURL(url, pid));
        //r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));
        InputStream t = r.accept("image/jp2").get(InputStream.class); // memory?
        return t;
    }

     */
    

    public File createFOXMLFile(String pid) throws LexerException, IOException, PhaseException {
        String objectId = pidParseAndGetObjectId(pid);
        File foxmlFile = new File(objectId+".fo.xml");
        if (!foxmlFile.exists()) foxmlFile.createNewFile();
        if (!foxmlFile.exists()) throw new PhaseException(this,"file not exists '"+foxmlFile.getAbsolutePath()+"'");
        return foxmlFile;
        
    }

    private static String pidParseAndGetObjectId(String pid) throws LexerException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        return pidParser.getObjectId();
    }


    private void processIterate(String url, String userName, String pswd) throws PhaseException {

        try {
            PIDsListLexer lexer = new PIDsListLexer(new FileReader(getIterateFile()));
            PIDsListParser parser = new PIDsListParser(lexer);
            parser.setPidsListCollect(new Emitter(url, userName, pswd));
            parser.pids();
        } catch (FileNotFoundException e) {
            throw new PhaseException(this,e);
        } catch (RecognitionException e) {
            throw new PhaseException(this,e);
        } catch (TokenStreamException e) {
            throw new PhaseException(this,e);
        } catch (RuntimeException e) {
            Throwable thr = e.getCause();
            if ((thr != null) && (thr instanceof PhaseException)) {
                throw ((PhaseException)thr);
            } else if (thr != null) {
                throw new PhaseException(this,thr);
            } else throw new PhaseException(this,e);
        } finally {
            LOGGER.info("All pids have been processed .. ");
            try {
                this.executorService.shutdown();
                this.executorService.awaitTermination(MAX_RUNNING_DAYS, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                throw new PhaseException(this,e);
            }
        }
    }



    @Override
    public void restart(String previousProcessUUID, File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd,
                        String replicationCollections, String replicationImages) throws PhaseException {
        if (!phaseCompleted) {
            // initalize import

            this.executorService = newFixedThreadPool(NUMBER_OF_THREADS);

            Import.initialize(KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"));
            this.injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
            this.findPid = true;

            this.replicationCollections = replicationCollections;
            this.replicationImages = Boolean.parseBoolean(replicationImages);
            processIterate(url, userName, pswd);
        }
    }

    public boolean findPid(String pid) throws LexerException, IOException {
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
        String objectId = pidParseAndGetObjectId(pid);
        return (akubraRepository.exists(objectId) && akubraRepository.re().exists(objectId));
    }



    class Emitter implements PidsListCollect {
        
        private String url,userName,pswd;
        
        
        public Emitter(String url, String userName, String pswd) {
            super();
            this.url = url;
            this.userName = userName;
            this.pswd = pswd;
        }


        @Override
        public void pidEmitted(String pid)  {
            if ((pid.startsWith("'")) || (pid.startsWith("\""))) {
                pid = pid.substring(1,pid.length()-1);
            }
            final String fpid = pid;
            SecondPhase.this.executorService.submit(()-> {
                try {
                    LOGGER.info("Submitting task for pid "+fpid);
                    SecondPhase.this.pidEmitted(fpid, this.url, this.userName, this.pswd);
                } catch (PhaseException e) {
                    SecondPhase.this.exceptions.add(e);
                    throw new RuntimeException(e);
                }
            });

        }


        @Override
        public void pathEmitted(String path) {
            if ((path.startsWith("'")) || (path.startsWith("\""))) {
                path = path.substring(1,path.length()-1);
            }
            SecondPhase.this.pathEmmited(path, this.url, this.userName, this.pswd);
        }


    }
    

}
