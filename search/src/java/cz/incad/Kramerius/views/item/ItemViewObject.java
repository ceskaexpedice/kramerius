package cz.incad.Kramerius.views.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.qbizm.kramerius.imp.jaxb.PageNumber;

import cz.incad.Kramerius.views.item.menu.ItemMenuViewObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.impl.AbstractTreeNodeProcessorAdapter;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class ItemViewObject {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ItemViewObject.class.getName());
    @Inject
    ServletContext servletContext;
    @Inject
    MostDesirable mostDesirable;
    @Inject
    HttpServletRequest request;
    @Inject
    Provider<Locale> localeProvider;
    @Inject
    ResourceBundleService resourceBundleService;
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    UserManager userManager;

    protected List<String> uuidPath;
    protected List<String> models;
    protected String pdfPage;
	
	
	public ItemViewObject() {
		super();
	}
	

    public void saveMostDesirable() {
        String pidPath = request.getParameter("pid_path");
        StringTokenizer tokenizer = new StringTokenizer(pidPath, "/");
        if (tokenizer.hasMoreTokens()) {
            this.mostDesirable.saveAccess(tokenizer.nextToken(), new Date());
        }
    }

    public String getFirstPageImageUrl() {
        if(pdfPage==null){
            return "fullThumb?uuid=" + getLastUUID();
        }else{
            return "djvu?uuid=" + getLastUUID() + "&amp;scaledWidth=512&amp;page="+(Integer.parseInt(pdfPage)-1);
        }
    }
    
    public String getThumbImageUrl() {
        if(pdfPage==null){
            return "thumb?uuid=" + getLastUUID() + "&amp;scaledWidth=650";
        }else{
            return "thumb?uuid=" + getLastUUID() + "&amp;scaledWidth=650&amp;page="+(Integer.parseInt(pdfPage)-1);
        }
    }

    public String getPage(){
        return pdfPage;
    }
    
    public String getImagePid() {
        return getLastUUID();
    }

    public String getFirstUUID() {
        return (getPids().isEmpty() ? null : getPids().get(0));
    }

    public String getParentUUID() {
        List<String> pids = getPids();
        if (pids.size() >= 2) {
            return pids.get(pids.size() - 2);
        } else {
            return null;
        }
    }

    public String getLastUUID() {
        if(pdfPage==null)
            return (getPids().isEmpty() ? null : getPids().get(getPids().size() - 1));
        else
            return (getPids().isEmpty() ? null : getPids().get(getPids().size() - 2));
    }

    public String[] getModelsFromRequest() {
        String[] models = request.getParameter("path").split("/");
        return models;
    }

    public String[] getUUIDPathFromRequest() {
        String[] pids = request.getParameter("pid_path").split("/");
        return pids;
    }

    public List<String> getPids() {
        LOGGER.fine("uuid path is  "+this.uuidPath);
        return uuidPath;
    }

    public void init() {
        try {
            final String[]pathFromRequests = getUUIDPathFromRequest();
            final String[] modelsFromRequest = getModelsFromRequest();
            String lastUuid = pathFromRequests[pathFromRequests.length -1];
            if (isPageUUID(lastUuid)) {
                pdfPage = getPageUUID(lastUuid);
                lastUuid = pathFromRequests[pathFromRequests.length -2];
            }
            // find everything
            final FindRestUUIDs fru = new FindRestUUIDs(this.fedoraAccess, lastUuid);
            fedoraAccess.processSubtree("uuid:"+lastUuid, fru);

            
            List<String> uuidsPathList = new ArrayList<String>(){{
                addAll(Arrays.asList(pathFromRequests));
                addAll(fru.getPathFromRoot());
            }};
            
            List<String> modelsPathList = new ArrayList<String>(){{
                addAll(Arrays.asList(modelsFromRequest));
                addAll(fru.getModelsFromRoot());
            }};
            
            this.uuidPath = uuidsPathList;
            this.models = modelsPathList;
            
        } catch (IOException e) {
            // co s tim ?
            throw new RuntimeException(e);
        } catch (ProcessSubtreeException e) {
            // co s tim ?
            throw new RuntimeException(e);
        }
    }

    public boolean isPageUUID(String uuid) {
        return uuid.indexOf("@")>-1;
    }

    public String getPageUUID(String pageUuid) {
        return pageUuid.substring(1);
    }
    

    public List<String> getModels() {
        return this.models;
    }

    public List<ItemMenuViewObject> getMenus() {
        try {
            List<String> pids = getPids();
            List<String> models = getModels();
            List<ItemMenuViewObject> menus = new ArrayList<ItemMenuViewObject>();
            for (int i = 0; i < pids.size(); i++) {
                menus.add(new ItemMenuViewObject(this.request, this.servletContext, this.fedoraAccess, this.userManager, this.resourceBundleService.getResourceBundle("labels", localeProvider.get()), KConfiguration.getInstance(), this, localeProvider.get(),pids.get(i), models.get(i), i));
            }
            return menus;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return new ArrayList<ItemMenuViewObject>();
        }
    }

    
    public String getDeepZoomURL() {
        return null;
    }
    

    /** Finds lists represents uuid path and model path from given root */
    private class FindRestUUIDs extends AbstractTreeNodeProcessorAdapter {
        
        private List<String> pathFromRoot = new ArrayList<String>();
        private List<String> modelsFromRoot = new ArrayList<String>();
        
        private String rootUUID;
        private FedoraAccess fedoraAccess;
        
        private int previousLevel = 0;
        private boolean broken = false;
        
        public FindRestUUIDs(FedoraAccess fedoraAccess,String rootUUID) {
            super();
            this.rootUUID = rootUUID;
            this.fedoraAccess = fedoraAccess;
        }


        @Override
        public void processUuid(String pageUuid, int level) throws ProcessSubtreeException {
            try {
                // dolu
                if (previousLevel < level || level == 0) {
                    if (!pageUuid.equals(rootUUID)) {
                        pathFromRoot.add(pageUuid);
                        modelsFromRoot.add(fedoraAccess.getKrameriusModelName(pageUuid));
                    }
                //nahoru 
                } else if (previousLevel > level) {
                    broken = true;
                // stejny level ale ne ten prvni
                } else if ((previousLevel == level) && (previousLevel != 0)){
                    broken = true;
                }
                previousLevel = level;
            } catch (IOException e) {
                throw new ProcessSubtreeException(e);
            } 
        }


        
        public List<String> getPathFromRoot() {
            LOGGER.fine("Path from root :"+this.pathFromRoot);
            return pathFromRoot;
        }


        public List<String> getModelsFromRoot() {
            LOGGER.fine("Models from root :"+this.pathFromRoot);
            return modelsFromRoot;
        }


        @Override
        public boolean breakProcessing(String pid, int level) {
            try {
                if (!broken) {
                    String uuid = ensureUUID(pid);
                    return fedoraAccess.isStreamAvailable(uuid, FedoraUtils.IMG_FULL_STREAM);
                } else return true;
            } catch (LexerException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
