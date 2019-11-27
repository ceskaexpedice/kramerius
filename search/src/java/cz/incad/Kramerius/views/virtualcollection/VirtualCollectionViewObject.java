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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.security.strenderers.CollectionsWrapper;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.Collection.Description;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import cz.incad.kramerius.virtualcollections.CollectionsManager.SortOrder;
import cz.incad.kramerius.virtualcollections.CollectionsManager.SortType;

public class VirtualCollectionViewObject {

    public static Logger LOGGER = Logger.getLogger(VirtualCollectionViewObject.class.getName());
    
    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    @Inject
    Provider<Locale> localeProvider;


    @Inject
    Provider<Collection> virtualCollectionProvider;

    @Inject
    KConfiguration kConfiguration;
    
    @Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;

    
    @Inject
    LoggedUsersSingleton loggedUsersSingleton;
    
    @Inject
    Provider<User> userProvider;

    @Inject
    @Named("fedora")
    CollectionsManager fedoraManager;

    @Inject
    @Named("solr")
    CollectionsManager solrManager;

    public List<CollectionItemViewObject> getVirtualCollections() throws Exception {
        return wrap(this.solrManager.getCollections());
    }
    
    
    private List<CollectionItemViewObject> wrap(List<Collection> collections) throws IOException {
        List<CollectionItemViewObject> retvals = new ArrayList<CollectionItemViewObject>();
        for (Collection collection : collections) {
            retvals.add(new CollectionItemViewObject(collection, this.fedoraAccess));
        }
        return retvals;
    }

    public List<CollectionItemViewObject> getVirtualCollectionsFromFedora() throws Exception {
        List<Collection> collections = this.fedoraManager.getCollections();
        return wrap(collections);
    }
    
    public List<CollectionItemViewObject> getVirtualCollectionsLocale() throws Exception {
        SortOrder selectedVal = sortOrder();
        SortType sortType = sortType();
        if (selectedVal != null) {
        	if (sortType == null) {
        		sortType = SortType.ALPHABET;
        	}
        	return onlyLocalizedDescriptions(this.solrManager.getSortedCollections(this.localeProvider.get(), selectedVal, sortType));
        } else {
            return onlyLocalizedDescriptions(this.solrManager.getCollections());
        }
    }

    private SortOrder sortOrder() {
        String confString = KConfiguration.getInstance().getConfiguration().getString("search.collection.sort");
        if (confString == null) return null;
        SortOrder selectedVal = null;
        for (SortOrder v : CollectionsManager.SortOrder.values()) {
            if (confString.equals(v.name())) {
                selectedVal = v;
                break;
            }
        }
        return selectedVal;
    }

    private SortType sortType() {
        String confString = KConfiguration.getInstance().getConfiguration().getString("search.collection.sortType");
        if (confString == null) return null;
        SortType selectedVal = null;
        for (SortType v : CollectionsManager.SortType.values()) {
            if (confString.equals(v.name())) {
                selectedVal = v;
                break;
            }
        }
        return selectedVal;
    }
    
    public boolean isThumbnailsVisible() {
        boolean thumbs = KConfiguration.getInstance().getConfiguration().getBoolean("search.collection.thumbs",false);
        return thumbs;
    }
    
    
    private List<CollectionItemViewObject> onlyLocalizedDescriptions(List<Collection> rawCollection) throws IOException {
        Locale locale = this.localeProvider.get();
        List<Collection> ncols = new ArrayList<Collection>();
        for (Collection rCol : rawCollection) {
            Collection col = new Collection(rCol.getPid(),rCol.getLabel(),rCol.isCanLeaveFlag());
            col.setNumberOfDocs(rCol.getNumberOfDocs());
            Description l = rCol.lookup(locale.getLanguage());
            if (l != null) {
                col.addDescription(l);;
            }
            ncols.add(col);
        }
        return wrap(ncols);
    }
    
    public List<CollectionItemViewObject> getVirtualCollectionsFromFedoraLocale() throws Exception {
        SortOrder selectedVal = sortOrder();
        SortType sortType = sortType();
        if (selectedVal != null) {
        	if (sortType == null) {
        		sortType = SortType.ALPHABET;
        	}
        	return onlyLocalizedDescriptions(this.fedoraManager.getSortedCollections(this.localeProvider.get(), selectedVal, sortType));
        } else {
            return onlyLocalizedDescriptions(this.fedoraManager.getCollections());
        }
    }
    
    public Collection getCurrent(){
         return this.virtualCollectionProvider.get();
        
    } 
    
    public boolean getCanLeaveCurrent(){
        return this.getCurrent().isCanLeaveFlag();
    }
    
    public String getCurrentText(){
         Collection c = this.getCurrent();
         Description lookup = c.lookup(this.localeProvider.get().getLanguage());
         return lookup != null ? lookup.getText() : "";
    } 
    
    
    public List<String> getHomeTabs() throws Exception{
         String[] tabs = kConfiguration.getPropertyList("search.home.tabs");
         if (!this.loggedUsersSingleton.isLoggedUser(this.requestProvider)) {
             tabs= filterLogged(tabs);
         }
         // Mozne hodnoty custom,mostDesirables,newest,facets,browseAuthor,browseTitle,info
         // Pokud mame nastavenou sbirku NEzobrazime mostDesirables, custom, browseAuthor,browseTitle
         ArrayList<String> validTabs = new ArrayList<String>();
         Collection vc = getCurrent();
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
    
    
    
    public CollectionItemViewObject getParameterCollection()  {
        try {
            HttpServletRequest request = this.requestProvider.get();
            String parameter = request.getParameter("collection");
            if (parameter != null) {
                return new CollectionItemViewObject(this.fedoraManager.getCollection(parameter), this.fedoraAccess);
            } else return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    
    public String getLocaleLang() {
        return this.localeProvider.get().getLanguage();
    }

    
}
