package cz.incad.kramerius.rest.api.k5.client.item.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;


public class MetadataAggregate {
	
	List<Metadata> metadatas = new ArrayList<Metadata>();
	
    @Inject
    public MetadataAggregate(Set<Metadata> metadatas) {
        super();
        for (Metadata p : metadatas) {
            this.metadatas.add(p);
        }
        
        Collections.sort(this.metadatas,new Comparator<Metadata>() {

			@Override
			public int compare(Metadata o1, Metadata o2) {
				Integer first = o1.getSortingKey();
				Integer second = o2.getSortingKey();
				return first.compareTo(second);
			}
        	
        });
    }

	public Metadata getMetadataCollector(String pid) {
		for (Metadata m : this.metadatas) {
			if (m.isApplicable(pid, null)) return m; 
		}
		return null;
	}
}
