package cz.incad.kramerius.repository;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.RepositoryApi.Triplet;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.java.Pair;

// helper utility used for extracting structure information 
public class ExtractStructureHelper {
    
    public static final Logger LOGGER = Logger.getLogger(ExtractStructureHelper.class.getName());
    
    private ExtractStructureHelper() {}

    public static JSONObject pidAndRelationToJson(String pid, String relation) {
        JSONObject json = new JSONObject();
        json.put("pid", pid);
        json.put("relation", relation);
        return json;
    }

    public static JSONObject extractStructureInfo(KrameriusRepositoryApi krameriusRepositoryApi, String pid) throws RepositoryException, SolrServerException, IOException {
        JSONObject structure = new JSONObject();
        //parents
        JSONObject parents = new JSONObject();
        Pair<RepositoryApi.Triplet, List<RepositoryApi.Triplet>> parentsTpls = krameriusRepositoryApi.getParents(pid);
        if (parentsTpls.getFirst() != null) {
            //sortRelations(relsExt, ownRelations);
            parents.put("own", pidAndRelationToJson(parentsTpls.getFirst().source, parentsTpls.getFirst().relation));

        }
        JSONArray fosterParents = new JSONArray();
        for (RepositoryApi.Triplet fosterParentTpl : parentsTpls.getSecond()) {
            fosterParents.put(pidAndRelationToJson(fosterParentTpl.source, fosterParentTpl.relation));
        }
        parents.put("foster", fosterParents);
        structure.put("parents", parents);

        Document relsExt = krameriusRepositoryApi.getRelsExt(pid, true);
        
        JSONObject children = new JSONObject();
        Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> childrenTpls = krameriusRepositoryApi.getChildren(pid);
        JSONArray ownChildren = new JSONArray();
        Map<String, JSONObject> mapping = new HashMap<>();
        
        for (RepositoryApi.Triplet ownChildTpl : childrenTpls.getFirst()) {
            mapping.put(ownChildTpl.target, pidAndRelationToJson(ownChildTpl.target, ownChildTpl.relation));
        }
        
        
        exploreRelsExt(relsExt, (child)-> {
            Element ch = child;
            Namespace namespace = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            org.dom4j.QName qname = new org.dom4j.QName("resource", namespace);
            Attribute attribute = ch.attribute(qname);
            if (attribute != null) {
                String value = attribute.getValue();
                if (value.startsWith("info:fedora/uuid")) {
                    String extractedPid = value.substring("info:fedora/".length());
                    JSONObject jsonObject = mapping.get(extractedPid);
                    if (jsonObject != null) {
                        ownChildren.put(jsonObject);
                    }
                }
            }
        });

        List<String> devList = new ArrayList<>();
        for (int i = 0; i < ownChildren.length(); i++) { devList.add(ownChildren.get(i).toString()); }
        LOGGER.fine(String.format("Pids sorted by RELS-EXT %s %s", pid, devList));
        
        
        children.put("own", ownChildren);
        JSONArray fosterChildren = new JSONArray();
        for (RepositoryApi.Triplet fosterChildTpl : childrenTpls.getSecond()) {
            fosterChildren.put(pidAndRelationToJson(fosterChildTpl.target, fosterChildTpl.relation));
        }
        
        
        children.put("foster", fosterChildren);
        structure.put("children", children);

        
        //model
        String model = krameriusRepositoryApi.getModel(pid);
        structure.put("model", model);
    
        return structure;
    }

    private static void exploreRelsExt(Document relsExt, Consumer<Element> consumer) {
        Element rootElement = relsExt.getRootElement();
        Stack<Element> stack = new Stack<>();
        stack.push(rootElement);
        while(!stack.isEmpty()) {
            Element pop = stack.pop();
            List<Element> children = pop.elements();
            for (Element child : children) {
                consumer.accept(child);
                stack.push(child);
            }
        }
    }

    private static void sortRelations(Document relsExt, JSONObject ownRelations) {
        relsExt.selectNodes("");
    }
}

