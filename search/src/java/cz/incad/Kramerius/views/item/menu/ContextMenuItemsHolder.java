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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.Initializable;
import cz.incad.Kramerius.views.AbstractViewObject;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Ctx menu holder

 * @author pavels
 */
public class ContextMenuItemsHolder extends AbstractViewObject implements Initializable  {
    /* menu items*/
    private List<ContextMenuItem> items = new ArrayList<ContextMenuItem>();
    
    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Inject
    KConfiguration kconfig;
    
    @Inject
    Provider<Locale> localesProvider;
    
    
    @Override
    public void init() {
        
        String i18nServlet ="something..";
        
        items.add(new ContextMenuItem("administrator.menu.showmetadata", "", "viewMetadata", "", false));
        items.add(new ContextMenuItem("administrator.menu.persistenturl", "", "persistentURL", "", true));
        items.add(new ContextMenuItem("administrator.menu.generatepdf", "_data_x_role", "generatepdf", "", true));
        items.add(new ContextMenuItem("administrator.menu.downloadOriginal", "_data_x_role", "downloadOriginalItem", "", true));

        if (this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
            items.add(new ContextMenuItem("administrator.menu.print", "", "ctxPrint", "", true));
            items.add(new ContextMenuItem("administrator.menu.reindex", "_data_x_role", "reindex", "", true));
            items.add(new ContextMenuItem("administrator.menu.deletefromindex", "_data_x_role", "deletefromindex", "", true));
            items.add(new ContextMenuItem("administrator.menu.deleteuuid", "_data_x_role", "deletePid", "", true));
            items.add(new ContextMenuItem("administrator.menu.setpublic", "_data_x_role", "changeFlag.change", "", true));
            items.add(new ContextMenuItem("administrator.menu.exportFOXML", "_data_x_role", "exportFOXML", "", true));
            items.add(new ContextMenuItem("administrator.menu.exportcd", "_data_x_role", "generateStatic",
                    "'static_export_CD','img','" + i18nServlet + "','" + localesProvider.get().getISO3Country() + "','" + localesProvider.get().getISO3Language() + "'", true));

            items.add(new ContextMenuItem("administrator.menu.exportdvd", "_data_x_role", "generateStatic",
                    "'static_export_CD','img','" + i18nServlet + "','" + localesProvider.get().getISO3Country() + "','" + localesProvider.get().getISO3Language() + "'", true));
            items.add(new ContextMenuItem("administrator.menu.generateDeepZoomTiles", "_data_x_role", "generateDeepZoomTiles", "", true));
            items.add(new ContextMenuItem("administrator.menu.deleteGeneratedDeepZoomTiles", "_data_x_role", "deleteGeneratedDeepZoomTiles", "", true));

            items.add(new ContextMenuItem("administrator.menu.showrights", "_data_x_role", "securedActionsTableForCtxMenu",
                    "'" + SecuredActions.READ.getFormalName() + "', '" + SecuredActions.ADMINISTRATE.getFormalName() + "'", true));
            items.add(new ContextMenuItem("administrator.menu.editor", "_data_x_role", "openEditor",
                    "'" + kconfig.getEditorURL() + "'", true));

        }
    }
        
    
    public List<ContextMenuItem> getItems() {
        return new ArrayList<ContextMenuItem>(this.items);
    }
}
