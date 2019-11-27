package cz.incad.Kramerius.views.virtualcollection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import biz.sourcecode.base64Coder.Base64Coder;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.CollectionUtils;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import cz.incad.kramerius.virtualcollections.VirtualCollectionsManager;
import cz.incad.kramerius.virtualcollections.impl.AbstractCollectionManager;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.fedora.api.ObjectProfile;

public class VirtualCollectionServlet extends GuiceServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(VirtualCollectionServlet.class.getName());
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    KConfiguration kConfiguration;
    public static final String ACTION_NAME = "action";

    @Inject
    @Named("fedora")
    CollectionsManager collectionManager;

    @Inject
    IsActionAllowed actionAllowed;

    @Inject
    Provider<User> userProvider;


    boolean permit(User user) {
        if (user != null)
            return this.actionAllowed.isActionAllowed(user, SecuredActions.VIRTUALCOLLECTION_MANAGE.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH);
        else
            return false;
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Actions actionToDo = Actions.CHANGE;
            String actionNameParam = req.getParameter(ACTION_NAME);
            if (actionNameParam != null) {
                actionToDo = Actions.valueOf(actionNameParam);
            }
            try {
                User user = this.userProvider.get();
                if (this.permit(user)) {
                    actionToDo.doPerform(this, fedoraAccess, this.collectionManager, req, resp);
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
                PrintWriter out = resp.getWriter();
                out.print(e1.toString());
            } catch (SecurityException e1) {
                LOGGER.log(Level.INFO, e1.getMessage());
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } catch (Exception e1) {
                LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                PrintWriter out = resp.getWriter();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
                out.print(e1.toString());
            }
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            PostActions actionToDo = PostActions.IMAGES_UPLOAD;
            String actionNameParam = req.getParameter(ACTION_NAME);
            if (actionNameParam != null) {
                User user = this.userProvider.get();
                if (this.permit(user)) {
                    actionToDo = PostActions.valueOf(actionNameParam);
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }
            try {
                actionToDo.doPerform(this, fedoraAccess, this.collectionManager, req, resp);
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
                PrintWriter out = resp.getWriter();
                out.print(e1.toString());
            } catch (SecurityException e1) {
                LOGGER.log(Level.INFO, e1.getMessage());
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } catch (Exception e1) {
                LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                PrintWriter out = resp.getWriter();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
                out.print(e1.toString());
            }
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }



    protected void writeOutput(HttpServletRequest req, HttpServletResponse resp, String s) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(s);
    }

    protected String[] getLangs() {
        String[] langs = kConfiguration.getPropertyList("interface.languages");
        return langs;
    }

    enum PostActions {
        
        /** long text uploading */
        LONG_TEXT_UPLOAD {

            @Override
            void doPerform(VirtualCollectionServlet vc, 
                    FedoraAccess fedoraAccess, 
                    CollectionsManager colMan, 
                    HttpServletRequest req, 
                    HttpServletResponse response) throws Exception, SecurityException {
                String collection = req.getParameter("pid");
                String language = req.getParameter("lang");

                String encodedProfile = req.getParameter("encodedData");
                byte[] decoded = Base64Coder.decode(encodedProfile);

                CollectionUtils.modifyLangDatastream(collection, language, AbstractCollectionManager.LONG_TEXT_DS_PREFIX+language,new String(decoded, "UTF-8"), fedoraAccess);
            }
        },

        /** image uploads */
        IMAGES_UPLOAD {
            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan,
                    HttpServletRequest req, HttpServletResponse response) throws Exception, SecurityException {

                String collection = req.getParameter("pid");
                
                ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                FileItemIterator iterator = upload.getItemIterator(req);
                
                while (iterator.hasNext()) {
                    FileItemStream fileItemStream = iterator.next();
                    String filename = fileItemStream.getName();

                    if (!fileItemStream.isFormField()) {
                        InputStream inputStream = fileItemStream.openStream();
                        String streamName = fileItemStream.getFieldName();
                        String contentType = fileItemStream.getContentType();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        IOUtils.copyStreams(inputStream, bos);;
                        LOGGER.info("Creating stream '"+streamName+"' for collection '"+collection+"'");
                        CollectionUtils.modifyImageDatastream(collection, streamName, contentType, bos.toByteArray(), fedoraAccess);
                    } else {
                        throw new IllegalArgumentException("illegal argument! ");
                    }
                }
            }
        };        
        abstract void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse response) throws Exception, SecurityException;
    }
    
    enum Actions {

        /**
         * Request to check if pid is in virtual collection
         */
        CHECK {

            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse resp) throws IOException, SecurityException {
                String pid = req.getParameter("pid");
                String collection = req.getParameter("collection");
                resp.setContentType("text/plain");
                if (CollectionUtils.isInCollection(pid, collection, fedoraAccess)) {
                    vc.writeOutput(req, resp, "1");
                } else {
                    vc.writeOutput(req, resp, "0");
                }

            }
        },
        /**
         * Request to add pid to virtual collection
         */
        ADDTOCOLLECTIONS {

            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse resp) throws IOException, SecurityException {
                String pid = req.getParameter("pid");

                String base = ProcessUtils.getLrServlet();
                
                if (base == null || pid == null) {
                    LOGGER.severe("Cannot start long running process");
                    return;
                }              
                String baseurl = base + "?action=start&def=virtualcollections&out=text";
                
                String[] rcollections = req.getParameterValues("remove");
                if (rcollections != null) {
                    for (String collection : rcollections) {
                        String token = System.getProperty(ProcessStarter.TOKEN_KEY);
                        String url = baseurl + "&params=remove," + pid + "," + URLEncoder.encode(collection, "UTF-8");
                        if(token!=null){
                            url += "&token=" + token;
                        }
                        //ProcessUtils.startProcess("virtualcollections", url)
                        ProcessStarter.httpGet(url);
                    }
                }
                String[] collections = req.getParameterValues("add");
                if (collections != null) {
                    for (String collection : collections) {
                        String token = System.getProperty(ProcessStarter.TOKEN_KEY);
                        String url = baseurl + "&params=add," + pid + "," + collection;
                        if(token!=null){
                            url += "&token=" + token;
                        }
                        ProcessStarter.httpGet(url);
                    }
                }

                String url = base + "?action=start&def=reindex&out=text&params=fromKrameriusModel," + pid + "," + pid + "&token=" + System.getProperty(ProcessStarter.TOKEN_KEY);

                LOGGER.info("indexer URL:" + url);
                try {
                    ProcessStarter.httpGet(url);
                } catch (Exception e) {
                    LOGGER.severe("Error spawning indexer for " + pid + ":" + e);
                }

            }
        },
        /**
         * Request to add pid to virtual collection
         */
        ADDTOCOLLECTION {

            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse resp) throws IOException, SecurityException {
                String pid = req.getParameter("pid");
                String collection = req.getParameter("collection");
                CollectionUtils.addToCollection(pid, collection, fedoraAccess);
            }
        },
        /**
         * Request to remove pid from virtual collection
         */
        REMOVEFROMCOLLECTION {

            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse resp) throws IOException, SecurityException {
                String pid = req.getParameter("pid");
                String collection = req.getParameter("collection");
                CollectionUtils.removeFromCollection(pid, collection, fedoraAccess);
            }
        },
        /**
         * Request to write to response the content of parameter content.
         * Used when creating datastream
         */
        TEXT {

            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse resp) throws IOException, SecurityException {
                String content = req.getParameter("content");
                vc.writeOutput(req, resp, content);
            }
        },
        /**
         * Request to create a virtual collection
         */
        LABEL {

            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse resp) throws IOException, SecurityException {
                String pid = req.getParameter("pid");
                ObjectProfile op = fedoraAccess.getAPIA().getObjectProfile(pid, null);
                vc.writeOutput(req, resp, op.getObjLabel());
            }
        },
        /**
         * Request to create a virtual collection
         */
        CREATE {

            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse resp) throws IOException, SecurityException, InterruptedException {
                
                boolean canLeave = Boolean.parseBoolean(req.getParameter("canLeave"));
                Map<String, String> plainTexts = new HashMap<String, String>();
                Enumeration paramNames = req.getParameterNames();
                while(paramNames.hasMoreElements()) {
                    String p = paramNames.nextElement().toString();
                    if (p.startsWith("text_")) {
                        String langCode = p.substring("text_".length());
                        plainTexts.put(langCode, req.getParameter(p));
                    }
                }
                
                String pid = CollectionUtils.create(fedoraAccess, null, canLeave, plainTexts, new CollectionUtils.CollectionManagerWait(colMan));
                resp.setContentType("text/plain");
                vc.writeOutput(req, resp, pid);
            }
        },
        /**
         * Request to delete a virtual collection
         */
        DELETE {

            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse resp) throws Exception, SecurityException {
                String pid = req.getParameter("pid");
                CollectionUtils.delete(pid, fedoraAccess);
            }
        },
        /**
         * Request to change a virtual collection
         */
        CHANGE {

            @Override
            void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse resp) throws IOException, SecurityException {
                String[] langs = vc.getLangs();
                String pid = req.getParameter("pid");
                boolean canLeave = Boolean.parseBoolean(req.getParameter("canLeave"));
                CollectionUtils.modify(pid, pid, canLeave, fedoraAccess);
                String string = req.getRequestURL().toString();
                URL url = new URL(string);
                //String k4url = url.getProtocol() + "://" + url.getHost() + ApplicationURL.extractPort(url) + req.getRequestURI();
                for (int i = 0; i < langs.length; i++) {
                    String lang = langs[++i];
                    String text = req.getParameter("text_" + lang);
                    if (text != null) {
                        CollectionUtils.modifyLangDatastream(pid, lang, text, fedoraAccess);
                    }
                }
                PrintWriter out = resp.getWriter();
                out.print("1");

            }
        };

        abstract void doPerform(VirtualCollectionServlet vc, FedoraAccess fedoraAccess, CollectionsManager colMan, HttpServletRequest req, HttpServletResponse response) throws Exception, SecurityException;
    }
}
