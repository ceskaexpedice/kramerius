package cz.incad.kramerius.virtualcollections;

import java.util.List;

public interface CDKVirtualCollectionsGet {

    public List<Collection> virtualCollectionsFromResource(String res);

    public Collection virtualCollectionsFromResource(String vc, String res);

    public List<Collection> virtualCollections();

    public String getResource(String vcId);
}
