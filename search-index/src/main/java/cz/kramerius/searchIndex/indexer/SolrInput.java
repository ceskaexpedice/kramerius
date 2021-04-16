package cz.kramerius.searchIndex.indexer;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SolrInput {

    private Document outDoc = null;
    private Map<String, List<String>> fields = new HashMap<>();

    public void addField(String name, String value) {
        if (value != null) {
            value = value.trim();
            if (value.isEmpty()) {
                value = null;
            }
        }
        if (value != null) {
            List<String> fieldValues = fields.get(name);
            if (fieldValues == null) {
                fieldValues = new ArrayList<>();
                fields.put(name, fieldValues);
            }
            if (!fieldValues.contains(value)) { //we don't want duplicate values
                fieldValues.add(value);
            }
            outDoc = null;//reset outDoc after every chanage
        }
    }

    public void printTo(File outFile, boolean prettyPrint) throws IOException {
        Document document = getDocument();
        //System.out.println(document.asXML().toString());
        FileWriter out = new FileWriter(outFile);
        OutputFormat format = prettyPrint ? OutputFormat.createPrettyPrint() : OutputFormat.createCompactFormat();
        XMLWriter writer = new XMLWriter(out, format);
        writer.write(document);
        writer.close();
        out.close();
    }

    public Document getDocument() {
        if (outDoc == null) {
            outDoc = buildDoc();
        }
        return outDoc;
    }

    private Document buildDoc() {
        Document doc = DocumentHelper.createDocument();
        Element docEl = doc.addElement("add").addElement("doc");
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        for (String fieldName : fieldNames) {
            List<String> values = fields.get(fieldName);
            //na pořadí opakovatelných polí záleží, proto se zde nebudou řadit
            //Collections.sort(values, new CzechAlphabetComparator());
            for (String value : values) {
                Element fieldEl = docEl.addElement("field");
                fieldEl.addAttribute("name", fieldName);
                fieldEl.addText(value);
            }
        }
        return doc;
    }

    public Map<String, List<String>> getFieldsCopy() {
        Map<String, List<String>> fieldsCopy = new HashMap<>();
        for (String key : fields.keySet()) {
            List<String> values = fields.get(key);
            fieldsCopy.put(key, new ArrayList<>(values));
        }
        return fieldsCopy;
    }


}
