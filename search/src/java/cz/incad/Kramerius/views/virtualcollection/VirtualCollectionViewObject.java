/*
 * Copyright (C) 2011 Alberto Hernandez
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
package cz.incad.Kramerius.views.virtualcollection;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import cz.incad.kramerius.virtualcollections.VirtualCollectionsManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VirtualCollectionViewObject {

    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    @Inject
    Provider<Locale> localeProvider;
    
    @Inject
    Provider<VirtualCollection> virtualCollectionProvider;

    @Inject
    KConfiguration kConfiguration;
    
    @Inject
	@Named("securedFedoraAccess")
	protected transient FedoraAccess fedoraAccess;
    
    public List<VirtualCollection> getVirtualCollections() {
        return VirtualCollectionsManager.getVirtualCollections(this.fedoraAccess, kConfiguration.getPropertyList("interface.languages"));
    }
    
    public List<VirtualCollection> getVirtualCollectionsLocale() {
        Locale locale = this.localeProvider.get();
        return VirtualCollectionsManager.getVirtualCollections(this.fedoraAccess, new String[]{"lang", locale.getLanguage()});
    }
    
    public VirtualCollection getCurrent(){
         return this.virtualCollectionProvider.get();
        
    } 
    
    public List<String> getHomeTabs(){
         String[] tabs = kConfiguration.getPropertyList("search.home.tabs");
         // Mozne hodnoty custom,mostDesirables,newest,facets,browseAuthor,browseTitle,info
         // Pokud mame nastavenou sbirku NEzobrazime mostDesirables, custom, browseAuthor,browseTitle
         
         ArrayList<String> validTabs = new ArrayList<String>();
         VirtualCollection vc = getCurrent();
         for(String tab:tabs){
             if(vc == null ||
                 tab.equals("info") ||
                 tab.equals("facets") ||
                 tab.equals("newest") ||
                 (tab.equals("collections") && getVirtualCollections().size()>0)){
                 validTabs.add(tab);
             }
         }
         
         return validTabs;
    }
    
    
    
}
