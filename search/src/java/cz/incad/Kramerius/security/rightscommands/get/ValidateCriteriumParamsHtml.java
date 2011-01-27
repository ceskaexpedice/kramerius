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
package cz.incad.Kramerius.security.rightscommands.get;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;

import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.impl.criteria.CriteriumsLoader;

public class ValidateCriteriumParamsHtml extends ServletRightsCommand {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ValidateCriteriumParamsHtml.class.getName());
    
    @Inject
    RightCriteriumLoader criteriumLoader;

    @Inject
    RightCriteriumContextFactory ctxFactory;
    
    @Override
    public void doCommand() {
        try {
            HttpServletRequest request = this.requestProvider.get();
            String par = request.getParameter("criterium");
            String uuid = request.getParameter("uuid");
            String params = request.getParameter("params");
            RightCriterium criterium = criteriumLoader.getCriterium(par);
            RightCriteriumContext ctx = ctxFactory.create(uuid, this.userProvider.get(), request.getRemoteHost(), request.getRemoteAddr());
            criterium.setEvaluateContext(ctx);
            boolean validated = criterium.validateParams(params);
            StringTemplate template = ServletRightsCommand.stFormsGroup().getInstanceOf("criteriumParamValidate");
            template.setAttribute("validated", validated);

            String content = template.toString();
            this.responseProvider.get().getOutputStream().write(content.getBytes("UTF-8"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }

    }
}
