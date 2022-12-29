package cz.incad.kramerius.rest.apiNew.client.v60.filter;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.properties.DefaultPropertiesInstances;
import cz.incad.kramerius.utils.XMLUtils;

public class DefaultFilter implements ProxyFilter{
	
	private Instances libraries;
	
	@Inject
	public DefaultFilter(Instances libraries) {
		super();
		this.libraries = libraries;
	}

	@Override
	public String newFilter() {
		if (this.libraries.isAnyDisabled()) {
			return filter();
		} else return null;
	}

	
	private String filter() {
		if (this.libraries.isAnyDisabled()) {
			List<String> eInsts = libraries.enabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
			//List<String> eInsts = libraries.enabledInstances().stream().map(OneInstance::getName)::getName).;
			String enabled = eInsts.stream().collect(Collectors.joining(" OR "));
			return "cdk.collection:("+enabled+")";
		} else {
			return null;
		}
	}

	@Override
	public String enhancedFilter(String f) {
		if (this.libraries.isAnyDisabled()) {
			return f+" AND "+filter();
		} else return f;
	}
	
	
	
	@Override
	public void filterValue(Element rawDoc) {
	
		List<String> dInsts = libraries.disabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
		Element cdkElement = XMLUtils.findElement(rawDoc, new XMLUtils.ElementsFilter() {
			@Override
			public boolean acceptElement(Element element) {
				String name = element.getAttribute("name");
				if (name != null && name.equals("cdk.collection")) {
					return true;
				}
				return false;
			}
		});

		if (cdkElement != null) {
			List<Element> elements = XMLUtils.getElements(cdkElement);
				elements.forEach(e-> {
				String content = e.getTextContent().trim();
				synchronized(rawDoc.getOwnerDocument()) {
					if (dInsts.contains(content)) {
						cdkElement.removeChild(e);
					}
				}					
			});
		}

	}

	@Override
	public void filterValue(JSONObject rawDoc) {
		List<String> dInsts = libraries.disabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
		if (rawDoc.has("cdk.collection")) {
			JSONArray col = rawDoc.getJSONArray("cdk.collection");
			JSONArray nCol = new JSONArray();
			for (int i = 0; i < col.length(); i++) {
				String lib = col.getString(i);
				if (!dInsts.contains(lib)) { nCol.put(lib); }
			}
			rawDoc.put("cdk.collection", nCol);
		}
	}

	@Override
	public String enhanceFacetsTerms() {
		List<String> dInsts = libraries.disabledInstances().stream().map(OneInstance::getName).collect(Collectors.toList());
		if (!dInsts.isEmpty()) {
			String excludedTerms = dInsts.stream().collect(Collectors.joining(","));
			return excludedTerms;
		} else return null;
	}

	
	
}
