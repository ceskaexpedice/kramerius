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
package cz.incad.Kramerius.exts.menu.main.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import cz.incad.Kramerius.exts.menu.main.MainMenu;
import cz.incad.Kramerius.exts.menu.main.MainMenuPart;
import cz.incad.Kramerius.exts.menu.main.impl.MainMenuImpl;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.AdminMenuPartImpl;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.Convert;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.DefaultWMock;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.Enumerator;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.GlobalRightsAdministration;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.Import;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.ImportMonographs;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.ImportPeriodicals;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.IndexerAdministration;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.MetadataEditor;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.ParametrizedConvertMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.ParametrizedImportMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.ProcessesDialog;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.ReplicationRights;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.RolesEditor;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.UsersAdministration;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.VirtualCollectionsAdministration;
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.WMock;
import cz.incad.Kramerius.exts.menu.main.impl.pub.PublicMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.pub.PublicMenuPartImpl;
import cz.incad.Kramerius.exts.menu.main.impl.pub.items.ChangePassword;
import cz.incad.Kramerius.exts.menu.main.impl.pub.items.SaveProfile;
import cz.incad.Kramerius.exts.menu.main.impl.pub.items.ShowProfile;

/**
 * Menu configuration bean
 * @author pavels
 */
public class MenuConfiguration extends AbstractModule {

    @Override
    protected void configure() {
        // casti menu
        Multibinder<MainMenuPart> parts
            = Multibinder.newSetBinder(binder(), MainMenuPart.class);
        parts.addBinding().to(PublicMenuPartImpl.class);
        parts.addBinding().to(AdminMenuPartImpl.class);
        
        
        // polozky public menu
        Multibinder<PublicMenuItem> publicItems
        = Multibinder.newSetBinder(binder(), PublicMenuItem.class);
        publicItems.addBinding().to(ShowProfile.class);
        publicItems.addBinding().to(SaveProfile.class);
        publicItems.addBinding().to(ChangePassword.class);
        
        // polozky admin menu
        Multibinder<AdminMenuItem> adminItems
        = Multibinder.newSetBinder(binder(), AdminMenuItem.class);
        adminItems.addBinding().to(ProcessesDialog.class);
        adminItems.addBinding().to(ImportMonographs.class);
        adminItems.addBinding().to(ImportPeriodicals.class);
        adminItems.addBinding().to(ImportPeriodicals.class);
        adminItems.addBinding().to(IndexerAdministration.class);
        adminItems.addBinding().to(GlobalRightsAdministration.class);
        adminItems.addBinding().to(Enumerator.class);
        adminItems.addBinding().to(ReplicationRights.class);
        adminItems.addBinding().to(Convert.class);
        adminItems.addBinding().to(Import.class);
        adminItems.addBinding().to(MetadataEditor.class);
        adminItems.addBinding().to(RolesEditor.class);
        adminItems.addBinding().to(UsersAdministration.class);
        adminItems.addBinding().to(VirtualCollectionsAdministration.class);

        // pridani parametrizovanych procesu
        adminItems.addBinding().to(ParametrizedImportMenuItem.class);
        adminItems.addBinding().to(ParametrizedConvertMenuItem.class);

//        adminItems.addBinding().to(WMock.class);
//        adminItems.addBinding().to(DefaultWMock.class);
        
        // menu
        bind(MainMenu.class).to(MainMenuImpl.class);
    }

    
}
