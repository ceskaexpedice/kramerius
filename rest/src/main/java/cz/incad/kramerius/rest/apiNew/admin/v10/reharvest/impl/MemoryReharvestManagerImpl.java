package cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.AlreadyRegistedPidsException;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestManager;


public class MemoryReharvestManagerImpl implements ReharvestManager {

    private List<ReharvestItem> items = new ArrayList<>();
    private Map<String, ReharvestItem> mapper = new HashMap<>();
    
    public MemoryReharvestManagerImpl() {
        super();
    }

    public void register(ReharvestItem item) throws AlreadyRegistedPidsException {
        List<String> alreadyRegistredPids = this.items.stream().filter(x -> x.getState() != null && (x.getState().equals("open") || x.getState().startsWith("waiting_for"))).map(ReharvestItem::getPids).flatMap(List::stream).collect(Collectors.toList());
        List<String> intersection = new ArrayList<>(item.getPids());
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
    
    public void deregister(String id) {
        ReharvestItem ritem = this.mapper.get(id);
        if (ritem != null) {
            this.mapper.remove(id);
            this.items.remove(ritem);
        }
    }

}
