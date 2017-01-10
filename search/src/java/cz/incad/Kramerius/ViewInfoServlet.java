package cz.incad.Kramerius;

import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import net.sf.json.JSONObject;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.AbstractObjectPath;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.http.AbstractLoggedUserProvider;
import cz.incad.kramerius.utils.ALTOUtils;
import cz.incad.kramerius.utils.ALTOUtils.AltoDisected;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RelsExtHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.solr.SolrUtils;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionsManager;

public class ViewInfoServlet extends GuiceServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(MimeTypeServlet.class.getName());
    
    private static StringTemplateGroup ST_GROUP = null;

    
    static {
        try {
            ST_GROUP = stGroup();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
    
    @Inject
    @Named("rawFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    SolrAccess solrAccess;
    
    @Inject
    DeepZoomCacheService deepZoomCacheService;

    @Inject
    IsActionAllowed actionAllowed;

    
    @Inject
    RightsManager rightsManager;

    @Inject
    RightCriteriumContextFactory ctxFactory;

    @Inject
    Provider<User> currentLoggedUserProvider;

    @Inject
    @Named("solr")
    CollectionsManager collectionGet;
    
    private InputStream dataStream;

    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pid = req.getParameter(UUID_PARAMETER);
            ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
    
            
            User user = currentLoggedUserProvider.get();
            if ((pid != null) && (!pid.equals(""))) {
                boolean imgfullAvailable = this.fedoraAccess.isImageFULLAvailable(pid);
                
                
                String mimeType = imgfullAvailable ? this.fedoraAccess.getImageFULLMimeType(pid) : "";
                boolean generated = imgfullAvailable ? resolutionFilePresent(pid) : false;
                boolean conf = imgfullAvailable ? deepZoomConfigurationEnabled(pid) : false;
                boolean hasAlto = this.fedoraAccess.isStreamAvailable(pid, "ALTO");
                String donator = this.fedoraAccess.getDonator(pid);
                
                HashMap map = new HashMap();
                // img full je dostupny
                map.put("imgfull", imgfullAvailable);
                // pdf rozsah - TODO: dat do konfigurace 
                map.put("pdfMaxRange", KConfiguration.getInstance().getConfiguration().getInt("generatePdfMaxRange",20));
                // img preview dostupny
                map.put("previewStreamGenerated", fedoraAccess.isStreamAvailable(pid, ImageStreams.IMG_PREVIEW.getStreamName()));
                // vygenerovano deepZoom
                map.put("deepZoomCacheGenerated", ""+generated);
                // povoleno deep zoom - TODO: dat do konfigurace
                map.put("deepZoomCofigurationEnabled", ""+conf);
                // forward to iip
                map.put("imageServerConfigured", ""+(!KConfiguration.getInstance().getUrlOfIIPServer().equals("")));
                // zobrazovany pid
                map.put("pid", pid);
                // model zobrazovaneho pidu
                map.put("model", fedoraAccess.getKrameriusModelName(fedoraAccess.getRelsExt(pid)));
                // cesta nahoru {uuid + parentofuuid + parentofparentofuuid + ... + root}
                map.put("pathsOfPids",paths);
                // donator
                map.put("donator", fedoraAccess.getDonator(pid));
                // nezobrazitelny obsah .. 
                map.put("displayableContent", ImageMimeType.loadFromMimeType(mimeType) != null);

                
                if (this.currentLoggedUserProvider.get().hasSuperAdministratorRole()) {
                    // kam to jinam dat ?? 
                    map.put("canhandlecommongroup",true);
                }

                Map<String, List<MappedPath>> securedActions = new HashMap<String, List<MappedPath>>();
                securedActions.put(SecuredActions.READ.getFormalName(), fillActionsToJSON(req, pid, paths,  SecuredActions.READ));
                securedActions.put(SecuredActions.ADMINISTRATE.getFormalName(), fillActionsToJSON(req, pid, paths, SecuredActions.ADMINISTRATE));

                
                //HashMap<String, HashMap<String, String>> secMapping = new HashMap<String, HashMap<String,String>>(); 
                // interpretuj pravo READ pro celou cestu  -
                // standardne jsou zdroje chraneny pres securedFedoraAccess, zde je to jine, 
                // pravo se neineterpretuje vicekrat, interpretuje se jednou a vysledek 
                // se pak vyhodnoti
                /*
                boolean[] vals = fillActionsToJSON(req, uuid, paths, secMapping, SecuredActions.READ);
                if (!firstMustBeTrue(vals)) {
                    //throw new SecurityException("access denided");
                }
                
                // pravo admin do kontext menu
                fillActionsToJSON(req, uuid, paths, secMapping, SecuredActions.ADMINISTRATE);
                */
                
                //map.put("actions",secMapping);
                
                Map<String, List<MappedPath>> globalActions = new HashMap<String, List<MappedPath>>();
                
                HttpSession session = req.getSession();
                if (session != null) {
                    List<String> actions = (List<String>) session.getAttribute(AbstractLoggedUserProvider.SECURITY_FOR_REPOSITORY_KEY);
                    if (actions != null) {
                        actions = new ArrayList<String>(actions);
                        SecuredActions[] acts = new SecuredActions[] { SecuredActions.ADMINISTRATE, SecuredActions.READ };
                        for (SecuredActions act : acts) {
                            actions.remove(act.getFormalName());
                        }
                        for (SecuredActions act : acts) {
                            List<MappedPath> pathElems = new ArrayList<MappedPath>();
                            pathElems.add(new MappedPath(new ObjectPidsPath().injectRepository().injectCollections(this.collectionGet), new boolean[] {true}));
                            globalActions.put(act.getFormalName(), pathElems);
                        }
                    }
                }

                map.put("securedActions",securedActions); 
                map.put("globalActions", globalActions);

                
                map.put("mimeType", mimeType);
                map.put("donator", ""+donator);

                if (hasAlto) {
                    boolean flag = altoObject(pid,map, req);
                    map.put("hasAlto", ""+flag);
                } else {
                    map.put("hasAlto", "false");
                }
                
                resp.setContentType("application/json; charset=utf-8");
                StringTemplate template = ST_GROUP.getInstanceOf("viewinfo");
                template.setAttribute("data", map);
                
                resp.getWriter().println(template.toString());
            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch(SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (CollectionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    


    private boolean altoObject(String imagePid,HashMap map, HttpServletRequest req) throws IOException, ParserConfigurationException, SAXException {
        try {
            if (req.getParameterMap().containsKey("q")) {
                String par = req.getParameter("q");
                Document parsed = getAltoDocument(imagePid);
                AltoDisected disected = ALTOUtils.disectAlto(par, parsed);
                map.put("alto", disected.toJSON().toString());
                return true;
            } else return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }


    private Document getAltoDocument(String imagePid) throws IOException, ParserConfigurationException, SAXException {
        InputStream is = this.fedoraAccess.getDataStream(imagePid, "ALTO");
        return XMLUtils.parseDocument(is);
    }






    private boolean firstMustBeTrue(boolean[] vals) {
        return (vals.length > 0) && (vals[0]);
    }
    
    private boolean atLeastOneTrue(boolean[] vals) {
        boolean accessed = false;
        for (boolean val : vals) {
            if (val) {
                accessed = true;
                break;
            }
        }
        return accessed;
    }

    public static class MappedPath {

        private ObjectPidsPath path;
        
        private boolean[] flags;
        
        public MappedPath(ObjectPidsPath path, boolean[] flags) {
            super();
            this.path = path;
            this.flags = flags;
        }
        
        public boolean[] getFlags() {
            return flags;
        }
        
        public ObjectPidsPath getPath() {
            return path;
        }
        
        public List<MappedPathElement> getMappedPathElements() {
            List<MappedPathElement> elms = new ArrayList<ViewInfoServlet.MappedPathElement>();
            String[] pathFromLeafToRoot = this.path.getPathFromLeafToRoot();
            for (int i = 0; i < pathFromLeafToRoot.length; i++) {
                elms.add(new MappedPathElement(this.flags[i], pathFromLeafToRoot[i]));
            }
            return elms;
        }
    }

    public static class MappedPathElement {
        private boolean flag;
        private String pid;
        
        public MappedPathElement(boolean flag, String pid) {
            super();
            this.flag = flag;
            this.pid = pid;
        }
        
        public String getPid() {
            return pid;
        }
        
        public boolean getFlag() {
            return this.flag;
        }
        
    }
   
    
    public MappedPath findPathWithFirstAccess(HttpServletRequest req, String pid, ObjectPidsPath[] paths,SecuredActions act) throws CollectionException {
        for (ObjectPidsPath objectPath : paths) {
            ObjectPidsPath path = objectPath.injectRepository().injectCollections(this.collectionGet);
            boolean[] allowedActionForPath = actionAllowed.isActionAllowedForAllPath(act.getFormalName(), pid, FedoraUtils.IMG_FULL_STREAM ,path);
            if (atLeastOneTrue(allowedActionForPath)) {
                return new MappedPath(path, allowedActionForPath);
            }
        }
        return null;
    }
    
    public List<MappedPath> fillActionsToJSON(HttpServletRequest req, String pid, ObjectPidsPath[] paths, SecuredActions act) throws CollectionException {
        List<MappedPath> mappedPaths = new ArrayList<ViewInfoServlet.MappedPath>();
        for (ObjectPidsPath objectPath : paths) {
            ObjectPidsPath path = objectPath.injectRepository().injectCollections(this.collectionGet);
            boolean[] allowedActionForPath = actionAllowed.isActionAllowedForAllPath(act.getFormalName(), pid, FedoraUtils.IMG_FULL_STREAM,path);
            mappedPaths.add(new MappedPath(path, allowedActionForPath));
        }
                
        return mappedPaths;
    }
    
    
/*    
    public boolean[] fillActionsToJSON(HttpServletRequest req, String uuid, ObjectPidsPath[] paths, HashMap<String, HashMap<String, String>> secMapping,SecuredActions act) {
        
        for (ObjectPidsPath objectPath : paths) {
            ObjectPidsPath path = objectPath.injectRepository();
            boolean[] allowedActionForPath = actionAllowed.isActionAllowedForAllPath(act.getFormalName(), uuid,path);
            for (boolean b : allowedActionForPath) {
                if (b) break;
            }
            
            
        }
                
        
        ArrayList<String> pathWithRepository = new ArrayList<String>(Arrays.asList(paths));
        pathWithRepository.add(0, SpecialObjects.REPOSITORY.getUuid());
        Collections.reverse(pathWithRepository);

        boolean[] allowedActionForPath = actionAllowed.isActionAllowedForAllPath(act.getFormalName(), uuid,paths);
        
        for (int j = 0; j < allowedActionForPath.length; j++) {
            if (!secMapping.containsKey(act.getFormalName())) {
                secMapping.put(act.getFormalName(), new HashMap<String, String>());
            }
            HashMap<String, String> pathMap = secMapping.get(act.getFormalName());
            pathMap.put(pathWithRepository.get(j), ""+allowedActionForPath[j]);
        }
        return allowedActionForPath;
    }
  */  


    private boolean resolutionFilePresent(String uuid) throws IOException, ParserConfigurationException, SAXException {
        boolean resFile = deepZoomCacheService.isResolutionFilePresent(uuid);
        return resFile;
    }
    
    
    private boolean deepZoomConfigurationEnabled(String uuid) {
        try {
            String relsExtUrl = RelsExtHelper.getRelsExtTilesUrl(uuid, this.fedoraAccess);
            return relsExtUrl != null;
        } catch (XPathExpressionException e) {
            LOGGER.severe(e.getMessage());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return false;
    }
    
    private static StringTemplateGroup stGroup() throws IOException {
        InputStream stream = ViewInfoServlet.class.getResourceAsStream("viewinfo.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        StringTemplateGroup group = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        return group;
    }

    
    public static void main(String[] args) {
        StringTemplate template = new StringTemplate(
            "$data.keys:{action| $data.(action).keys:{ key| $key$ :  $data.(action).(key)$ };separator=\",\"$ }$") ;
        
        HashMap map = new HashMap();

        HashMap<String, String> data = new HashMap<String, String>(); {
            data.put("drobnustky","true");
            data.put("stranka","true");
            data.put("repository","true");
        };
        map.put("edit",data);
        
        template.setAttribute("data", map);
        System.out.println(template.toString());
        
    }
    
    
    
    
}
