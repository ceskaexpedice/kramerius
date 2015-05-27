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
package cz.incad.Kramerius.exts.menu.context.guice;

import javax.print.attribute.standard.PrinterLocation;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import cz.incad.Kramerius.exts.menu.context.ContextMenu;
import cz.incad.Kramerius.exts.menu.context.ContextMenuPart;
import cz.incad.Kramerius.exts.menu.context.impl.ContextMenuImpl;
import cz.incad.Kramerius.exts.menu.context.impl.adm.AdminContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.adm.AdminContextMenuPartImpl;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.ApplyMovingWallItem;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.DeleteFromIndex;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.DeleteGeneratedDeepZoom;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.DeletePid;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.Editor;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.ExportCd;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.ExportDvd;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.ExportFoxml;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.GenerateDeepZoom;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.ParametrizedPdfExport;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.Reindex;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.Rights;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.ServerSort;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.SetPolicyFlag;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.ShowStatistics;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.StreamRights;
import cz.incad.Kramerius.exts.menu.context.impl.adm.items.VirtualCollectionAdd;
import cz.incad.Kramerius.exts.menu.context.impl.pub.PublicContextMenuItem;
import cz.incad.Kramerius.exts.menu.context.impl.pub.PublicContextMenuPartImpl;
import cz.incad.Kramerius.exts.menu.context.impl.pub.items.ModifyFavorites;
import cz.incad.Kramerius.exts.menu.context.impl.pub.items.DownloadOriginals;
import cz.incad.Kramerius.exts.menu.context.impl.pub.items.FeedBack;
import cz.incad.Kramerius.exts.menu.context.impl.pub.items.GeneratePDF;
import cz.incad.Kramerius.exts.menu.context.impl.pub.items.PersistentURL;
import cz.incad.Kramerius.exts.menu.context.impl.pub.items.PrintLocal;
import cz.incad.Kramerius.exts.menu.context.impl.pub.items.SelectPartAndPrintLocal;
import cz.incad.Kramerius.exts.menu.context.impl.pub.items.ServerPrint;
import cz.incad.Kramerius.exts.menu.context.impl.pub.items.ShowMetadata;

public class ContextMenuConfiguration extends AbstractModule {

    @Override
    protected void configure() {
        
        // casti menu
        Multibinder<ContextMenuPart> parts
            = Multibinder.newSetBinder(binder(), ContextMenuPart.class);
        parts.addBinding().to(PublicContextMenuPartImpl.class);
        parts.addBinding().to(AdminContextMenuPartImpl.class);
        
        //  public menu items
        Multibinder<PublicContextMenuItem> publicItems
        = Multibinder.newSetBinder(binder(), PublicContextMenuItem.class);
        publicItems.addBinding().to(ServerPrint.class);
        publicItems.addBinding().to(ShowMetadata.class);
        publicItems.addBinding().to(PersistentURL.class);
        publicItems.addBinding().to(GeneratePDF.class);
        publicItems.addBinding().to(DownloadOriginals.class);
        publicItems.addBinding().to(FeedBack.class);
        publicItems.addBinding().to(ModifyFavorites.class);
        publicItems.addBinding().to(PrintLocal.class);
        publicItems.addBinding().to(SelectPartAndPrintLocal.class);
        
        
        
        // admin  menu items
        Multibinder<AdminContextMenuItem> adminMenuItems
        = Multibinder.newSetBinder(binder(), AdminContextMenuItem.class);

//        adminMenuItems.addBinding().to(ServerPrint.class);
        adminMenuItems.addBinding().to(Reindex.class);
        adminMenuItems.addBinding().to(ServerSort.class);
        adminMenuItems.addBinding().to(DeleteFromIndex.class);
        adminMenuItems.addBinding().to(DeletePid.class);
        adminMenuItems.addBinding().to(SetPolicyFlag.class);
//        adminMenuItems.addBinding().to(ExportCD.class);
//        adminMenuItems.addBinding().to(ExportDVD.class);
        adminMenuItems.addBinding().to(ExportFoxml.class);
        adminMenuItems.addBinding().to(ParametrizedPdfExport.class);
        adminMenuItems.addBinding().to(GenerateDeepZoom.class);
        adminMenuItems.addBinding().to(DeleteGeneratedDeepZoom.class);
        adminMenuItems.addBinding().to(ShowStatistics.class);

        adminMenuItems.addBinding().to(Rights.class);
        adminMenuItems.addBinding().to(StreamRights.class);


        adminMenuItems.addBinding().to(ApplyMovingWallItem.class);
        adminMenuItems.addBinding().to(Editor.class);
        adminMenuItems.addBinding().to(VirtualCollectionAdd.class);
        
        

        
        // menu
        bind(ContextMenu.class).to(ContextMenuImpl.class);
    }

    
}
