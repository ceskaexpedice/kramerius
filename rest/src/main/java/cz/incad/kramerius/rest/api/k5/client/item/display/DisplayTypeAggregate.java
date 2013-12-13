package cz.incad.kramerius.rest.api.k5.client.item.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;


public class DisplayTypeAggregate {

	List<DisplayType> types = new ArrayList<DisplayType>();
	
    @Inject
    public DisplayTypeAggregate(Set<DisplayType> types) {
        super();
        for (DisplayType p : types) {
            this.types.add(p);
        }
        
        Collections.sort(this.types,new Comparator<DisplayType>() {

			@Override
			public int compare(DisplayType o1, DisplayType o2) {
				Integer first = o1.getSortingKey();
				Integer second = o2.getSortingKey();
				return first.compareTo(second);
			}
        	
        });
    }

	public DisplayType getDisplayType(String pid, HashMap<String, Object> options) {
		for (DisplayType d : this.types) {
			if (d.isApplicable(pid, options)) return d; 
		}
		return null;
	}

}
