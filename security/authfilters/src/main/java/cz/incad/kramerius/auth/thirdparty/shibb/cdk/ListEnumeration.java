package cz.incad.kramerius.auth.thirdparty.shibb.cdk;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ListEnumeration implements Enumeration<String>{

	private List<String> list = new ArrayList<>();
	
	public ListEnumeration(List<String> l) {
		this.list = l;
	}
	
	@Override
	public boolean hasMoreElements() {
		return this.list.size() > 0;
	}

	@Override
	public String nextElement() {
		String remove = this.list.remove(0);
		return remove;
	}
}
