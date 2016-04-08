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
package cz.incad.Kramerius.exts.menu.context.impl.pub.items;


import static cz.incad.utils.IKeys.PID_PARAMETER;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.exts.menu.context.impl.AbstractContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.pub.PublicContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.utils.MenuMimeTypesUtils;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Generate PDF menu item
 * @author pavels
 */
public class GeneratePDF extends AbstractContextMenuItem implements PublicContextMenuItem {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GeneratePDF.class.getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    

    @Override
    public boolean isMultipleSelectSupported() {
        return true;
    }

    @Override
    public boolean isRenderable() {
        return super.isRenderable() && isEnabledForMimeType();
    }

    public boolean isEnabledForMimeType() {
        try {
            String pid = this.requestProvider.get().getParameter(PID_PARAMETER);
            // hledani 
            if (pid == null) {
            	return true;
            }
        
            //zobrazujeme titul 
            String mimeType = MenuMimeTypesUtils.mimeTypeDisect(this.fedoraAccess.findFirstViewablePid(pid), this.fedoraAccess);
            return !MenuMimeTypesUtils.isPDFMimeType(mimeType);

        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
    }

    @Override	
    public String getRenderedItem() throws IOException {
        return super.renderContextMenuItem("javascript:generatepdf();", "administrator.menu.generatepdf", SecuredActions.SHOW_CLIENT_PDF_MENU.getFormalName());
    }


}
