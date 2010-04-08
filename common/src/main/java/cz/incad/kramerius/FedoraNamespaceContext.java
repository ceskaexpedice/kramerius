package cz.incad.kramerius;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static cz.incad.kramerius.FedoraNamespaces.*;

import javax.xml.namespace.NamespaceContext;

public class FedoraNamespaceContext implements NamespaceContext {

	static Map<String, String> NAMESPACES_DEFAULT_MAPPING = new HashMap<String, String>();
	static {
		NAMESPACES_DEFAULT_MAPPING.put("mods", BIBILO_MODS_URI);
		NAMESPACES_DEFAULT_MAPPING.put("dc", DC_NAMESPACE_URI);
		NAMESPACES_DEFAULT_MAPPING.put("fedora-models", FEDORA_MODELS_URI);
		NAMESPACES_DEFAULT_MAPPING.put("kramerius", KRAMERIUS_URI);
	}
	
	@Override
	public String getNamespaceURI(String arg0) {
		return NAMESPACES_DEFAULT_MAPPING.get(arg0);
	}

	@Override
	public String getPrefix(String arg0) {
		return getPrefixInternal(arg0);
	}
	
	private String getPrefixInternal(String uri) {
		Collection<String> keys = NAMESPACES_DEFAULT_MAPPING.keySet();
		for (String key : keys) {
			String val = NAMESPACES_DEFAULT_MAPPING.get(key);
			if (val.equals(uri)) {
				return key;
			}
		}
		return null;
	}
	
	@Override
	public Iterator getPrefixes(String arg0) {
		String prefixInternal = getPrefixInternal(arg0);
		if (prefixInternal != null) {
			return Arrays.asList(prefixInternal).iterator();
		} else return new ArrayList().iterator();
	}
	
}
