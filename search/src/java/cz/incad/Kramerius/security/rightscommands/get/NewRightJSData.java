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
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;

import cz.incad.Kramerius.security.ServletCommand;
import cz.incad.Kramerius.security.rightscommands.ServletRightsCommand;
import cz.incad.Kramerius.security.strenderers.CriteriumParamsWrapper;
import cz.incad.Kramerius.security.strenderers.CriteriumGuiWrapper;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.impl.criteria.CriteriumsLoader;

/**
 * Ziskani javascriptu pri zobrazeni formulare pro zadani noveho prava
 * @author pavels
 *
 */
public class NewRightJSData extends ServletRightsCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(NewRightJSData.class.getName());
   
    @Inject
    RightCriteriumWrapperFactory factory;

    @Override
    public void doCommand() {
        try {
            StringTemplate template = ServletRightsCommand.stJSDataGroup().getInstanceOf("newRightData");
            RightCriteriumParams[] allParams = rightsManager.findAllParams();
            template.setAttribute("allParams", CriteriumParamsWrapper.wrapCriteriumParams(allParams));
            List<RightCriteriumWrapper> criteriums = this.factory.createAllCriteriumWrappers();
            template.setAttribute("allCriteriums", CriteriumGuiWrapper.wrapCriteriums(criteriums, true));
            String content = template.toString();
            this.responseProvider.get().getOutputStream().write(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    
}
