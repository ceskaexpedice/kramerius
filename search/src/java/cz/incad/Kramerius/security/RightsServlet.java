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
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.logging.Level;

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
import cz.incad.Kramerius.security.rightscommands.post.Create;
import cz.incad.Kramerius.security.rightscommands.post.Delete;
import cz.incad.Kramerius.security.rightscommands.post.DeleteCriteriumParams;
import cz.incad.Kramerius.security.rightscommands.post.Edit;
import cz.incad.Kramerius.security.rightscommands.post.RenameCriteriumParams;
import cz.incad.Kramerius.security.userscommands.get.EditRoleHtml;
import cz.incad.Kramerius.security.userscommands.get.ShowRolesHtml;
import cz.incad.Kramerius.security.utils.UserFieldParser;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.security.impl.RightCriteriumParamsImpl;
import cz.incad.kramerius.security.impl.RightImpl;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class RightsServlet extends GuiceServlet {

    private static final String NONE_CONSTANT = "cz.incad.kramerius.security.impl.criteria.none";

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
    
    
    @Inject
    Provider<HttpServletResponse> responseProvider;

    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uuid = req.getParameter(UUID_PARAMETER);
        try {
            PIDParser pidParser = new PIDParser("uuid:"+uuid);
            pidParser.objectPid();

            String action = req.getParameter("action");
            this.responseProvider.get().setContentType("text/html");
                    
            throw new UnsupportedOperationException("get is unsupported");
            
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
        create(Create.class),
        deleteparams(DeleteCriteriumParams.class),
        renameparams(RenameCriteriumParams.class);
        
        
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
            auser =  userManager.findRoleByName(user.trim());
        }

        if (auser == null) {
            throw new IllegalStateException("cannot find user by given id '"+user+"'");
        }
        return auser;
    }



    /*
    public static RightCriteriumWrapper criteriumFromPost(RightsManager rightsManager, HttpServletRequest req, RightCriteriumParams params, RightCriteriumWrapperFactory factory) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String rightCriteriumId = req.getParameter("rightCriteriumId");
        String criteriumHidden = req.getParameter("criteriumHidden");
        if (criteriumHidden.equals(NONE_CONSTANT)) return null;
        
        RightCriteriumWrapper rightCriterium = null;
        if ((rightCriteriumId != null) && (!rightCriteriumId.equals("")) && (Integer.parseInt(rightCriteriumId) > 0)) {
            rightCriterium = rightsManager.findRightCriteriumById(Integer.parseInt(rightCriteriumId));
            if (!rightCriterium.getRightCriterium().getQName().equals(criteriumHidden)) {
                rightCriterium = factory.createCriteriumWrapper(criteriumHidden);
            }
        } else if ((!criteriumHidden.equals(NONE_CONSTANT) && (!"".equals(criteriumHidden.trim())))){
            rightCriterium = factory.createCriteriumWrapper(criteriumHidden);

        }
        if ((rightCriterium != null) && (rightCriterium.getRightCriterium().isParamsNecessary())){
            rightCriterium.setCriteriumParams(params);
        }
        return rightCriterium;
    }*/



    /*
    public static RightCriteriumParams criteriumParamsFromPost(RightsManager rightsManager, HttpServletRequest req) {
        String critParamId = req.getParameter("rightCriteriumParamId");
        String paramsHidden = req.getParameter("paramsHidden");
        String paramsShortHidden = req.getParameter("paramsShortDescriptionHidden");
        String paramsLongDescHidden = req.getParameter("paramsLongDescriptionHidden");

        RightCriteriumParams params = null;
        if ((critParamId != null) && (!critParamId.equals("")) && (Integer.parseInt(critParamId) > 0)) {
            params = rightsManager.findParamById(Integer.parseInt(critParamId));
            params.setObjects(paramsHidden.split(";"));
            params.setShortDescription(paramsShortHidden);
        } else {
            params = new RightCriteriumParamsImpl(-1);
            params.setObjects(paramsHidden.split(";"));
            params.setShortDescription(paramsShortHidden);
            params.setLongDescription(paramsLongDescHidden);
        }
        return params;
    }*/

    
}
