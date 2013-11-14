package cz.incad.kramerius.rest.api.k5.client.item.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.api.k5.client.item.metadata.Metadata;

public class TreeAggregate {


	List<ItemTreeRender> trees = new ArrayList<ItemTreeRender>();
	
    @Inject
    public TreeAggregate(Set<ItemTreeRender> trees) {
        super();
        for (ItemTreeRender p : trees) {
            this.trees.add(p);
        }
        
        Collections.sort(this.trees,new Comparator<ItemTreeRender>() {

			@Override
			public int compare(ItemTreeRender o1, ItemTreeRender o2) {
				Integer first = o1.getSortingKey();
				Integer second = o2.getSortingKey();
				return first.compareTo(second);
			}
        	
        });
    }

    
    public ItemTreeRender getTreeRenderer(String pid) {
    	for (ItemTreeRender t : this.trees) {
			if (t.isApplicable(pid, null)) return t;
		}
    	return null;
    }
}
