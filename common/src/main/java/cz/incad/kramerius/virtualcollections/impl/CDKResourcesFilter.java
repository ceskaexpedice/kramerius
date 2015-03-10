package cz.incad.kramerius.virtualcollections.impl;

import java.util.List;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.VirtualCollection;

public class CDKResourcesFilter {

    public List<String> getResources() {
        List<String> al = KConfiguration.getInstance().getConfiguration().getList("cdk.collections.sources");
        return al;
    }
    
    public List<String> getHidden() {
        List<String> al = KConfiguration.getInstance().getConfiguration().getList("cdk.collections.hidden");
        return al;
    }
    
    public boolean isResource(VirtualCollection vc) {
        return isResource(vc.getPid());
    }
    
    public boolean isResource(String pid) {
        List<String> al = getResources();
        return al.contains(pid);
    }

    public boolean isHidden(VirtualCollection vc) {
        return isHidden(vc.getPid());
    }
    
    public boolean isHidden(String pid) {
        List<String> al = getHidden();
        return al.contains(pid);
    }
    
    public boolean isFiltered(VirtualCollection vc) {
        return isResource(vc.getPid()) || isHidden(vc.getPid());
    }
    
    public boolean isFiltered(String pid) {
        return isResource(pid) || isHidden(pid);
    }
    
}
