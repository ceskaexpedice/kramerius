package cz.incad.kramerius.rest.apiNew.client.v60.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.rest.api.k5.client.JSONDecorator;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance.TypeOfChangedStatus;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.properties.DefaultPropertiesInstances;
import cz.incad.kramerius.utils.XMLUtils;

@Ignore
public class DefaultFilterTest {


	@Test
	public void testFilterNew() {
		Instances insts = new DefaultPropertiesInstances();
		insts.find("mzk").setConnected(false, TypeOfChangedStatus.user);
		//insts.setStatus("mzk", false);
		ProxyFilter pf = new DefaultFilter(insts);
		String newFilter = pf.newFilter();
		Assert.assertEquals("cdk.collection:(inovatika OR knav OR knav-test)",newFilter);
	}
	
	@Test
	public void testFilterApply() {
		Instances insts = new DefaultPropertiesInstances();
		insts.find("mzk").setConnected(false, TypeOfChangedStatus.user);
		ProxyFilter pf = new DefaultFilter(insts);
		String eFilter = pf.enhancedFilter("model:monograph AND titles.search:*");
		Assert.assertEquals("model:monograph AND titles.search:* AND cdk.collection:(inovatika OR knav)",eFilter);
	}

	@Test
	public void testValueDocXML() throws IOException, ParserConfigurationException, SAXException, TransformerException {
		Instances insts = new DefaultPropertiesInstances();
		insts.find("mzk").setConnected(false, TypeOfChangedStatus.user);
		ProxyFilter pf = new DefaultFilter(insts);

		InputStream stream = DefaultFilterTest.class.getResourceAsStream("filter_simple.xml");
		String xml = IOUtils.toString(stream,"UTF-8");
		List<Element> respXML = respXML(xml);
		for (Element doc : respXML) {
			pf.filterValue(doc);
		}
	}
	
	@Test
	public void testValueDocJSON() throws IOException {

		Instances insts = new DefaultPropertiesInstances();
		insts.find("mzk").setConnected(false, TypeOfChangedStatus.user);
		ProxyFilter pf = new DefaultFilter(insts);

		InputStream stream = DefaultFilterTest.class.getResourceAsStream("filter_simple.json");
		String json = IOUtils.toString(stream,"UTF-8");
		List<JSONArray> arrs = respJSON(json);
		for (JSONArray oneArray : arrs) {
			for (int i = 0; i < oneArray.length(); i++) {
				JSONObject doc = oneArray.getJSONObject(i);
				pf.filterValue(doc);
				JSONArray cdkCol = doc.getJSONArray("cdk.collection");
				System.out.println(cdkCol);
				Assert.assertTrue(cdkCol.length() == 1);
			}
		}

		stream = DefaultFilterTest.class.getResourceAsStream("filter_groups.json");
		json = IOUtils.toString(stream,"UTF-8");
		arrs = respJSON(json);
		for (JSONArray oneArray : arrs) {
			for (int i = 0; i < oneArray.length(); i++) {
				JSONObject doc = oneArray.getJSONObject(i);
				pf.filterValue(doc);
				JSONArray cdkCol = doc.getJSONArray("cdk.collection");
				Assert.assertTrue(cdkCol.length() == 1);
			}
		}

	}

    private List<Element>  respXML(String rawString) throws ParserConfigurationException, SAXException, IOException {
        Document doc = XMLUtils.parseDocument(new StringReader(rawString));
        List<Element> elms = XMLUtils.getElementsRecursive(doc.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return (element.getNodeName().equals("doc"));
            }
        });
        return elms;
    }

	
    private List<JSONArray>  respJSON(String rawString) throws UnsupportedEncodingException, JSONException {
        List<JSONArray> docsArrays = new ArrayList<JSONArray>();

        JSONObject resultJSONObject = new JSONObject(rawString);
        Stack<JSONObject> prcStack = new Stack<JSONObject>();
        prcStack.push(resultJSONObject);
        while (!prcStack.isEmpty()) {
            JSONObject popped = prcStack.pop();
            //Iterator keys = popped.keys();
            for (Iterator keys = popped.keys(); keys.hasNext(); ) {
                Object kobj = (Object) keys.next();
                String key = (String) kobj;
                Object obj = popped.get(key);
                boolean docsKey = key.equals("docs");
                if (docsKey && (obj instanceof JSONArray)) {
                    docsArrays.add((JSONArray) obj);
                }
                if (obj instanceof JSONObject) {
                    prcStack.push((JSONObject) obj);
                }
                if (obj instanceof JSONArray) {
                    JSONArray arr = (JSONArray) obj;
                    for (int i = 0, ll = arr.length(); i < ll; i++) {
                        Object arrObj = arr.get(i);
                        if (arrObj instanceof JSONObject) {
                            prcStack.push((JSONObject) arrObj);
                        }
                    }
                }
            }
        }
        return docsArrays;
    }

}
