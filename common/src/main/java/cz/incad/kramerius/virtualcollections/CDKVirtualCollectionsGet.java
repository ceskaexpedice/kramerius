package cz.incad.kramerius.virtualcollections;

import java.util.List;

public interface CDKVirtualCollectionsGet {

    public List<VirtualCollection> virtualCollectionsFromResource(String res);

    public VirtualCollection virtualCollectionsFromResource(String vc, String res);

    public List<VirtualCollection> virtualCollections();

    public String getResource(String vcId);
}
