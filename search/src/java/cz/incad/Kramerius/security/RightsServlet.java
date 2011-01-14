/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.Kramerius.security;

import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.security.strenderers.CriteriumParamsWrapper;
import cz.incad.Kramerius.security.strenderers.CriteriumWrapper;
import cz.incad.Kramerius.security.strenderers.RightWrapper;
import cz.incad.Kramerius.security.strenderers.SecuredActionWrapper;
import cz.incad.Kramerius.security.strenderers.TitlesForObjects;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.ClassRightCriterium;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.security.impl.criteria.CriteriumsLoader;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class RightsServlet extends GuiceServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(RightsServlet.class.getName());
    
    @Inject
    transient RightsManager rightsManager;

    @Inject
    transient Provider<User> userProvider;

    @Inject
    transient SolrAccess solrAccess;

    @Inject
    transient ResourceBundleService resourceBundleService;

    @Inject
    transient Provider<Locale> localesProvider;

    @Inject
    @Named("securedFedoraAccess")
    transient FedoraAccess fedoraAccess;
    
    @Inject
    UserManager userManager;
    
    
    StringTemplateGroup htmlForms;
    StringTemplateGroup jsData;
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uuid = req.getParameter(UUID_PARAMETER);
        try {
            PIDParser pidParser = new PIDParser("uuid:"+uuid);
            pidParser.objectPid();
            
            String securedActionString = req.getParameter("securedaction");
            
            String action = req.getParameter("action");
            
            htmlForms  = stFormsGroup();
            jsData  = stJSDataGroup();
            
            GetAction selectedAction = GetAction.valueOf(action);
            if (selectedAction != null) {
                ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("labels", localesProvider.get());
                String[] uuids = this.solrAccess.getPathOfUUIDs(uuid);
                String[] models = this.solrAccess.getPathOfModels(uuid);
                selectedAction.doAction(getServletContext(), req, resp, req.getParameterMap(), this.fedoraAccess, this.solrAccess, this.rightsManager, this.userProvider.get(),uuids, models, securedActionString, resourceBundle, htmlForms, jsData);
            }

        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

   

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        PostAction postAction = PostAction.valueOf(action);
        if (postAction != null) {
            postAction.doAction(
                            getServletContext(), 
                            req, 
                            resp, 
                            fedoraAccess, 
                            solrAccess, 
                            rightsManager, 
                            userManager, 
                            userProvider.get(), 
                            action);
        }
    }


    static enum PostAction {
        delete {

            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, UserManager userManager, User user, String action) throws IOException {
                try {
                    Right right = createRightFromPostIds(req, rightsManager, userManager);
                    rightsManager.deleteRight(right);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                    resp.sendError(HttpServletResponse. SC_INTERNAL_SERVER_ERROR);
                } catch(Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
            
        },
        edit {
            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, UserManager userManager, User user, String action) throws IOException {
                try {
                    Right right = createRightFromPost(req, rightsManager, userManager);
                    rightsManager.updateRight(right);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                    resp.sendError(HttpServletResponse. SC_INTERNAL_SERVER_ERROR);
                } catch(Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
        },

        create {
            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, UserManager userManager, User user, String action) throws IOException {
                try {
                    Right right = createRightFromPost(req, rightsManager, userManager);
                    rightsManager.insertRight(right);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                    resp.sendError(HttpServletResponse. SC_INTERNAL_SERVER_ERROR);
                } catch(Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
        };
        abstract void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, UserManager userManager, User user, String action) throws IOException;
    }
    
    
    static enum GetAction {

        showrights {

            @Override
            void doAction(ServletContext context, HttpServletRequest req,  HttpServletResponse resp, Map parameterMap,FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, User user, String[] path, String[] models, String action, ResourceBundle resourceBundle, StringTemplateGroup stGroup, StringTemplateGroup jsData) {
                try {
                    String uuid = req.getParameter(UUID_PARAMETER);
                    List<String> saturatedPath = rightsManager.saturatePathAndCreatesPIDs(uuid, path);
                    Right[] findRights = rightsManager.findRights((String[]) saturatedPath.toArray(new String[saturatedPath.size()]), action, user);
                    findRights = SortingRightsUtils.sortRights(findRights, saturatedPath);
                    StringTemplate template = stGroup.getInstanceOf("rightsTable");
                    template.setAttribute("rights", RightWrapper.wrapRights(findRights));
                    template.setAttribute("uuid", uuid);
                    
                    template.setAttribute("action",new SecuredActionWrapper(resourceBundle, SecuredActions.findByFormalName(action)));
                    resp.getOutputStream().write(template.toString().getBytes("UTF-8"));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
        }, 

        editrightjsdata {
            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, Map parameterMap, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, User user, String[] path, String[] models, String action, ResourceBundle resourceBundle, StringTemplateGroup htmlForms, StringTemplateGroup jsData) {
                try {
                    String uuid = req.getParameter(UUID_PARAMETER);
                    String rightId = req.getParameter("rightid");
                    Right right = new RightWrapper(rightsManager.findRightById(Integer.parseInt(rightId)));
                    System.out.println(right.getUser());    
                    
                    StringTemplate template = jsData.getInstanceOf("editRightData");
                    RightCriteriumParams[] allParams = rightsManager.findAllParams();
                    template.setAttribute("allParams", CriteriumParamsWrapper.wrapCriteriumParams(allParams));
                    template.setAttribute("allCriteriums", CriteriumWrapper.wrapCriteriums(CriteriumsLoader.criteriums()));
                    template.setAttribute("right", right);
                    template.setAttribute("criterium", right.getCriterium());
                    template.setAttribute("criteriumParams", right.getCriterium() != null ? right.getCriterium().getCriteriumParams() : null);
                    
                    String content = template.toString();
                    resp.getOutputStream().write(content.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (InstantiationException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (IllegalAccessException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
            
        },
        newrightjsdata {

            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, Map parameterMap, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, User user,  String[] path, String[] models, String action, ResourceBundle resourceBundle, StringTemplateGroup htmlForms, StringTemplateGroup jsData) {
                try {
                    StringTemplate template = jsData.getInstanceOf("newRightData");
                    RightCriteriumParams[] allParams = rightsManager.findAllParams();
                    template.setAttribute("allParams", CriteriumParamsWrapper.wrapCriteriumParams(allParams));
                    template.setAttribute("allCriteriums", CriteriumWrapper.wrapCriteriums(CriteriumsLoader.criteriums()));
                    
                    String content = template.toString();
                    resp.getOutputStream().write(content.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (InstantiationException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (IllegalAccessException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
            
        },
        
        
        
        newright {

            
            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, Map parameterMap, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, User user, String[] path,String[]models, String action, ResourceBundle resourceBundle,StringTemplateGroup htmlForms, StringTemplateGroup jsData) {
                String uuid = req.getParameter(UUID_PARAMETER);
                try {
                    StringTemplate template = htmlForms.getInstanceOf("rightDialog");
                    HashMap<String, String> titles = TitlesForObjects.createFinerTitles(fedoraAccess,rightsManager, uuid, path, models, resourceBundle);

                    List<String> saturatedPath = rightsManager.saturatePathAndCreatesPIDs(uuid, path);
                    
                    RightCriteriumParams[] allParams = rightsManager.findAllParams();
                    template.setAttribute("allParams", allParams);
                    template.setAttribute("titles", titles);
                    template.setAttribute("uuid", uuid);  
                    template.setAttribute("action", new SecuredActionWrapper(resourceBundle, SecuredActions.findByFormalName(action)));
                    template.setAttribute("objects", saturatedPath);
                    template.setAttribute("criteriumNames",CriteriumsLoader.criteriumClasses());
                    
                    
                    String content = template.toString();

                    resp.getOutputStream().write(content.getBytes("UTF-8"));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
            
        },
        
        showglobalrights {

            @Override
            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, Map parameterMap, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, User user, String[] path,String[]models, String action, ResourceBundle resourceBundle,StringTemplateGroup htmlForms, StringTemplateGroup jsData) {
                String uuid = req.getParameter(UUID_PARAMETER);
                try {
                    StringTemplate template = htmlForms.getInstanceOf("rightsForRepository");
                    template.setAttribute("actions", SecuredActionWrapper.wrap(resourceBundle, SecuredActions.values()));
                    //template.setAttribute("uuid", "uuid:1");
                    
                    String content = template.toString();
                    resp.getOutputStream().write(content.getBytes("UTF-8"));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
            }
            
        };

        abstract void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, Map parametersMap, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, User user,  String[] path, String[] models, String action, ResourceBundle resourceBundle, StringTemplateGroup htmlForms, StringTemplateGroup jsData);
    }
    
    
    private static StringTemplateGroup stFormsGroup() throws IOException {
        InputStream stream = RightsServlet.class.getResourceAsStream("rights.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        StringTemplateGroup group = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        return group;
    }

    private static StringTemplateGroup stJSDataGroup() throws IOException {
        InputStream stream = RightsServlet.class.getResourceAsStream("rightsData.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        StringTemplateGroup group = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        return group;
    }


    public static String getJSData(String data, String html) {
        StringTemplate template = new StringTemplate("{\"data\":\"$data$\",\"html\":\"$html$\"}");
        template.setAttribute("data", data);
        template.setAttribute("html", html);
        return template.toString();
    }



    public static Right createRightFromPostIds(HttpServletRequest req, RightsManager rightsManager, UserManager userManager) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String rightId = req.getParameter("rightId");
        return rightsManager.findRightById(Integer.parseInt(rightId));
    }
    
    public static Right createRightFromPost(HttpServletRequest req, RightsManager rightsManager, UserManager userManager) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String rightId = req.getParameter("rightId");
        String rightCriteriumId = req.getParameter("rightCriteriumId");
        String critParamId = req.getParameter("rightCriteriumParamId");

        String uuidHidden = req.getParameter("uuidHidden");
        String criteriumHidden = req.getParameter("criteriumHidden");
        String priorityHidden = req.getParameter("priorityHidden");
        
        String paramsAsociatedHidden = req.getParameter("paramsAssocatedHidden");
        String paramsHidden = req.getParameter("paramsHidden");
        String paramsShortHidden = req.getParameter("paramsShortDescriptionHidden");
        String paramsLongDescHidden = req.getParameter("paramsLongDescriptionHidden");
        
        String userTypeHidden = req.getParameter("userTypeHidden");
        String userIdHidden = req.getParameter("userIdHidden");
        String formalActionHidden = req.getParameter("formalActionHidden");
        
        RightCriteriumParams params = null;
        if ((critParamId != null) && (!critParamId.equals("")) && (Integer.parseInt(critParamId) > 0)) {
            params = rightsManager.findParamById(Integer.parseInt(critParamId));
        } else {
            if ("true".equals(paramsAsociatedHidden)) {
                params = new RightCriteriumParamsImpl(-1);
                params.setObjects(paramsHidden.split(";"));
                params.setShortDescription(paramsShortHidden);
                params.setLongDescription(paramsLongDescHidden);
            }
        }

        RightCriterium rightCriterium = null;
        if ((rightCriteriumId != null) && (!rightCriteriumId.equals("")) && (Integer.parseInt(rightCriteriumId) > 0)) {
            rightCriterium = rightsManager.findRightCriteriumById(Integer.parseInt(rightCriteriumId));
            if (!rightCriterium.getQName().equals(criteriumHidden)) {
                rightCriterium = ClassRightCriterium.instanceCriterium((Class<? extends RightCriterium>) Class.forName(criteriumHidden));
                rightCriterium.setId(-1);
            }
        } else {
            rightCriterium = ClassRightCriterium.instanceCriterium((Class<? extends RightCriterium>) Class.forName(criteriumHidden));
            rightCriterium.setId(-1);
        
        }
        if ((priorityHidden != null) && (!priorityHidden.equals(""))) {
            rightCriterium.setFixedPriority(Integer.parseInt(priorityHidden));
        }
        rightCriterium.setCriteriumParams(params);

        
        AbstractUser auser = null;
        if (userTypeHidden.equals("user")) {
            auser = userManager.findUserByLoginName(userIdHidden);
        } else {
            auser =  userManager.findGroupByName(userIdHidden);
        }

        if (auser == null) {
            throw new IllegalStateException("cannot find user by given id '"+userIdHidden+"'");
        }
        
        SecuredActions securedAction = SecuredActions.findByFormalName(formalActionHidden);
        Right right = null;
        if (securedAction != null) {
            if ((rightId != null) && (!rightId.equals("")) && (Integer.parseInt(rightId) > 0)) {
                right = rightsManager.findRightById(Integer.parseInt(rightId));
                right.setCriterium(rightCriterium);
            } else {
                right = new RightImpl(-1, rightCriterium, uuidHidden, securedAction.getFormalName(), auser);
            }
        } else {
            throw new IllegalArgumentException("cannot find action '"+formalActionHidden+"'");
        }
        right.setAction(formalActionHidden);
        return right;
    }

    
}
