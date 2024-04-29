package cz.incad.kramerius.rest.apiNew.admin.v10.reharvest;

import java.util.List;

public interface ReharvestManager {

    public void register(ReharvestItem item);
    
    public List<ReharvestItem> getItems();
    
    public ReharvestItem getTopItem();
    

    public ReharvestItem getItemById(String id);
    
    
    public void deregister(String id);
}
