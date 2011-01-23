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
import java.util.ArrayList;
import java.util.Arrays;
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
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.security.rightscommands.get.EditRightsJSData;
import cz.incad.Kramerius.security.rightscommands.get.NewRightHtml;
import cz.incad.Kramerius.security.rightscommands.get.NewRightJSData;
import cz.incad.Kramerius.security.rightscommands.get.ShowRightsHtml;
import cz.incad.Kramerius.security.rightscommands.get.ShowsActionsTableHtml;
import cz.incad.Kramerius.security.rightscommands.post.Create;
import cz.incad.Kramerius.security.rightscommands.post.Delete;
import cz.incad.Kramerius.security.rightscommands.post.Edit;
import cz.incad.Kramerius.security.strenderers.AbstractUserWrapper;
import cz.incad.Kramerius.security.strenderers.CriteriumParamsWrapper;
import cz.incad.Kramerius.security.strenderers.CriteriumWrapper;
import cz.incad.Kramerius.security.strenderers.RightWrapper;
import cz.incad.Kramerius.security.strenderers.SecuredActionWrapper;
import cz.incad.Kramerius.security.strenderers.TitlesForObjects;
import cz.incad.Kramerius.security.utils.UserFieldParser;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.IsActionAllowed;
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
    transient UserManager userManager;

    @Inject
    transient IsActionAllowed actionAllowed;
    
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uuid = req.getParameter(UUID_PARAMETER);
        try {
            PIDParser pidParser = new PIDParser("uuid:"+uuid);
            pidParser.objectPid();

            String action = req.getParameter("action");
            
            try {
                GetCommandsEnum command = GetCommandsEnum.valueOf(action);
                command.doAction(getInjector());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
            }
            
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

   

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //TODO: ochranit akci
      String action = req.getParameter("action");
        
        try {
            PostCommandsEnum command = PostCommandsEnum.valueOf(action);
            command.doAction(getInjector());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }

    }


    static enum PostCommandsEnum {
        delete(Delete.class),
        edit(Edit.class),
        create(Create.class);
        
        private Class<? extends ServletCommand> commandClass;
        
        private PostCommandsEnum(Class<? extends ServletCommand> command) {
            this.commandClass = command;
        }
        
        public void doAction(Injector injector) throws InstantiationException, Exception {
            ServletCommand command = commandClass.newInstance();
            injector.injectMembers(command);
            command.doCommand();
        }
        
    }


