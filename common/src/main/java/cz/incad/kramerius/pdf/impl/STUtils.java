package cz.incad.kramerius.pdf.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;

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
		// http://localhost:8080/search/i18n
		//?action=bundle&amp;lang=cs&amp;country=CZ&amp;name=base
		String localeURl = i18nUrl+"?action=bundle&amp;lang="+locale.getLanguage()+"&amp;country="+locale.getCountry()+"&amp;name=base";
		LOGGER.info("i18n url = "+localeURl);
		return localeURl;
	}

	public static void main(String[] args) {
		System.out.println(createBundleURL(Locale.getDefault(), " http://localhost:8080/search/i18n"));
	}
	
	public static String metadata(FedoraAccess fedoraAccess, String parentUUID) throws IOException {
		org.w3c.dom.Document biblioMods = fedoraAccess.getDC(parentUUID);
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

	
//	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException {
//		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
//		fact.setNamespaceAware(true);
//		DocumentBuilder builder = fact.newDocumentBuilder();
//		//URL url = new URL("http://194.108.215.227:8080/fedora/get/uuid:046b1546-32f0-11de-992b-00145e5790ea/BIBLIO_MODS");
//		URL url = new URL("http://194.108.215.227:8080/fedora/get/uuid:0eaa6730-9068-11dd-97de-000d606f5dc6/BIBLIO_MODS");
//		
//		Document source = builder.parse(url.openStream());
//		
//		
//		Map map = prepareBiblioModsModel(source.getDocumentElement());
//		System.out.println(map);
//		StringTemplate template = getGroup().lookupTemplate("MONOGRAPH");
//		template.setAttribute("bibliomods",map);
//		System.out.println(template.toString());
		
		
//		URL xslUrl = TemplatesUtils.class.getResource("templates/biblio.xsl");
//		//Document xsl = builder.parse(xslUrl.openStream());
//		
//		
//		TransformerFactory transFact = TransformerFactory.newInstance();
//        Transformer trans = transFact.newTransformer(new StreamSource(xslUrl.openStream()));
//        
//        trans.transform(new StreamSource(url.openStream()), new StreamResult(System.out));
//	}
	
	
}
