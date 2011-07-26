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
package cz.incad.Kramerius.admins.commands;

import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.admins.AdminCommand;
import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.users.LoggedUsersSingleton;

public class ChangeVisibililtyFlagHtml extends AdminCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ChangeVisibililtyFlagHtml.class.getName());

    @Inject
    UserManager userManager;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Override
    public void doCommand() {
        try {
            if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
                String uuid = requestProvider.get().getParameter(UUID_PARAMETER);
                PolicyFlag policyFlag = PolicyFlag.NONE;
                Document relsExt = fedoraAccess.getRelsExt(uuid);
                if (relsExt != null) {
                    policyFlag = PolicyFlag.findByVal(getPolicyVal(relsExt));
                }
                StringTemplate template = AdminCommand.stFormsGroup().getInstanceOf("changeVisibilityFlag");
                Map<String, String> bundle = bundleToMap(); {
                    bundle.put("administrator.dialogs.changevisibility.flag", MessageFormat.format(bundle.get("administrator.dialogs.changevisibility.flag"),policyFlag.value));
                }
                Map<String,Boolean> selection = new HashMap<String, Boolean>(); {
                    selection.put("public", policyFlag == PolicyFlag.PUBLIC);
                    selection.put("private", policyFlag == PolicyFlag.PRIVATE);
                }
                template.setAttribute("bundle",bundle);
                template.setAttribute("policyFlag", policyFlag);
                template.setAttribute("selection", selection);
                this.responseProvider.get().setContentType("text/html");
                this.responseProvider.get().setCharacterEncoding("UTF-8");
                this.responseProvider.get().getWriter().println(template.toString());
            } else {
                this.responseProvider.get().sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }
    
    public String getPolicyVal(Document relsExt) throws XPathExpressionException {
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("//kramerius:policy/text()");
        Object policy = expr.evaluate(relsExt, XPathConstants.STRING);
        return policy != null ? policy.toString() : null;
    }
    
    enum PolicyFlag {
        
        NONE(null),PUBLIC("policy:public"),PRIVATE("policy:private");

        String value;

        private PolicyFlag(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
        
        @Override
        public String toString() {
            return value;
        }



        public static PolicyFlag findByVal(String val) {
            PolicyFlag[] vals = values();
            for (PolicyFlag flag : vals) {
                if ((val != null) && (flag.value != null) && (flag.value.equals(val))) {
                    return flag;
                } else if ((val == null) && (flag.value == null)) {
                    return flag;
                }
            }
            return null;
        }
        
    }
}
