/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.Kramerius.exts.menu.main.impl;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.exts.menu.main.MainMenuItem;
import cz.incad.Kramerius.exts.menu.utils.GlobalRightsUtils;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Abstract main menu item
 * 
 * @author pavels
 */
public abstract class AbstractMainMenuItem implements MainMenuItem {

    @Inject
    protected ResourceBundleService resourceBundleService;

    @Inject
    protected Provider<Locale> localesProvider;

    @Inject
    protected Provider<HttpServletRequest> requestProvider;

    @Inject
    protected DefinitionManager definitionManager;

    protected boolean hasUserAllowedPlanProcess(String processDef) {
        LRProcessDefinition lrProcess = definitionManager.getLongRunningProcessDefinition(processDef);
        if (lrProcess != null && lrProcess.getSecuredAction() != null)
            return hasUserAllowedAction(lrProcess.getSecuredAction());
        else
            return hasUserAllowedAction(processDef);
    }

    protected boolean hasUserAllowedAction(String actionFormalName) {
        HttpServletRequest request = this.requestProvider.get();
        return GlobalRightsUtils.hasUserAllowedAction(actionFormalName, request);
    }
    
    /**
     * Disable or enable item by configuration
     */
    public boolean isEnabledByconfiguration() {
        String clzName = this.getClass().getName()+".enabled";
        return KConfiguration.getInstance().getConfiguration().getBoolean(clzName,true);
    }
    


    protected String renderMainMenuItem(String href, String labelKey, boolean newWindow) throws IOException {
        String label = this.resourceBundleService.getResourceBundle("labels", this.localesProvider.get()).getString(labelKey);
        return String.format("<div align=\"left\"> <a href=\"%s\"" + (newWindow ? " target=\"_blank\"" : "") + "> %s </a> </div>", href, label);
    }

}
