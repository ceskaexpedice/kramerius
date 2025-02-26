package cz.incad.kramerius.pdf.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.utils.IOUtils;

public class STUtils {

    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(STUtils.class.getName());
    
	
	public static String localizedXslt(Locale locale, String i18nUrl, File file, String title, String modelName, String pid) throws IOException {
		String read = IOUtils.readAsString(STUtils.class.getResourceAsStream("templates/localized_xslt_template.xslt"), Charset.forName("UTF-8"), true);
		StringTemplate st = new StringTemplate(read);
		st.setAttribute("bundle_url", createBundleURL(locale,i18nUrl));
		st.setAttribute("template_folder", file.getAbsoluteFile().toURI().toURL().toString());
		st.setAttribute("model", modelName);
        st.setAttribute("pid", pid);
		st.setAttribute("parent_title", title);
		String string = st.toString();
		return string;
	}
	
	private static String createBundleURL(Locale locale, String i18nUrl) {
		String localeURl = i18nUrl+"?action=bundle&amp;lang="+locale.getLanguage()+"&amp;country="+locale.getCountry()+"&amp;name=base";
		LOGGER.info("i18n url = "+localeURl);
		return localeURl;
	}

	public static void main(String[] args) {
		System.out.println(createBundleURL(Locale.getDefault(), " http://localhost:8080/search/i18n"));
	}
	
	public static String metadata(AkubraRepository akubraRepository, String parentUUID) throws IOException {
		Document biblioMods = akubraRepository.getDatastreamContent(parentUUID, KnownDatastreams.BIBLIO_MODS).asDom(false);
		Element root = biblioMods.getDocumentElement();
		Map stModel = prepareDCModel(root);
		StringTemplateGroup group = getGroup();
		StringTemplate stMetadata = group.getInstanceOf("metadata");
		stMetadata.setAttribute("dc", stModel);
		String metadata = stMetadata.toString();
		return metadata;
	}
	

	public static String description() {
		StringTemplateGroup group = getGroup();
		StringTemplate stDescription = group.getInstanceOf("description");
		String description = stDescription.toString();
		return description;
	}

	/* TODO AK_NEW
	public static String textPage(FedoraAccess fa, String uuid, String modelName, String title) throws IOException {
		org.w3c.dom.Document biblioMods = fa.getBiblioMods(uuid);
		Element root = biblioMods.getDocumentElement();
		Map stModel = prepareBiblioModsModel(root);
		StringTemplateGroup group = getGroup();
		StringTemplate intpart = group.getInstanceOf("render");
		intpart.setAttribute("bibliomods", stModel);
		intpart.setAttribute("model", modelName);
		intpart.setAttribute("title", title);
		return intpart.toString();
	}
	
	public static String internalPart(FedoraAccess fa, String uuid, String title) throws IOException {
		org.w3c.dom.Document biblioMods = fa.getBiblioMods(uuid);
		Element root = biblioMods.getDocumentElement();
		Map stModel = prepareBiblioModsModel(root);
		StringTemplateGroup group = getGroup();
		StringTemplate intpart = group.getInstanceOf("internalpart");
		intpart.setAttribute("bibliomods", stModel);
		intpart.setAttribute("title", title);
		return intpart.toString();
	}
	*/
	private static void elementMap(Element m,Map parentMap) {
		Map map = new HashMap();
		String mprefix = m.getLocalName();
		String type = m.getAttribute("type");
		if (type != null) mprefix+= type;

		String transliteration = m.getAttribute("transliteration");
		if (transliteration != null) mprefix+= transliteration;
		
		String content = m.getTextContent();
		if ((content != null)  &&  !content.trim().equals(""))  map.put("content", content.trim());
		
		if (parentMap.containsKey(mprefix)) {
			Object previous = parentMap.get(mprefix);
			if (previous instanceof Map) {
				Map previousMap = (Map) previous;
				parentMap.put(mprefix, new ArrayList(Arrays.asList(previousMap,map)));
			} else {
				List list = (List) previous;
				list.add(map);
			}
		} else {
			parentMap.put(mprefix, map);
		}
		
		NodeList nlist = m.getChildNodes();
		for (int i = 0,ll=nlist.getLength(); i < ll; i++) {
			Node item = nlist.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				elementMap((Element) item, map);
			}
		}
	}
	
	private static Map prepareBiblioModsModel(Element root) {
		Map stModel = new HashMap();
		elementMap(root, stModel);
		return stModel;
	}
	


	private static Map prepareDCModel(Element root) {
		Map stModel = new HashMap();
		NodeList nlist = root.getChildNodes();
		for (int i = 0,ll=nlist.getLength(); i < ll; i++) {
			Node item = nlist.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
				if (item.getNamespaceURI().equals(RepositoryNamespaces.DC_NAMESPACE_URI)) {
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

	protected static StringTemplateGroup getGroup() {
		InputStreamReader common = new InputStreamReader(STUtils.class.getResourceAsStream("templates/common.st"),Charset.forName("UTF-8"));
		InputStreamReader internalpart = new InputStreamReader(STUtils.class.getResourceAsStream("templates/models.st"),Charset.forName("UTF-8"));
		InputStreamReader firstpage = new InputStreamReader(STUtils.class.getResourceAsStream("templates/firstpage.st"),Charset.forName("UTF-8"));
		StringTemplateGroup groupCommon = new StringTemplateGroup(common,DefaultTemplateLexer.class);
		StringTemplateGroup groupFirstPage = new StringTemplateGroup(firstpage, DefaultTemplateLexer.class);
		groupFirstPage.setSuperGroup(groupCommon);

		StringTemplateGroup models = new StringTemplateGroup(internalpart, DefaultTemplateLexer.class);
		models.setSuperGroup(groupFirstPage);

		return models;
	}

	

	
}
