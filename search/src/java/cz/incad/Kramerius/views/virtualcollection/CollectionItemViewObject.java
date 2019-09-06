package cz.incad.Kramerius.views.virtualcollection;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.impl.fedora.FedoraStreamUtils;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.Collection.Description;

public class CollectionItemViewObject {

    private Collection collection;
    private boolean thumbnailAvailable = false;
    private boolean fullAvailable = false;
    
    public CollectionItemViewObject(Collection collection, FedoraAccess fedoraAccess) throws IOException {
        super();
        this.collection = collection;
        this.thumbnailAvailable = fedoraAccess.isStreamAvailable(this.collection.getPid(), ImageStreams.IMG_THUMB.name());
        this.fullAvailable = fedoraAccess.isStreamAvailable(this.collection.getPid(), ImageStreams.IMG_FULL.name());
    }

    public String getPid() {
        return collection.getPid();
    }

    public String getLabel() {
        return collection.getLabel();
    }
    /**
     * @return the canLeave
     */
    public boolean isCanLeave() {
    	return collection.isCanLeaveFlag();
    }

    public int getNumberOfDocs() {
    	return this.collection.getNumberOfDocs();
    }
    
    public boolean isThumbnailAvailable() {
        return thumbnailAvailable;
    }

    public boolean isFullAvailable() {
        return fullAvailable;
    }

    public List<Description> getDescriptions() {
        return collection.getDescriptions();
    }
    
    
    public Map<String, String> getDescriptionsMap(){
        Map map = new HashMap<String, String>();
        for(Description d : this.collection.getDescriptions()){
            map.put(d.getLangCode(), d.getText());
        }
        return map;
    }

    public Map<String, String> getLongDescriptionsMap(){
        Map map = new HashMap<String, String>();
        for(Description d : this.collection.getDescriptions()){
            map.put(d.getLangCode(), d.getLongText());
        }
        return map;
    }
    
}
