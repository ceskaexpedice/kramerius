package cz.incad.Kramerius.backend.pdf.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;

public class TemplatesUtils {

	
	public static String metadata(FedoraAccess fedoraAccess, String parentUUID) throws IOException {
		org.w3c.dom.Document biblioMods = fedoraAccess.getDC(parentUUID);
		Element root = biblioMods.getDocumentElement();
		Map stModel = prepareDCModel(root);
		StringTemplateGroup group = getGroup("firstpage");
		StringTemplate stMetadata = group.getInstanceOf("metadata");
		stMetadata.setAttribute("dc", stModel);
		String metadata = stMetadata.toString();
		return metadata;
	}
	

	public static String description() {
		StringTemplateGroup group = getGroup("firstpage");
		StringTemplate stDescription = group.getInstanceOf("description");
		String description = stDescription.toString();
		return description;
	}
	
	public static String internalPart(FedoraAccess fa, String uuid) throws IOException {
		org.w3c.dom.Document biblioMods = fa.getBiblioMods(uuid);
		Element root = biblioMods.getDocumentElement();
		Map stModel = prepareBiblioModsModel(root);
		StringTemplateGroup group = getGroup("firstpage");
		StringTemplate intpart = group.getInstanceOf("internalpart");
		intpart.setAttribute("bibiomods", stModel);
		return intpart.toString();
	}
	
	private static Map prepareBiblioModsModel(Element root) {
		Map stModel = new HashMap();
		NodeList nlist = root.getChildNodes();
		for (int i = 0,ll=nlist.getLength(); i < ll; i++) {
			Node item = nlist.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				
				if (item.getNamespaceURI().equals(FedoraNamespaces.BIBILO_MODS_URI)) {
					if (item.getLocalName().equals("titleInfo")) {
						bibloModsTitleInfo((Element) item, stModel);
					}
				}
				if (item.getNamespaceURI().equals(FedoraNamespaces.BIBILO_MODS_URI)) {
					String localName = item.getLocalName();
					if (localName.equals("part")) {
						String type = ((Element)item).getAttribute("type");
						biblioModsPart((Element)item, stModel);
					}
				}
			}
		}
		return stModel;
	}
	
	private static void biblioModsPart(Element elm, Map stModel) {
		String type = elm.getAttribute("type");
		Element extent = XMLUtils.findElement(elm, "extent", FedoraNamespaces.BIBILO_MODS_URI);
		String attribute = extent.getAttribute("pages");
		if (attribute != null)  {
			stModel.put("pages", attribute);
		}
	}


	private static void bibloModsTitleInfo(Element elm, Map stModel) {
		String type = elm.getAttribute("type");
		String prefix = "";
		if ((type != null) && "alternative".equals(type)) {
			prefix = "alternative_";
			
		}
		Element title = XMLUtils.findElement(elm, "title", FedoraNamespaces.BIBILO_MODS_URI);
		stModel.put(prefix+"title", title.getTextContent());
		Element subTitle = XMLUtils.findElement(elm, "subTitle", FedoraNamespaces.BIBILO_MODS_URI);
		stModel.put(prefix+"subtitle", subTitle.getTextContent());
	}


	private static Map prepareDCModel(Element root) {
		Map stModel = new HashMap();
		NodeList nlist = root.getChildNodes();
		for (int i = 0,ll=nlist.getLength(); i < ll; i++) {
			Node item = nlist.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (item.getNamespaceURI().equals(FedoraNamespaces.DC_NAMESPACE_URI)) {
					String name = item.getLocalName();
					String value = item.getTextContent();
					if (stModel.containsKey(name)) {
						Object obj = stModel.get(name);
						List<String> vals = null;
						if (obj instanceof String) {
							vals = new ArrayList<String>();
							vals.add(obj.toString());
							stModel.put(name, vals);
						} else {
							vals = (List<String>) obj;
						}
						vals.add(value);
					} else {
						stModel.put(name, value);
					}
				}
			}
		}
		return stModel;
	}

	protected static StringTemplateGroup getGroup(String mainTemplateName) {
		InputStreamReader reader = new InputStreamReader(TemplatesUtils.class.getResourceAsStream("/cz/incad/Kramerius/backend/pdf/impl/templates/"+mainTemplateName+".st"),Charset.forName("UTF-8"));
		StringTemplateGroup group = new StringTemplateGroup(reader,DefaultTemplateLexer.class);
		return group;
	}

}
