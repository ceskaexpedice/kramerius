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
package cz.incad.Kramerius.views.item.menu;

import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.logging.Level;

import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.Initializable;
import cz.incad.Kramerius.views.AbstractViewObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;

/**
 * Ctx menu holder 
 * @author pavels
 */
public class ContextMenuItemsHolder extends AbstractViewObject implements Initializable  {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ContextMenuItemsHolder.class.getName());
    
    /* menu items*/
    private List<ContextMenuItem> items = new ArrayList<ContextMenuItem>();
    private List<ContextMenuItem> adminItems = new ArrayList<ContextMenuItem>();
    
    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Inject
    KConfiguration kconfig;
    
    @Inject
    Provider<Locale> localesProvider;
    

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    
    
    @Override
    public void init() {
    
        
        String i18nServlet ="i18n";
        String pid = this.requestProvider.get().getParameter(UUID_PARAMETER);
        try {
            boolean imgfullAvailable = this.fedoraAccess.isImageFULLAvailable(pid);
            String mimeType = imgfullAvailable ? this.fedoraAccess.getImageFULLMimeType(pid) : "";

            items.add(new ContextMenuItem("administrator.menu.showmetadata", "", "viewMetadata", "", false));
            items.add(new ContextMenuItem("administrator.menu.persistenturl", "", "persistentURL", "", true));
            
            if (!isPDFMimeType(mimeType)) {
                items.add(new ContextMenuItem("administrator.menu.generatepdf", "_data_x_role", "generatepdf", "", true));
            }

            items.add(new ContextMenuItem("administrator.menu.downloadOriginal", "_data_x_role", "downloadOriginalItem", "", true));
            items.add(new ContextMenuItem("administrator.menu.feedback", "_data_x_role", "feedbackDialog", "", true));
            if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
                items.add(new ContextMenuItem("administrator.menu.favorites.add", "_data_x_role", "addToFavorites",
                        "", true));
            }
            
            if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
                items.add(new ContextMenuItem("administrator.menu.print", "", "ctxPrint", "", true));
                adminItems.add(new ContextMenuItem("administrator.menu.reindex", "_data_x_role", "reindex", "", true));
                adminItems.add(new ContextMenuItem("administrator.menu.deletefromindex", "_data_x_role", "deletefromindex", "", true));
                adminItems.add(new ContextMenuItem("administrator.menu.deleteuuid", "_data_x_role", "deletePid", "", true));
                adminItems.add(new ContextMenuItem("administrator.menu.setpublic", "_data_x_role", "changeFlag.change", "", true));
                adminItems.add(new ContextMenuItem("administrator.menu.exportFOXML", "_data_x_role", "exportFOXML", "", true));

                if (!isPDFMimeType(mimeType)) {
                    adminItems.add(new ContextMenuItem("administrator.menu.exportcd", "_data_x_role", "exportToCD",
                            "'img','" + i18nServlet + "','" + localesProvider.get().getISO3Country() + "','" + localesProvider.get().getISO3Language() + "'", false));

                    adminItems.add(new ContextMenuItem("administrator.menu.exportdvd", "_data_x_role", "exportToDVD",
                            "'img','" + i18nServlet + "','" + localesProvider.get().getISO3Country() + "','" + localesProvider.get().getISO3Language() + "'", false));
                }

                adminItems.add(new ContextMenuItem("administrator.menu.generateDeepZoomTiles", "_data_x_role", "generateDeepZoomTiles", "", true));
                adminItems.add(new ContextMenuItem("administrator.menu.deleteGeneratedDeepZoomTiles", "_data_x_role", "deleteGeneratedDeepZoomTiles", "", true));

                adminItems.add(new ContextMenuItem("administrator.menu.showrights", "_data_x_role", "securedActionsTableForCtxMenu",
                        "'" + SecuredActions.READ.getFormalName() + "', '" + SecuredActions.ADMINISTRATE.getFormalName() + "'", true));

                java.util.logging.Logger tlogger = java.util.logging.Logger.getLogger(ContextMenuItemsHolder.class.getName());
                tlogger.info("configuration .... ");
                if (this.kconfig.getSecuredAditionalStreams() != null && this.kconfig.getSecuredAditionalStreams().length > 0) {
                    adminItems.add(new ContextMenuItem("administrator.menu.showstremrights", "_data_x_role", "securedStreamsTableForCtxMenu",
                            "'" + SecuredActions.READ.getFormalName() + "', '" + SecuredActions.ADMINISTRATE.getFormalName() + "'", true));
                }

                adminItems.add(new ContextMenuItem("administrator.menu.editor", "_data_x_role", "openEditor",
                        "'" + kconfig.getEditorURL() + "'", true));

                adminItems.add(new ContextMenuItem("administrator.menu.virtualcollection.add", "_data_x_role", "vcAddToVirtualCollection",
                        "", true));


            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (MissingResourceException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
        

    public List<ContextMenuItem> getAdminItems() {
        return new ArrayList<ContextMenuItem>(this.adminItems);
    }    
    
    public List<ContextMenuItem> getItems() {
        return new ArrayList<ContextMenuItem>(this.items);
    }
    
    public boolean isPDFMimeType(String mimeType) {
        return ImageMimeType.PDF.getValue().toLowerCase().equals(mimeType.toLowerCase());
    }
}
