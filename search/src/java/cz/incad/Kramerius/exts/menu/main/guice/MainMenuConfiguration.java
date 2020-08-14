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
import cz.incad.Kramerius.exts.menu.main.impl.adm.items.*;
import cz.incad.Kramerius.exts.menu.main.impl.pub.PublicMainMenuItem;
import cz.incad.Kramerius.exts.menu.main.impl.pub.PublicMenuPartImpl;
import cz.incad.Kramerius.exts.menu.main.impl.pub.items.ChangePassword;
import cz.incad.Kramerius.exts.menu.main.impl.pub.items.SaveProfile;
import cz.incad.Kramerius.exts.menu.main.impl.pub.items.ShowProfile;

/**
 * Menu configuration bean
 * @author pavels
 */
public class MainMenuConfiguration extends AbstractModule {

    @Override
    protected void configure() {

        // casti menu
        Multibinder<MainMenuPart> parts
            = Multibinder.newSetBinder(binder(), MainMenuPart.class);
        parts.addBinding().to(PublicMenuPartImpl.class);
        parts.addBinding().to(AdminMenuPartImpl.class);
        
        
        // polozky public menu
        Multibinder<PublicMainMenuItem> publicItems
        = Multibinder.newSetBinder(binder(), PublicMainMenuItem.class);
        publicItems.addBinding().to(ShowProfile.class);
        publicItems.addBinding().to(SaveProfile.class);
        publicItems.addBinding().to(ChangePassword.class);
        
        // polozky admin menu
        Multibinder<AdminMenuItem> adminItems
        = Multibinder.newSetBinder(binder(), AdminMenuItem.class);
        adminItems.addBinding().to(ProcessesDialog.class);
        adminItems.addBinding().to(IndexerAdministration.class);
        adminItems.addBinding().to(IndexerMigration.class);
        adminItems.addBinding().to(RebuildProcessingIndex.class);


        adminItems.addBinding().to(UsersAdministration.class);
        adminItems.addBinding().to(RolesEditor.class);
        adminItems.addBinding().to(GlobalRightsAdministration.class);
        adminItems.addBinding().to(CriteriaEditor.class);
        // administrace
        // prava
        adminItems.addBinding().to(CollectionsRightsAdministration.class);
 
        adminItems.addBinding().to(MovingWallProcess.class);



        // pridani parametrizovanych procesu
        adminItems.addBinding().to(ParametrizedImport.class);
        adminItems.addBinding().to(NDKMetsImport.class);
        adminItems.addBinding().to(ParametrizedK4Replication.class);

        adminItems.addBinding().to(DataMigration.class);

        adminItems.addBinding().to(ShowStatistics.class);
        adminItems.addBinding().to(DeleteProcesses.class);

        //adminItems.addBinding().to(DNNTFlagSet.class);
        adminItems.addBinding().to(ParametrizedDNNTFlagSet.class);
        adminItems.addBinding().to(ParametrizedDNNTFlagUnset.class);

        //adminItems.addBinding().to(DNNTFlagUnset.class);
        adminItems.addBinding().to(DNNTCSVExport.class);

        bind(MainMenu.class).to(MainMenuImpl.class);
    }
}
