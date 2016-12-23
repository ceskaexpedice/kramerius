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
package cz.incad.Kramerius.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import antlr.RecognitionException;
import antlr.TokenStreamException;


public class DownloadOriginalViewObject extends AbstractViewObject {

    @Inject
    IsActionAllowed isActionAllowed;
    
    @Inject
    SolrAccess solrAccess;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    @Named("solr")
    CollectionsManager collectionGet;
    
    public List<DownloadItem> getDownloadItems() throws RecognitionException, TokenStreamException, IOException, CollectionException {
        List<DownloadItem> items = new ArrayList<DownloadOriginalViewObject.DownloadItem>();
        List params = getPidsParams();
        for (Object param : params) {
            boolean accessed = false;
            ObjectPidsPath[] path = solrAccess.getPath(param.toString());
            for (ObjectPidsPath objectPidsPath : path) {
                objectPidsPath = objectPidsPath.injectRepository().injectCollections(this.collectionGet);
                if (isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), param.toString(), FedoraUtils.IMG_FULL_STREAM, objectPidsPath)) {
                    accessed = true;
                    break;
                }
            }
            String dctitle = DCUtils.titleFromDC(fedoraAccess.getDC(param.toString()));
            
            items.add(new DownloadItem(dctitle,"img?uuid=" + param.toString() + "&stream=IMG_FULL&action=GETRAW&asFile=true",fedoraAccess.getKrameriusModelName(param.toString()) , accessed, fedoraAccess.isImageFULLAvailable(param.toString())));
        }
        return items;
    }
    
    public static class DownloadItem {
        
        private String label;
        private String href;
        private String type;
        private boolean right = false;
        private boolean scanPresent = false;
        
        public DownloadItem(String label, String href, String type, boolean right, boolean scanPresent) {
            super();
            this.label = label;
            this.href = href;
            this.right = right;
            this.type = type;
            this.scanPresent = scanPresent;
        }
        
        public String getType() {
            return type;
        }
 
        public String getHref() {
            return href;
        }
        
        public boolean isRight() {
            return right;
        }
        
        public String getLabel() {
            return label;
        }
        
        public boolean isScanPresent() {
            return scanPresent;
        }
    }
}
