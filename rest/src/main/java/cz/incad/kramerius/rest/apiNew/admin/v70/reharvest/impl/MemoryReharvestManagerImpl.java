package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.impl;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONException;

import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.AlreadyRegistedPidsException;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;


public class MemoryReharvestManagerImpl implements ReharvestManager {

    private List<ReharvestItem> items = new ArrayList<>();
    private Map<String, ReharvestItem> mapper = new HashMap<>();
    
    public MemoryReharvestManagerImpl() {
        super();
    }

    public void register(ReharvestItem item) throws AlreadyRegistedPidsException {
        List<String> alreadyRegistredPids = this.items.stream().filter(x -> x.getState() != null && (x.getState().equals("open") || x.getState().startsWith("waiting_for"))).map(ReharvestItem::getPid).collect(Collectors.toList());
        List<String> intersection = Arrays.asList(item.getPid());
        intersection.retainAll(alreadyRegistredPids);
        
        if (intersection.isEmpty()) {
            this.items.add(item);
            sortItems();
            this.mapper.put(item.getId(), item);
        } else {
            throw new AlreadyRegistedPidsException(intersection);
        }
        
    }
    
    public List<ReharvestItem> getItems() {
        sortItems();
        return this.items;
    }

    private void sortItems() {
        this.items.sort((item1, item2)-> {
            return item1.getTimestamp().compareTo(item2.getTimestamp());
        });
    }
    
    public ReharvestItem getTopItem(String status) {
        sortItems();
        List<ReharvestItem> listWithStatus = this.items.stream().filter(x -> x.getState() != null && x.getState().equals(status)).collect(Collectors.toList());
        return listWithStatus.size() > 0 ? listWithStatus.get(0) : null;
    }

    public ReharvestItem getItemById(String id) {
        return this.mapper.get(id);
    }
 
    @Override
    public ReharvestItem getOpenItemByPid(String pid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ReharvestItem update(ReharvestItem item) throws UnsupportedEncodingException, JSONException, ParseException {
        return item;
    }

    public void deregister(String id) {
        ReharvestItem ritem = this.mapper.get(id);
        if (ritem != null) {
            this.mapper.remove(id);
            this.items.remove(ritem);
        }
    }

}
