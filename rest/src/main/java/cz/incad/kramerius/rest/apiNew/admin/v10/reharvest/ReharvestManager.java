package cz.incad.kramerius.rest.apiNew.admin.v10.reharvest;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;

import org.json.JSONException;

public interface ReharvestManager {

    public void register(ReharvestItem item) throws AlreadyRegistedPidsException;
 
    public ReharvestItem update(ReharvestItem item) throws UnsupportedEncodingException, JSONException, ParseException;
    
    public List<ReharvestItem> getItems();
    
    public ReharvestItem getTopItem(String status);

    public ReharvestItem getItemById(String id);

    public ReharvestItem getOpenItemByPid(String pid);
    
    public void deregister(String id);
}
