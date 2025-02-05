package cz.incad.kramerius.rest.apiNew.client.v70.filter;

import static org.easymock.EasyMock.createMockBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.impl.MemoryReharvestManagerImpl;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance.TypeOfChangedStatus;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.properties.DefaultOnePropertiesInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.properties.DefaultPropertiesInstances;
import cz.incad.kramerius.utils.XMLUtils;


public class DefaultFilterTest {



    private List<String> cdkCollectionFromDoc(List<Element> respXML) {
        Element cdkCollection = XMLUtils.findElement(respXML.get(0), new  XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                if (element.getNodeName().equals("arr") && element.getAttribute("name").equals("cdk.collection")) {
                    return true;
                } else return false;
            }
        });
        
        Assert.assertTrue(cdkCollection != null);
        List<String> list = XMLUtils.getElements(cdkCollection).stream().map(Element::getTextContent).collect(Collectors.toList());
        return list;
    }
    
    

    
//    @Test
//    public void testValueDocWithPhysicalLocationJSON() throws IOException {
//        ReharvestManager reharvest = new MemoryReharvestManagerImpl();
//        DefaultPropertiesInstances inst = createMockBuilder(DefaultPropertiesInstances.class)
//                .withConstructor(reharvest)
//                .addMockedMethod("allInstances")
//                .addMockedMethod("enabledInstances")
//                .addMockedMethod("disabledInstances")
//                .addMockedMethod("find")
//                .addMockedMethod("isAnyDisabled")
//                .createMock();
//
//
//        DefaultOnePropertiesInstance mzk = new DefaultOnePropertiesInstance(null,null, inst,"mzk");
//        DefaultOnePropertiesInstance nkp = new DefaultOnePropertiesInstance(null, inst,"nkp");
//        DefaultOnePropertiesInstance knav = new DefaultOnePropertiesInstance(null,inst,"knav");
//        knav.setConnected(false, TypeOfChangedStatus.user);
//
//
//        List<OneInstance> allInstances = new ArrayList<>();
//        allInstances.add(mzk);
//        allInstances.add(nkp);
//        allInstances.add(knav);
//
//        List<OneInstance> enabledInstances = new ArrayList<>();
//        enabledInstances.add(nkp);
//        enabledInstances.add(knav);
//
//
//        List<OneInstance> disabledInstances = new ArrayList<>();
//        disabledInstances.add(mzk);
//
//        EasyMock.expect(inst.allInstances()).andReturn(allInstances).anyTimes();
//        EasyMock.expect(inst.enabledInstances()).andReturn(enabledInstances).anyTimes();
//        EasyMock.expect(inst.disabledInstances()).andReturn(disabledInstances).anyTimes();
//
//        EasyMock.expect(inst.find("mzk")).andReturn(mzk).anyTimes();
//        EasyMock.expect(inst.find("nkp")).andReturn(nkp).anyTimes();
//        EasyMock.expect(inst.find("knav")).andReturn(knav).anyTimes();
//        EasyMock.expect(inst.isAnyDisabled()).andReturn(true).anyTimes();
//
//        EasyMock.replay(inst);
//
//        ProxyFilter pf = new DefaultFilter(inst,null);
//
//        InputStream stream = DefaultFilterTest.class.getResourceAsStream("filter_simple_physicalfacets.json");
//        String json = IOUtils.toString(stream,"UTF-8");
//        List<JSONArray> arrs = respJSON(json);
//        for (JSONArray oneArray : arrs) {
//            for (int i = 0; i < oneArray.length(); i++) {
//                JSONObject doc = oneArray.getJSONObject(i);
//                pf.filterValue(doc);
//                JSONArray cdkCol = doc.getJSONArray("cdk.collection");
//                //Assert.assertTrue(cdkCol);
//
//            }
//        }
//    }

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
