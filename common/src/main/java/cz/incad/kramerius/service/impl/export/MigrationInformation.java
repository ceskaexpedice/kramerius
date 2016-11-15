package cz.incad.kramerius.service.impl.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.XMLUtils;

public class MigrationInformation implements EnahanceInformation {
    
    
    
    public byte[] enhance(byte[] foxml, byte[] itemjson) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        URL resource = MigrationInformation.class.getResource("res/migrationtemplates.stg");
        StringTemplateGroup group = new StringTemplateGroup(new InputStreamReader(resource.openStream()), DefaultTemplateLexer.class);
        StringTemplate template = group.getInstanceOf("datastream");
        template.setAttribute("pths", createModel(itemjson));
        
        Document streamDocument = XMLUtils.parseDocument(new StringReader(template.toString()), true);
        Document foxmlDocument = XMLUtils.parseDocument(new ByteArrayInputStream(foxml), true);
        Node rootNode = streamDocument.getDocumentElement();
        foxmlDocument.adoptNode(rootNode);
        foxmlDocument.getDocumentElement().appendChild(rootNode);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLUtils.print(foxmlDocument, bos);
        return bos.toByteArray();
    }

    private List<List<Map<String,String>>> createModel(byte[] itemjson) {
        JSONObject jsonObj = new JSONObject(new String(itemjson, Charset.forName("UTF-8")));
        JSONArray jArr = jsonObj.getJSONArray("context");
        List<List<Map<String,String>>> paths = new ArrayList<List<Map<String,String>>>();
        for (int i = 0,ll=jArr.length(); i < ll; i++) {
            JSONArray contextPath = jArr.getJSONArray(i);
            // one context path
            List<Map<String, String>> list = new ArrayList<Map<String,String>>();
            for (int j = 0,lz=contextPath.length(); j < lz;  j++) {
                JSONObject oneItem = contextPath.getJSONObject(j);
                Map<String, String> m = new HashMap<String, String>();
                m.put("pid",oneItem.getString("pid"));
                m.put("model",oneItem.getString("model"));
                list.add(m);
            }
            paths.add(list);
        }
        return paths;
    }


}
