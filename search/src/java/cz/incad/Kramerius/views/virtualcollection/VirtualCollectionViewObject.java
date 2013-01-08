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
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import cz.incad.kramerius.virtualcollections.VirtualCollectionsManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    
    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Inject
    Provider<User> userProvider;
    
    private ArrayList languageCodes(){
        ArrayList l = new ArrayList<String>();
        String[] langs = kConfiguration.getPropertyList("interface.languages");
        for (int i = 0; i < langs.length; i++) {
                    String lang = langs[++i];
            l.add(lang);
        }
        return l;
    }
    public List<VirtualCollection> getVirtualCollections() throws Exception {
        //ArrayList l = new ArrayList<String>(Arrays.asList(kConfiguration.getPropertyList("interface.languages")));
        
        
        return VirtualCollectionsManager.getVirtualCollections(this.fedoraAccess, languageCodes());
    }
    
    public List<VirtualCollection> getVirtualCollectionsLocale() throws Exception {
        Locale locale = this.localeProvider.get();
        ArrayList<String> l = new ArrayList<String>();
        l.add(locale.getLanguage());
        return VirtualCollectionsManager.getVirtualCollections(this.fedoraAccess, l);
    }
    
    public VirtualCollection getCurrent(){
         return this.virtualCollectionProvider.get();
        
    } 
    
    public boolean getCanLeaveCurrent(){
        return this.getCurrent().isCanLeave();
    }
    
    public String getCurrentText(){
         VirtualCollection c = this.getCurrent();
         return c.getDescriptionLocale(this.localeProvider.get().getLanguage());
    } 
    
    
    public List<String> getHomeTabs() throws Exception{
         String[] tabs = kConfiguration.getPropertyList("search.home.tabs");
         if (!this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
             tabs= filterLogged(tabs);
         }
         
         // Mozne hodnoty custom,mostDesirables,newest,facets,browseAuthor,browseTitle,info
         // Pokud mame nastavenou sbirku NEzobrazime mostDesirables, custom, browseAuthor,browseTitle
         
         ArrayList<String> validTabs = new ArrayList<String>();
         VirtualCollection vc = getCurrent();
         for(String tab:tabs){
             if(vc == null ||
                 tab.equals("info") ||
                 tab.equals("facets") ||
                 tab.equals("newest") ||
                 (tab.equals("collections") && vc==null && getVirtualCollections().size()>0 ) ){
                 validTabs.add(tab);
             }
         }
         
         return validTabs;
    }

    private String[] filterLogged(String[] tabs) {
        List<String> mustBeLoggedList = new ArrayList<String>(Arrays.asList(kConfiguration.getPropertyList("search.home.tabs.onlylogged")));
        List<String> alist = new ArrayList<String>();
        for (int i = 0; i < tabs.length; i++) {
            if (!mustBeLoggedList.contains(tabs[i])) {
                alist.add(tabs[i]);
            }
        }
        return (String[]) alist.toArray(new String[alist.size()]);
    }
    
    
    
}
