package cz.incad.kramerius.service.impl;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.service.ExportService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExportServiceImpl implements ExportService {
    public static final Logger LOGGER = Logger.getLogger(ExportServiceImpl.class.getName());
    private static int BUFFER_SIZE = 1024;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration configuration;
    @Inject
    SolrAccess solrAccess;

    private static final String INFO = "info:fedora/";

    @Override
    public void exportParents(String pid) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        LOGGER.log(Level.INFO, "Exporting parents is set to true, -> exporting parents foxml");
        File exportDirectory = exportDirectory(pid);

        ObjectPidsPath[] paths = solrAccess.getPath(pid);
        for (ObjectPidsPath opid: paths) {
            String[] set = opid.getPathFromLeafToRoot();
            for (int i = 1; i < set.length ; i++) {
                String childPid = set[i];
                String subChild = set[i-1];
                byte[] archives = fedoraAccess.getAPIM().export(childPid, "info:fedora/fedora-system:FOXML-1.1", "archive");
                // remove everything but child
                Document doc = XMLUtils.parseDocument(new ByteArrayInputStream(archives), true);

                Element relsExt = XMLUtils.findElement(doc.getDocumentElement(),(element) -> {
                    return element.getLocalName().equals("datastream") && element.getAttribute("ID").equals("RELS-EXT");
                });

                List<Element> relsExtVersions = XMLUtils.getElements(relsExt,(element) -> {
                    return element.getLocalName().equals("datastreamVersion");
                });

                Element relsExtVersion = latestVersion(relsExtVersions);
                List<String> treePredicates = Arrays.asList(this.configuration.getPropertyList("fedora.treePredicates"));
                Element xmlContent = XMLUtils.findElement(relsExtVersion, "xmlContent", "info:fedora/fedora-system:def/foxml#");
                List<Element> elems = XMLUtils.getElements(XMLUtils.findElement( XMLUtils.findElement(xmlContent, "RDF", FedoraNamespaces.RDF_NAMESPACE_URI), "Description",FedoraNamespaces.RDF_NAMESPACE_URI), (element) -> {
                    String localName = element.getLocalName();
                    String uri = element.getNamespaceURI();
                    if (uri.equals(FedoraNamespaces.KRAMERIUS_URI)) {
                        if (treePredicates.contains(localName)) {
                            // je to relace
                            String target = element.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                            try {
                                PIDParser pidParser = new PIDParser(target);
                                pidParser.disseminationURI();
                                String relationPid = pidParser.getObjectPid();
                                if (!relationPid.equals(subChild)) {
                                    return true;
                                }
                            } catch (LexerException e) {
                                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                                return false;
                            }
                            return false;
                        }
                    }
                    return false;
                });
                elems.stream().forEach((e) -> {e.getParentNode().removeChild(e);});
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                XMLUtils.print(doc, bos);
                store(exportDirectory, childPid, bos.toByteArray());

            }
        }

    }

    private Element latestVersion(List<Element> relsExtVersions) {
        int max = 0;
        for (Element elm : relsExtVersions) {
            String id = elm.getAttribute("ID");
            int index = id.indexOf('.');
            max = Math.max(Integer.parseInt(id.substring(index+1)),max);
        }

        for (Element e:  relsExtVersions) {
            if (e.getAttribute("ID").equals("RELS-EXT."+max)) {
                return e;
            }
        }
        return relsExtVersions.isEmpty() ? null : relsExtVersions.get(0);
    }

    @Override
    public void exportTree(String pid) throws IOException {

        Set<String> pids = fedoraAccess.getPids(pid);
        if (pids.isEmpty())
            return;

        File exportDirectory = exportDirectory(pid);
        IOUtils.cleanDirectory(exportDirectory);
        for (String s : pids) {
            String p = s.replace(INFO, "");
            LOGGER.info("Exporting "+exportDirectory+" "+p);
            try{
                store(exportDirectory, p, fedoraAccess.getAPIM().export(p, "info:fedora/fedora-system:FOXML-1.1", "archive"));
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(ExportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(ExportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(ExportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }catch(Exception ex){
                if (configuration.getConfiguration().getBoolean("export.shouldStopWhenFail", true)) {
                    throw ex;
                } else {
                    LOGGER.warning("Cannot export object "+p+", skipping: "+ex);
                }
            }
        }
    }

    private File exportDirectory(String pid) {
        String exportRoot = configuration.getProperty("export.directory");
        IOUtils.checkDirectory(exportRoot);
        return IOUtils.checkDirectory(exportRoot+File.separator+pid.replace("uuid:", "").replaceAll(":", "_"));
    }


    private void store(File exportDirectory, String name, byte[] contents) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        String convertedName = name.replace("uuid:", "").replaceAll(":", "_")+ ".xml";
        File toFile = new File(exportDirectory, convertedName);
        OutputStream os = null;
        InputStream is = null;
        
        Document doc = XMLUtils.parseDocument(new ByteArrayInputStream(contents), true);
        contents = changeNameForUrn(doc);
        
        try {
            is = new ByteArrayInputStream(contents);
            os = new FileOutputStream(toFile);
            byte[] buf = new byte[BUFFER_SIZE];
            for (int byteRead; (byteRead = is.read(buf, 0, BUFFER_SIZE)) >= 0;) {
                os.write(buf, 0, byteRead);
            }
            is.close();
            os.close();
        } catch (IOException e) {
            LOGGER.severe("IOException in export-store: " + e);
            throw new RuntimeException(e);
        }

    }
    
    /**
     * args[0] uuid of the root object
     * args[1] is information about exporting parents
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, TransformerException, SAXException, ParserConfigurationException {
        LOGGER.info("Export service: "+Arrays.toString(args));
        Boolean exportParents = null;
        
        if (args.length > 1) {
            if (args[args.length - 1].equals("true")) {
                exportParents = true;
            }
            if (args[args.length - 1].equals("false")) {
                exportParents = false;
            }
        }
        
        if (exportParents != null) {
            args = restArgs(args, 1);
        }
        
        for (int i = 0; i < args.length; i++) {
            ExportServiceImpl inst = new ExportServiceImpl();
            inst.fedoraAccess = new FedoraAccessImpl(null, null);
            inst.configuration = KConfiguration.getInstance();
            inst.solrAccess = new SolrAccessImpl();
            inst.exportTree(args[i]);

            if (exportParents == null) {
                String property = inst.configuration.getProperty("export.parents");
                ProcessStarter.updateName("Export FOXML, příznak pro export rodičů: " + property + ", pro titul " + args[i]);
                if (Boolean.valueOf(property)) {
                    inst.exportParents(args[i]);
                }
            }
            else {
                ProcessStarter.updateName("Export FOXML, příznak pro export rodičů: " + exportParents + ", pro titul " + args[i]);
                if (exportParents == true) {
                    inst.exportParents(args[i]);
                }
            }

            LOGGER.info("ExportService finished.");
        }
    }
    
    static String[] restArgs(String[] args, int i) {
        String[] nargs = new String[args.length - i];
        System.arraycopy(args, 0, nargs, 0, args.length-i);
        return nargs;
    }
    
    /** Detects if foxml contains only identifier - urn and not uuid, it will change it to uuid
     * doc represents foxml of file to be exported
     * @throws IOException
     */
    private byte[] changeNameForUrn(Document doc) throws TransformerException {
        Element biblioMods = XMLUtils.findElement(doc.getDocumentElement(),(element) -> {
                    return element.getLocalName().equals("datastream") && element.getAttribute("ID").equals("BIBLIO_MODS");
                });
        List<Element> biblioModsVersions = XMLUtils.getElements(biblioMods,(element) -> {
                    return element.getLocalName().equals("datastreamVersion");
                });
        for (Element biblioModsVersion : biblioModsVersions) {
            Element xmlContent = XMLUtils.findElement(biblioModsVersion, "xmlContent", "info:fedora/fedora-system:def/foxml#");
            Element modsCollection = XMLUtils.findElement(xmlContent, "modsCollection", FedoraNamespaces.BIBILO_MODS_URI);
            Element mods = XMLUtils.findElement(modsCollection, "mods", FedoraNamespaces.BIBILO_MODS_URI);
            List<Element> identifiers = XMLUtils.getElements(mods,(element) -> {
                    return element.getLocalName().equals("identifier");
                });
            
            Element urn = null;
            Boolean isThereUuid = false;
            for (Element identifier : identifiers) {
                if (identifier.hasAttribute("type")) {
                    String type = identifier.getAttribute("type");
                    if (type.equals("urn")) {
                        urn = identifier;
                    }
                    if (type.equals("uuid")) {
                        isThereUuid = true;
                    }
                }
            }
            if (isThereUuid == false && urn != null) {
                urn.removeAttribute("type");
                urn.setAttribute("type", "uuid");
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLUtils.print(doc, bos);
        return bos.toByteArray();
    }
    
}
