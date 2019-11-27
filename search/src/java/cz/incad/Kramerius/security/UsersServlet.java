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
import java.util.Locale;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.security.userscommands.get.ChangePassword;
import cz.incad.Kramerius.security.userscommands.get.EditRoleHtml;
import cz.incad.Kramerius.security.userscommands.get.HintAllGroupsTable;
import cz.incad.Kramerius.security.userscommands.get.HintAllUsersTable;
import cz.incad.Kramerius.security.userscommands.get.HintGroupsForUserTable;
import cz.incad.Kramerius.security.userscommands.get.HintUsersForGroup;
import cz.incad.Kramerius.security.userscommands.get.NewRoleHtml;
import cz.incad.Kramerius.security.userscommands.get.PublicUserActivation;
import cz.incad.Kramerius.security.userscommands.get.ShowRolesHtml;
import cz.incad.Kramerius.security.userscommands.get.UsersJSAutocomplete;
import cz.incad.Kramerius.security.userscommands.get.ValidationUserName;
import cz.incad.Kramerius.security.userscommands.post.CreateRole;
import cz.incad.Kramerius.security.userscommands.post.DeleteRole;
import cz.incad.Kramerius.security.userscommands.post.RegisterPublicUser;
import cz.incad.Kramerius.security.userscommands.post.SaveNewPassword;
import cz.incad.Kramerius.security.userscommands.post.SaveRole;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class UsersServlet extends GuiceServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(UsersServlet.class.getName());

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

    @Inject
    Provider<HttpServletResponse> responseProvider;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uuid = req.getParameter(UUID_PARAMETER);
        try {
            PIDParser pidParser = new PIDParser("uuid:" + uuid);
            pidParser.objectPid();

            String action = req.getParameter("action");
            this.responseProvider.get().setContentType("text/html");
            try {
                GetCommandsEnum command = GetCommandsEnum.valueOf(action);
                command.doAction(getInjector());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String action = req.getParameter("action");
        try {
            PostCommandsEnum command = PostCommandsEnum.valueOf(action);
            command.doAction(getInjector());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    enum PostCommandsEnum {

        /** zmena hesla */
        savenewpswd(SaveNewPassword.class), saverole(SaveRole.class), deleterole(DeleteRole.class), 
        newrole(CreateRole.class),
        
        registernew(RegisterPublicUser.class);

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

    enum GetCommandsEnum {

//TODO: Delete !!
//        /** zobrazeni roli */
//        showroles(ShowRolesHtml.class),
//
//        /** editace roli */
//        editrole(EditRoleHtml.class),
//
//        /** nova role */
//        newrole(NewRoleHtml.class),
//
//        /** dialog pro zmenu hesla */
//        changepswd(ChangePassword.class),
//
//        /** tabulka uzivatelu */
//        hintallusers(HintAllUsersTable.class),
//
//        /** zobrazi skupiny pro uzivatele */
//        hintgroupforuser(HintGroupsForUserTable.class),
//
//        /** zobrazi uzivatele pro skupinu */
//        hintusersforgroup(HintUsersForGroup.class),
//
//        /** tabulka uzivatelu */
//        hintallgroups(HintAllGroupsTable.class),
//        /** zobrazeni prav */
//        userjsautocomplete(UsersJSAutocomplete.class),

        validUserName(ValidationUserName.class),
        activation(PublicUserActivation.class);

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

    // static enum GetAction {
    // userjsautocomplete {
    // @Override
    // void doAction(ServletContext context, HttpServletRequest req,
    // HttpServletResponse resp, Map parametersMap, FedoraAccess fedoraAccess,
    // SolrAccess solrAccess, RightsManager rightsManager, UserManager
    // userManager, User user, ResourceBundle resourceBundle,
    // StringTemplateGroup htmlForms, StringTemplateGroup jsData) throws
    // IOException {
    // List<AbstractUser> ausers = new ArrayList<AbstractUser>();
    // String autocompletetype = req.getParameter("autcompletetype");
    // String prefix = req.getParameter("t");
    // try {
    // UserFieldParser fparser = new UserFieldParser(prefix);
    // fparser.parseUser();
    // prefix = fparser.getUserValue();
    // } catch (LexerException e) {
    // LOGGER.log(Level.SEVERE, e.getMessage(),e);
    // }
    //
    // if (autocompletetype.equals("group")) {
    // Group[] groups = userManager.findGroupByPrefix(prefix.trim());
    // for (Group grp : groups) {
    // ausers.add(grp);
    // }
    // } else {
    // User[] users = userManager.findUserByPrefix(prefix.trim());
    // for (User auser : users) {
    // ausers.add(auser);
    // }
    // }
    //
    // StringTemplate template = jsData.getInstanceOf("userAutocomplete");
    // template.setAttribute("type", autocompletetype);
    // template.setAttribute("users", ausers);
    //
    // String content = template.toString();
    // resp.getOutputStream().write(content.getBytes("UTF-8"));
    //
    // }
    // };
    //
    // abstract void doAction(ServletContext context, HttpServletRequest req,
    // HttpServletResponse resp, Map parametersMap,
    // FedoraAccess fedoraAccess, SolrAccess solrAccess,
    // RightsManager rightsManager, UserManager manager,
    // User user, ResourceBundle resourceBundle,
    // StringTemplateGroup htmlForms, StringTemplateGroup jsData) throws
    // UnsupportedEncodingException, IOException;
    //
    // }

}
