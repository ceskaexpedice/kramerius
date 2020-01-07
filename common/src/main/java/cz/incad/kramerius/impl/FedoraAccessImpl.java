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
package cz.incad.kramerius.impl;

import com.google.inject.Inject;

import cz.incad.kramerius.*;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.fedora.api.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.xpath.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;

import static cz.incad.kramerius.utils.FedoraUtils.*;
import static cz.incad.kramerius.utils.RESTHelper.openConnection;

/**
 * Default implementation of fedoraAccess
 *
 * @see FedoraAccess
 * @author pavels
 */
public class FedoraAccessImpl implements FedoraAccess {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FedoraAccessImpl.class.getName());
    private final KConfiguration configuration;
    private String fedoraVersion;
    private StringTemplateGroup xpaths;
    private XPathFactory xPathFactory;
    
    private StatisticsAccessLog accessLog;
    
    @Inject
    public FedoraAccessImpl(KConfiguration configuration,  @Nullable StatisticsAccessLog accessLog) throws IOException {
        super();
        this.configuration = configuration;
        this.xPathFactory = XPathFactory.newInstance();
        // read template
        InputStream stream = FedoraAccessImpl.class.getResourceAsStream("fedora_xpaths.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        xpaths = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        this.accessLog = accessLog;
    }

    @Override
    public List<Element> getPages(String pid, boolean deep) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getPages(pid, relsExt.getDocumentElement());
    }

    @Override
    public String getKrameriusModelName(Document relsExt) throws IOException {
        try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasModel", FedoraNamespaces.FEDORA_MODELS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                return pidParser.getObjectId();
            } else {
                throw new IllegalArgumentException("cannot find model of given document");
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getKrameriusModelName(String pid) throws IOException {
        return getKrameriusModelName(getRelsExt(pid));
    }

    @Override
    public Document getRelsExt(String pid) throws IOException {
        try {
            String relsExtUrl = relsExtUrl(KConfiguration.getInstance(), makeSureObjectPid(pid));
            LOGGER.fine("Reading rels ext +" + relsExtUrl);
            InputStream docStream = RESTHelper.inputStream(relsExtUrl, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());

            return XMLUtils.parseDocument(docStream, true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    private String makeSureObjectPid(String pid) throws LexerException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        String sureObjectPid = pidParser.isPagePid() ? pidParser.getParentObjectPid() : pidParser.getObjectPid();
        return sureObjectPid;
    }

    @Override
    public List<String> getModelsOfRel(Document relsExt) {
        try {
            throw new UnsupportedOperationException("still unsupported");
//            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasModel", FedoraNamespaces.FEDORA_MODELS_URI);
//            if (foundElement != null) {
//                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
//                PIDParser pidParser = new PIDParser(sform);
//                pidParser.disseminationURI();
//                ArrayList<String> model = RelsExtModelsMap.getModelsOfRelation(pidParser.getObjectId());
//                return model;
//            } else {
//                throw new IllegalArgumentException("cannot find model of ");
//            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public List<String> getModelsOfRel(String pid) throws IOException {
        return getModelsOfRel(getRelsExt(pid));
    }

    @Override
    public String getDonator(Document relsExt) {
        try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasDonator", FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                return pidParser.getObjectId();
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getDonator(String pid) throws IOException {
        return getDonator(getRelsExt(pid));
    }
    
    @Override
    public String getFirstItemPid(Document relsExt)
            throws IOException {
        try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasItem", FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                String pidItem = "uuid:" + pidParser.getObjectId();
                return pidItem;
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }
    
    @Override
    public String getFirstItemPid(String pid) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getFirstItemPid(relsExt);
    }
    
    @Override
    public String getFirstVolumePid(Document relsExt) throws IOException {
 
        try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasVolume", FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                String pidVolume = "uuid:" + pidParser.getObjectId();
                return pidVolume;
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }
    
    @Override
    public String getFirstVolumePid(String pid) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getFirstVolumePid(relsExt);
    }
    
    @Override
    public Document getBiblioMods(String pid) throws IOException {
        try {
            String biblioModsUrl = biblioMods(KConfiguration.getInstance(), makeSureObjectPid(pid));
            LOGGER.fine("Reading bibliomods +" + biblioModsUrl);
            InputStream docStream = RESTHelper.inputStream(biblioModsUrl, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
            return XMLUtils.parseDocument(docStream, true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public Document getDC(String pid) throws IOException {
        try {
            String dcUrl = dc(KConfiguration.getInstance(), makeSureObjectPid(pid));
            LOGGER.fine("Reading dc +" + dcUrl);
            InputStream docStream = RESTHelper.inputStream(dcUrl, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(docStream, bos);
            return XMLUtils.parseDocument(new ByteArrayInputStream(bos.toByteArray()), true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public String findFirstViewablePid(String pid) throws IOException {
        final List<String> foundPids = new ArrayList<String>();
        try {
            processSubtree(makeSureObjectPid(pid), new TreeNodeProcessor() {
                boolean breakProcess = false;
                int previousLevel = 0;

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return breakProcess;
                }

                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }

                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try {
                        if (previousLevel < level || level == 0) {
                            if (FedoraAccessImpl.this.isImageFULLAvailable(pid)) {
                                foundPids.add(pid);
                                breakProcess = true;
                            }
                        } else if (previousLevel > level) {
                            breakProcess = true;
                        } else if ((previousLevel == level) && (level != 0)) {
                            breakProcess = true;
                        }
                        previousLevel = level;
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        } catch (LexerException e) {
            throw new IOException(e);
        }

        return foundPids.isEmpty() ? null : foundPids.get(0);
    }

    @Override
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException {
        try {
            String pid = pids.get(pids.size() - 1);
            pid = makeSureObjectPid(pid);
            if (isImageFULLAvailable(pid)) {
                return true;
            }
            Document relsExt = getRelsExt(pid);
            Element descEl = XMLUtils.findElement(relsExt.getDocumentElement(), "Description", FedoraNamespaces.RDF_NAMESPACE_URI);
            List<Element> els = XMLUtils.getElements(descEl);
            for (Element el : els) {
                if (getTreePredicates().contains(el.getLocalName())) {
                    if (el.hasAttribute("rdf:resource")) {
                        pid = el.getAttributes().getNamedItem("rdf:resource").getNodeValue();
                        pids.add(pid);
                        models.add(getKrameriusModelName(pid));
                        //return getFirstViewablePath(pids, models);
                        boolean hit = getFirstViewablePath(pids, models);
                        if (hit) {
                            return true;
                        } else {
                            pids.remove(pids.size() - 1);
                            models.remove(pids.size() - 1);
                        }
                    }
                }
            }
            return false;
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public List<Element> getPages(String pid, Element rootElementOfRelsExt)
            throws IOException {
        try {
            ArrayList<Element> elms = new ArrayList<Element>();
            String xPathStr = "/RDF/Description/hasPage";
            XPath xpath = this.xPathFactory.newXPath();
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(rootElementOfRelsExt, XPathConstants.NODESET);
            for (int i = 0, lastIndex = nodes.getLength() - 1; i <= lastIndex; i++) {
                Element elm = (Element) nodes.item(i);
                elms.add(elm);
            }
            return elms;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        HttpURLConnection con = null;
        try {
            pid = makeSureObjectPid(pid);
            con = referencedDataStream(pid, IMG_THUMB_STREAM);
            if (con == null) {
                con = (HttpURLConnection) openConnection(getThumbnailFromFedora(configuration, makeSureObjectPid(pid)), configuration.getFedoraUser(), configuration.getFedoraPass());
            }
            InputStream thumbInputStream = con.getInputStream();
            return thumbInputStream;
        } catch (FileNotFoundException e) {
            if (con != null) {
                throw new FileNotFoundException("Bad " + pid + ": datastream url (" + con.getURL() +") not found");
            } else {
                throw e;
            }

        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public Document getSmallThumbnailProfile(String pid) throws IOException {
        try {
            pid = makeSureObjectPid(pid);
            HttpURLConnection con = (HttpURLConnection) openConnection(thumbImageProfile(configuration, makeSureObjectPid(pid)), configuration.getFedoraUser(), configuration.getFedoraPass());
            InputStream stream = con.getInputStream();
            return XMLUtils.parseDocument(stream, true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        try {
            Document profileDoc = getSmallThumbnailProfile(makeSureObjectPid(pid));
            return disectMimetypeFromProfile(profileDoc, getFedoraVersion());
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        try {
            pid = makeSureObjectPid(pid);
            if (this.accessLog != null && this.accessLog.isReportingAccess(pid,IMG_FULL_STREAM)) {
                try {	
                        this.accessLog.reportAccess(pid,IMG_FULL_STREAM);
                } catch (Exception e) {
                        LOGGER.severe("cannot write statistic records");
                        LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }

            HttpURLConnection con = referencedDataStream(pid, IMG_FULL_STREAM);
            if (con == null) {
                con = (HttpURLConnection) openConnection(getFedoraStreamPath(configuration, makeSureObjectPid(pid), IMG_FULL_STREAM), configuration.getFedoraUser(), configuration.getFedoraPass());
            }
            con.connect();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream thumbInputStream = con.getInputStream();
                return thumbInputStream;
            } else {
                // copy error stream and forward status code
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copyStreams(con.getErrorStream(), bos);
                throw new FedoraIOException(con.getResponseCode(), new String(bos.toByteArray()));
            }
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public boolean isImageFULLAvailable(String pid) throws IOException {
        try {
            return isStreamAvailable(makeSureObjectPid(pid), FedoraUtils.IMG_FULL_STREAM);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public InputStream getFedoraDataStreamsList(String pid) throws IOException {
        try {
            HttpURLConnection con = (HttpURLConnection) openConnection(getFedoraDatastreamsList(configuration, makeSureObjectPid(pid)), configuration.getFedoraUser(), configuration.getFedoraPass());
            InputStream stream = con.getInputStream();
            return stream;
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public Document getFedoraDataStreamsListAsDocument(String pid) throws IOException {
        try {
            InputStream stream = getFedoraDataStreamsList(pid);
            return XMLUtils.parseDocument(stream, true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        try {
            Document parseDocument = XMLUtils.parseDocument(getFedoraDataStreamsList(makeSureObjectPid(pid)), true);
            return disectDatastreamInListOfDatastreams(parseDocument, streamName, getFedoraVersion());
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    
    
    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        try {
            Document parseDocument = XMLUtils.parseDocument(getFedoraDataStreamsList(makeSureObjectPid(pid)), true);
            return true;
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (FileNotFoundException e) {
            return false;
        } 
    }

    @Override
    public boolean isContentAccessible(String pid) throws IOException {
        return true;
    }

    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        try {
            Document profileDoc = getImageFULLProfile(makeSureObjectPid(pid));
            return disectMimetypeFromProfile(profileDoc, getFedoraVersion());
        } catch (LexerException e) {
            throw new IOException(e.getMessage());
        }
    }

    boolean disectDatastreamInListOfDatastreams(Document datastreams, String dsId, String fedoraVersion) throws XPathExpressionException, IOException {
        XPath xpath = this.xPathFactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        String templateName = "find_datastream" + FedoraUtils.getVersionCompatibilityPrefix(fedoraVersion);
        StringTemplate xpathTemplate = xpaths.getInstanceOf(templateName);
        xpathTemplate.setAttribute("dsid", dsId);
        String xpathStringExp = xpathTemplate.toString();
        XPathExpression expr = xpath.compile(xpathStringExp);
        Node oneNode = (Node) expr.evaluate(datastreams, XPathConstants.NODE);
        return (oneNode != null);
    }

    String disectMimetypeFromProfile(Document profileDoc, String fedoraVersion)
            throws XPathExpressionException {
        XPathFactory factory = this.xPathFactory;
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());

        String templateName = "find_mimetype" + FedoraUtils.getVersionCompatibilityPrefix(fedoraVersion);
        StringTemplate xpathTemplate = xpaths.getInstanceOf(templateName);
        String xpathStringExp = xpathTemplate.toString();
        XPathExpression expr = xpath.compile(xpathStringExp);

        Node oneNode = (Node) expr.evaluate(profileDoc, XPathConstants.NODE);
        if (oneNode != null) {
            Element elm = (Element) oneNode;
            String mimeType = elm.getTextContent();
            if ((mimeType != null) && (!mimeType.trim().equals(""))) {
                mimeType = mimeType.trim();
                return mimeType;
            }
        }
        return null;
    }

    @Override
    public Document getImageFULLProfile(String pid) throws IOException {
        try {
            pid = makeSureObjectPid(pid);
            HttpURLConnection con = (HttpURLConnection) openConnection(fullImageProfile(configuration, pid), configuration.getFedoraUser(), configuration.getFedoraPass());
            InputStream stream = con.getInputStream();
            return XMLUtils.parseDocument(stream, true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    /**
     * Utility method which returns profile URL for IMG_FULL
     *
     * @param configuration K4 configuration
     * @param pid requested pid
     * @return URL for IMG _FULL profile
     */
    public static String fullImageProfile(KConfiguration configuration, String pid) {
        return dsProfile(configuration, FedoraUtils.IMG_FULL_STREAM, pid);
    }

    /**
     * Utility method which returns profile URL for IMG_THUMB
     *
     * @param configuration K4 configuration
     * @param pid Requested pid
     * @return URL for IMG _THUMB profile
     */
    public static String thumbImageProfile(KConfiguration configuration, String pid) {
        return dsProfile(configuration, FedoraUtils.IMG_THUMB_STREAM, pid);
    }

    /**
     * Utility method which returns profile URL for DC
     *
     * @param configuration K4 configuration
     * @param pid Requested pid
     * @return URL for DC profile
     */
    public static String dcProfile(KConfiguration configuration, String pid) {
        return dsProfile(configuration, FedoraUtils.DC_STREAM, pid);
    }

    /**
     * Utility method which returns profile URL for BIBLIO_MODS
     *
     * @param configuration K4 configuration
     * @param pid Requested pid
     * @return URL for BIBLIO_MODS profile
     */
    public static String biblioModsProfile(KConfiguration configuration, String pid) {
        return dsProfile(configuration, FedoraUtils.BIBLIO_MODS_STREAM, pid);
    }

    /**
     * Utility method which returns profile URL for RELS_EXT
     *
     * @param configuration K4 configuration
     * @param pid Requested pid
     * @return URL for RELS_EXT profile
     */
    public static String relsExtProfile(KConfiguration configuration, String pid) {
        return dsProfile(configuration, FedoraUtils.RELS_EXT_STREAM, pid);
    }

    /**
     * Utility method which returns object's profile URL for given pid
     *
     * @param configuration K4 configuration
     * @param pid Requested pid
     * @return URL for object's profile
     */
    public static String profile(KConfiguration configuration, String pid) {
        String fedoraObject = configuration.getFedoraHost() + "/objects/" + pid;
        return fedoraObject + "?format=text/xml";
    }

    /**
     * Utility method which returns data stream's profile URL for given pid and
     * stream name
     *
     * @param configuration K4 configuration
     * @param pid Requested pid
     * @param ds Data stream name
     * @return URL for object datastream's profile
     */
    public static String dsProfile(KConfiguration configuration, String ds, String pid) {
        String fedoraObject = configuration.getFedoraHost() + "/objects/" + pid;
        return fedoraObject + "/datastreams/" + ds + "?format=text/xml";
    }

    /**
     * Utility method which returns data stream's profile URL for given pid and
     * stream name
     *
     * @param configuration K4 configuration
     * @param pid Requested pid
     * @param ds Data stream name
     * @return URL for object datastream's profile
     */
    @Deprecated
    public static String dsProfileForPid(KConfiguration configuration, String ds, String pid) {
        String fedoraObject = configuration.getFedoraHost() + "/objects/" + pid;
        return fedoraObject + "/datastreams/" + ds + "?format=text/xml";
    }

    /**
     * Utility method which returns BIBLIO_MODS's URL for given pid
     *
     * @param configuration K4 configuration
     * @param pid Requested PID
     * @return URL for BIBLIO_MODS
     */
    public static String biblioMods(KConfiguration configuration, String pid) {
        String fedoraObject = configuration.getFedoraHost() + "/get/" + pid;
        return fedoraObject + "/BIBLIO_MODS";
    }

    /**
     * Utility method which returns DC's URL for given pid
     *
     * @param configuration K4 configuration
     * @param pid Requested PID
     * @return URL for DC
     */
    public static String dc(KConfiguration configuration, String pid) {
        String fedoraObject = configuration.getFedoraHost() + "/get/" + pid;
        return fedoraObject + "/DC";
    }

    /**
     * Utility method which returns RELS_EXT's URL for given pid
     *
     * @param configuration K4 configuration
     * @param pid Requested PID
     * @return URL for RELS_EXT
     */
    public static String relsExtUrl(KConfiguration configuration, String pid) {
        String url = configuration.getFedoraHost() + "/get/" + pid + "/" + FedoraUtils.RELS_EXT_STREAM;
        return url;
    }
    private FedoraAPIM APIMport;
    private FedoraAPIA APIAport;
    private ObjectFactory of;

    @Override
    public FedoraAPIA getAPIA() {
        if (APIAport == null) {
            initAPIA();
        }
        return APIAport;
    }

    @Override
    public FedoraAPIM getAPIM() {
        if (APIMport == null) {
            initAPIM();
        }
        return APIMport;
    }

    @Override
    public ObjectFactory getObjectFactory() {
        if (of == null) {
            of = new ObjectFactory();
        }
        return of;
    }

    private void initAPIA() {
        FedoraAPIAService APIAservice = new FedoraAPIAService();
        APIAport = APIAservice.getFedoraAPIAServiceHTTPPort();
        connectFedora((BindingProvider) APIAport,
                KConfiguration.getInstance().getFedoraHost() + "/services/access");
    }

    private void initAPIM() {
        FedoraAPIMService APIMservice = new FedoraAPIMService();
        APIMport = APIMservice.getFedoraAPIMServiceHTTPPort();
        connectFedora((BindingProvider) APIMport,
                KConfiguration.getInstance().getFedoraHost() + "/services/management");
    }

    private static void connectFedora(BindingProvider portBinding, String endpointAddress) {
        final String user = KConfiguration.getInstance().getFedoraUser();
        final String pwd = KConfiguration.getInstance().getFedoraPass();
        final Map<String, Object> context = portBinding.getRequestContext();
        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
        context.put(BindingProvider.USERNAME_PROPERTY, user);
        context.put(BindingProvider.PASSWORD_PROPERTY, pwd);
    }

    /*
     private List<String> treePredicates = Arrays.asList(new String[]{
     "http://www.nsdl.org/ontologies/relationships#hasPage",
     "http://www.nsdl.org/ontologies/relationships#hasPart",
     "http://www.nsdl.org/ontologies/relationships#hasVolume",
     "http://www.nsdl.org/ontologies/relationships#hasItem",
     "http://www.nsdl.org/ontologies/relationships#hasUnit"
     });
     */
//    private ArrayList<String> treePredicates;
//
//    private List<String> getTreePredicates() {
//        if (treePredicates == null) {
//            treePredicates = new ArrayList<String>();
//            String prefix = KConfiguration.getInstance().getProperty("fedora.predicatesPrefix");
//            
//            String[] preds = KConfiguration.getInstance().getPropertyList("fedora.treePredicates");
//            for (String s : preds) {
//                LOGGER.log(Level.INFO, prefix+s);
//                treePredicates.add(prefix + s);
//            }
//        }
//        return treePredicates;
//    }
    private List<String> getTreePredicates() {
        return Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.treePredicates"));
    }

    @Override
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException {
        try {
            pid = makeSureObjectPid(pid);
            Document relsExt = null;
            try {
                relsExt = getRelsExt(pid);
            } catch (Exception ex) {
                LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + "):" + ex);
            }
            if (!processor.skipBranch(pid, 0)) {
                processSubtreeInternal(pid, relsExt, processor, 0, new Stack<String>());
            }
        } catch (LexerException e) {
            throw new ProcessSubtreeException(e);
        } catch (XPathExpressionException e) {
            throw new ProcessSubtreeException(e);
        }
    }

    boolean processSubtreeInternal(String pid, Document relsExt, TreeNodeProcessor processor, int level, Stack<String> pidStack) throws XPathExpressionException, LexerException, IOException, ProcessSubtreeException {
        processor.process(pid, level);
        boolean breakProcessing = processor.breakProcessing(pid, level);
        if (breakProcessing) {
            return breakProcessing;
        }
        if (relsExt == null) {
            return false;
        }
        XPathFactory factory = this.xPathFactory;
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("/rdf:RDF/rdf:Description/*");
        NodeList nodes = (NodeList) expr.evaluate(relsExt, XPathConstants.NODESET);
        
        if(pidStack.contains(pid)){
            LOGGER.log(Level.WARNING, "Cyclic reference on "+pid);
            return breakProcessing;
        }
        pidStack.push(pid);
        changeStack(processor, pidStack);
        for (int i = 0, ll = nodes.getLength(); i < ll; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element iteratingElm = (Element) node;
                String namespaceURI = iteratingElm.getNamespaceURI();
                if (namespaceURI != null && (namespaceURI.equals(FedoraNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI)
                        || namespaceURI.equals(FedoraNamespaces.RDF_NAMESPACE_URI))) {
                    String attVal = iteratingElm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                    if (!attVal.trim().equals("")) {
                        PIDParser pidParser = new PIDParser(attVal);

                        try {
                            pidParser.disseminationURI();
                        } catch (LexerException e) {
                            LOGGER.warning(e.getMessage());
                            LOGGER.warning("could not parse '"+attVal+"' -> skipping");
                            continue;
                        }

                        String objectId = pidParser.getObjectPid();
                        if (pidParser.getNamespaceId().equals("uuid")) {
                            if (!processor.skipBranch(objectId, level + 1)) {
                                Document iterationgRelsExt = null;
                                try {
                                    iterationgRelsExt = getRelsExt(objectId);
                                } catch (Exception ex) {
                                    LOGGER.warning("could not read RELS-EXT, skipping branch [" + (level + 1) + "] and pid (" + objectId + "):" + ex);
                                }
                                breakProcessing = processSubtreeInternal(pidParser.getObjectPid(), iterationgRelsExt, processor, level + 1, pidStack);
                                if (breakProcessing) {
                                    break;
                                }
                            } else {
                                LOGGER.fine("skipping branch [" + (level + 1) + "] and pid (" + objectId + ")");
                            }
                        }
                    }

                }
            }
        }
        pidStack.pop();
        changeStack(processor, pidStack);
        return breakProcessing;
    }

    private void changeStack(TreeNodeProcessor processor, Stack<String> pidStack) {
        if (processor instanceof TreeNodeProcessStackAware) {
            TreeNodeProcessStackAware stackAware = (TreeNodeProcessStackAware) processor;
            stackAware.changeProcessingStack(pidStack);
        }
    }

    @Override
    public Set<String> getPids(String pid) throws IOException {
        final Set<String> retval = new HashSet<String>();
        try {
            processSubtree(pid, new TreeNodeProcessor() {
                @Override
                public void process(String pid, int level) {
                    retval.add(pid);
                }

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return false;
                }

                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }
        return retval;
    }

    public void observeStreamHeaders(String pid, String datastreamName, StreamHeadersObserver streamObserver) throws IOException {
        try {
            pid = makeSureObjectPid(pid);
            HttpURLConnection con = referencedDataStream(pid, datastreamName);
            if (con == null) {
                String streamLocation =  configuration.getFedoraHost() + "/get/" + pid + "/" + datastreamName;
                con = (HttpURLConnection) openConnection(streamLocation, configuration.getFedoraUser(), configuration.getFedoraPass());
            }
            con.connect();
            int statusCode = con.getResponseCode();
            Map<String, List<String>> headerFields = con.getHeaderFields();
            streamObserver.observeHeaderFields(statusCode, headerFields);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }
    
    
    
    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        try {
            pid = makeSureObjectPid(pid);
            if (this.accessLog != null && this.accessLog.isReportingAccess(pid,datastreamName)) {
                try {
                        this.accessLog.reportAccess(pid,datastreamName);
                } catch (Exception e) {
                    LOGGER.severe("cannot write statistic records");
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
            
            HttpURLConnection con = referencedDataStream(pid, datastreamName);
            if (con == null) {
                String streamLocation =  configuration.getFedoraHost() + "/get/" + pid + "/" + datastreamName;
                con = (HttpURLConnection) openConnection(streamLocation, configuration.getFedoraUser(), configuration.getFedoraPass());
            }
            
            con.connect();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream thumbInputStream = con.getInputStream();
                return thumbInputStream;
            } else {
                // returns concrete exception
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copyStreams(con.getErrorStream(), bos);
                throw new FedoraIOException(con.getResponseCode(), new String(bos.toByteArray()));
            }
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

    private HttpURLConnection referencedDataStream(String pid,
            String datastreamName) throws IOException, MalformedURLException {
        HttpURLConnection con = null;
        Document datastreamProfile = this.getStreamProfile(pid, datastreamName);
        Element elm = XMLUtils.findElement(datastreamProfile.getDocumentElement(), "dsControlGroup", FedoraNamespaces.FEDORA_MANAGEMENT_NAMESPACE_URI);
        if (elm != null) {
            // Referenced stream
            if (elm.getTextContent().trim().equals("E")) {
                Element dsLocation = XMLUtils.findElement(datastreamProfile.getDocumentElement(), "dsLocation", FedoraNamespaces.FEDORA_MANAGEMENT_NAMESPACE_URI);
                if (dsLocation != null) {
                    // no user, no pass
                    URLConnection directConnection = openConnection(dsLocation.getTextContent().trim(), "", "");
                    if (directConnection instanceof HttpURLConnection) {
                        con = (HttpURLConnection) directConnection;
                    } 
                }
            }
        }
        return con;
    }

    @Override
    public InputStream getDataStreamXml(String pid, String datastreamName) throws IOException {
        try {
            pid = makeSureObjectPid(pid);
            String datastream = configuration.getFedoraHost() + "/objects/" + pid + "/datastreams/" + datastreamName + "?format=xml";
            HttpURLConnection con = (HttpURLConnection) openConnection(datastream, configuration.getFedoraUser(), configuration.getFedoraPass());
            con.connect();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream thumbInputStream = con.getInputStream();
                return thumbInputStream;
            }
            throw new FileNotFoundException(datastream);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName) throws IOException {
        try {
            InputStream stream = getDataStreamXml(pid, datastreamName);
            return XMLUtils.parseDocument(stream, true);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    public InputStream getDsProfileForPIDStream(String pid, String streamName) throws MalformedURLException, IOException {
        try {
            pid = makeSureObjectPid(pid);
            HttpURLConnection con = (HttpURLConnection) openConnection(dsProfileForPid(configuration, streamName, pid), configuration.getFedoraUser(), configuration.getFedoraPass());
            InputStream stream = con.getInputStream();
            return stream;
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        try {
            Document parseDocument = XMLUtils.parseDocument(getDsProfileForPIDStream(pid, datastreamName), true);
            return disectMimetypeFromProfile(parseDocument, getFedoraVersion());
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        return (this.isStreamAvailable(pid, FedoraUtils.IMG_PREVIEW_STREAM));
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        HttpURLConnection con = (HttpURLConnection) openConnection(getFedoraStreamPath(configuration, pid, FedoraUtils.IMG_PREVIEW_STREAM), configuration.getFedoraUser(), configuration.getFedoraPass());
        con.connect();
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream thumbInputStream = con.getInputStream();
            return thumbInputStream;
        } else {
            // concrete exception
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(con.getErrorStream(), bos);
            throw new FedoraIOException(con.getResponseCode(), new String(bos.toByteArray()));
        }
    }

    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException,
            XPathExpressionException {
        throw new UnsupportedOperationException("");
    }

    @Override
    public Document getObjectProfile(String pid) throws IOException {
        try {
            HttpURLConnection con = (HttpURLConnection) openConnection(profile(configuration, pid), configuration.getFedoraUser(), configuration.getFedoraPass());
            con.connect();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = con.getInputStream();
                return XMLUtils.parseDocument(is, true);
            } else {
                // concrete exception
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copyStreams(con.getErrorStream(), bos);
                throw new FedoraIOException(con.getResponseCode(), new String(bos.toByteArray()));
            }
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public Document getStreamProfile(String pid, String stream) throws IOException {
        try {
            HttpURLConnection con = (HttpURLConnection) openConnection(dsProfile(configuration, stream, pid), configuration.getFedoraUser(), configuration.getFedoraPass());
            con.connect();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = con.getInputStream();
                return XMLUtils.parseDocument(is, true);
            } else {
                // concrete exception
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copyStreams(con.getErrorStream(), bos);
                throw new FedoraIOException(con.getResponseCode(), new String(bos.toByteArray()));
            }
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }

    }

    public InputStream getFedoraDescribeStream() throws IOException {
        HttpURLConnection con = (HttpURLConnection) openConnection(FedoraUtils.getFedoraDescribe(configuration), configuration.getFedoraUser(), configuration.getFedoraPass());
        con.connect();
        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream stream = con.getInputStream();
            return stream;
        } else {
            throw new IOException("404");
        }
    }

    @Override
    public String getFedoraVersion() throws IOException {
        if (fedoraVersion == null) {
            try {
                fedoraVersion = disectFedoraVersionFromStream(getFedoraDescribeStream());
            } catch (XPathExpressionException e) {
                throw new IOException(e);
            } catch (ParserConfigurationException e) {
                throw new IOException(e);
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }
        return fedoraVersion;
    }

    public String disectFedoraVersionFromStream(InputStream stream) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        // do not use namespaces
        Document parseDocument = XMLUtils.parseDocument(stream, false);
        XPathFactory factory = this.xPathFactory;
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("/fedoraRepository/repositoryVersion/text()");
        Node oneNode = (Node) expr.evaluate(parseDocument, XPathConstants.NODE);
        return (oneNode != null && oneNode.getNodeType() == Node.TEXT_NODE) ? ((Text) oneNode).getData() : "";
    }


}