//    static enum PostAction {
//        delete {
//
//            @Override
//            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, UserManager userManager, User user, String action) throws IOException {
//                try {
//                    Right right = createRightFromPostIds(req, rightsManager, userManager);
//                    rightsManager.deleteRight(right);
//                } catch (NumberFormatException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
//                } catch (SQLException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
//                    resp.sendError(HttpServletResponse. SC_INTERNAL_SERVER_ERROR);
//                } catch(Exception e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
//                }
//            }
//            
//        },
//        edit {
//            @Override
//            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, UserManager userManager, User user, String action) throws IOException {
//                try {
//                    Right right = createRightFromPost(req, rightsManager, userManager);
//                    rightsManager.updateRight(right);
//                } catch (NumberFormatException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
//                } catch (SQLException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
//                    resp.sendError(HttpServletResponse. SC_INTERNAL_SERVER_ERROR);
//                } catch(Exception e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
//                }
//            }
//        },
//
//        create {
//            @Override
//            void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, UserManager userManager, User user, String action) throws IOException {
//                try {
//                    Right right = createRightFromPost(req, rightsManager, userManager);
//                    rightsManager.insertRight(right);
//                } catch (NumberFormatException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
//                } catch (SQLException e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
//                    resp.sendError(HttpServletResponse. SC_INTERNAL_SERVER_ERROR);
//                } catch(Exception e) {
//                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
//                }
//            }
//        };
//        
//        
//        
//        abstract void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, FedoraAccess fedoraAccess, SolrAccess solrAccess, RightsManager rightsManager, UserManager userManager, User user, String action) throws IOException;
//    }
    
    
    static enum GetCommandsEnum {

        /** zobrazeni prav */
        showrights(ShowRightsHtml.class),
        /** zobrazeni tabulku akci - tlacitka pro zmenu */
        showglobalrights(ShowsActionsTableHtml.class),
        /** nove pravo */
        newright(NewRightHtml.class),
        /** editace prava - javascript */
        editrightjsdata(EditRightsJSData.class),
        /** nove pravo - javascript */
        newrightjsdata(NewRightJSData.class);

        private Class<? extends ServletCommand> commandClass;
        
        private GetCommandsEnum(Class<? extends ServletCommand> command) {
            this.commandClass = command;
        }
        
        public void doAction(Injector injector) throws InstantiationException, Exception {
            ServletCommand command = commandClass.newInstance();
            injector.injectMembers(command);
            command.doCommand();
        }
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
        String uuidHidden = req.getParameter("uuidHidden");
        String priorityHidden = req.getParameter("priorityHidden");

        String formalActionHidden = req.getParameter("formalActionHidden");
        
        
        RightCriteriumParams params = criteriumParamsFromPost(rightsManager, req);
        RightCriterium rightCriterium = criteriumFromPost(rightsManager, req, params);
        AbstractUser auser = userFromPost(userManager, req);
        
        SecuredActions securedAction = SecuredActions.findByFormalName(formalActionHidden);
        Right right = null;
        if (securedAction != null) {
            if ((rightId != null) && (!rightId.equals("")) && (Integer.parseInt(rightId) > 0)) {
                right = rightsManager.findRightById(Integer.parseInt(rightId));
                right.setCriterium(rightCriterium);
                right.setUser(auser);
            } else {
                right = new RightImpl(-1, rightCriterium, uuidHidden, securedAction.getFormalName(), auser);
            }
        } else {
            throw new IllegalArgumentException("cannot find action '"+formalActionHidden+"'");
        }
        
        if ((priorityHidden != null) && (!priorityHidden.equals(""))) {
            right.setFixedPriority(Integer.parseInt(priorityHidden));
        }
        
        right.setAction(formalActionHidden);
        return right;
    }



    public static AbstractUser userFromPost(UserManager userManager, HttpServletRequest req) {
        String userTypeHidden = req.getParameter("userTypeHidden");
        String userIdHidden = req.getParameter("userIdHidden");
        String user = userIdHidden;
        try {
            UserFieldParser ufieldParser = new UserFieldParser(user);
            ufieldParser.parseUser();
            user = ufieldParser.getUserValue();
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
        

        AbstractUser auser = null;
        if (userTypeHidden.equals("user")) {
            auser = userManager.findUserByLoginName(user.trim());
        } else {
            auser =  userManager.findGroupByName(user.trim());
        }

        if (auser == null) {
            throw new IllegalStateException("cannot find user by given id '"+user+"'");
        }
        return auser;
    }



    public static RightCriterium criteriumFromPost(RightsManager rightsManager, HttpServletRequest req, RightCriteriumParams params) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String rightCriteriumId = req.getParameter("rightCriteriumId");
        String criteriumHidden = req.getParameter("criteriumHidden");

        RightCriterium rightCriterium = null;
        if ((rightCriteriumId != null) && (!rightCriteriumId.equals("")) && (Integer.parseInt(rightCriteriumId) > 0)) {
            rightCriterium = rightsManager.findRightCriteriumById(Integer.parseInt(rightCriteriumId));
            if (!rightCriterium.getQName().equals(criteriumHidden)) {
                rightCriterium = ClassRightCriterium.instanceCriterium((Class<? extends RightCriterium>) Class.forName(criteriumHidden));
                rightCriterium.setId(-1);
            }
        } else if ((!criteriumHidden.equals("none") && (!"".equals(criteriumHidden.trim())))){
            rightCriterium = ClassRightCriterium.instanceCriterium((Class<? extends RightCriterium>) Class.forName(criteriumHidden));
            rightCriterium.setId(-1);

        }
        
        if (rightCriterium != null) {
            rightCriterium.setCriteriumParams(params);
        }
        return rightCriterium;
    }



    public static RightCriteriumParams criteriumParamsFromPost(RightsManager rightsManager, HttpServletRequest req) {
        String critParamId = req.getParameter("rightCriteriumParamId");
        String paramsAsociatedHidden = req.getParameter("paramsAssocatedHidden");
        String paramsHidden = req.getParameter("paramsHidden");
        String paramsShortHidden = req.getParameter("paramsShortDescriptionHidden");
        String paramsLongDescHidden = req.getParameter("paramsLongDescriptionHidden");

        RightCriteriumParams params = null;
        if ((critParamId != null) && (!critParamId.equals("")) && (Integer.parseInt(critParamId) > 0) && ("true".equals(paramsAsociatedHidden))) {
            params = rightsManager.findParamById(Integer.parseInt(critParamId));
        } else {
            if ("true".equals(paramsAsociatedHidden)) {
                params = new RightCriteriumParamsImpl(-1);
                params.setObjects(paramsHidden.split(";"));
                params.setShortDescription(paramsShortHidden);
                params.setLongDescription(paramsLongDescHidden);
            }
        }
        return params;
    }

    
}
