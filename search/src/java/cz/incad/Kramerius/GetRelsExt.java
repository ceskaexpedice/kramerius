/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.Kramerius;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RDFModels;
import cz.incad.kramerius.lp.utils.DecriptionHTML;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * 
 * @author Administrator
 */
public class GetRelsExt extends GuiceServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GetRelsExt.class.getName());
    @Inject
    Provider<Locale> provider;
    @Inject
    ResourceBundleService bundleService;
    @Inject
    KConfiguration configuration;
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        try {
            ResourceBundle resourceBundle = this.bundleService.getResourceBundle("labels", this.provider.get());
            String pid = request.getParameter("pid");
            String relation = request.getParameter("relation");
            String format = request.getParameter("format");
            List<RelationNamePidValue> pids = getRdfPids(configuration, pid, relation);

            if (format == null) {
                response.setContentType("text/plain;charset=UTF-8");
                out.print(simpleList(pids));
            } else if (format.equals("json")) {
                response.setContentType("text/plain");
                out.print(json(pids, resourceBundle));
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            out.close();
        }
    }

    private String simpleList(List<RelationNamePidValue> pids) throws IOException {
        StringTemplateGroup group = stGroup();
        StringTemplate simple = group.getInstanceOf("simple");
        simple.setAttribute("list", pids);
        return simple.toString();
    }

    private String json(List<RelationNamePidValue> pids,
            ResourceBundle resourceBundle) throws IOException {
        HashMap<String, ArrayList<String>> models = prepareModel(pids);
        HashMap<String, String> res = new HashMap<String, String>();
        Iterator<String> keys = resourceBundle.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("fedora.model.")) {
                res.put(key.substring("fedora.model.".length()), resourceBundle.getString(key));
            }
        }

        StringTemplateGroup group = stGroup();
        StringTemplate json = group.getInstanceOf("json");
        json.setAttribute("model", models);
        json.setAttribute("res", res);
        return json.toString();

    }

    private StringTemplateGroup stGroup() throws IOException {
        InputStream stream = GetRelsExt.class.getResourceAsStream("GetRelsExt.stg");
        String string = IOUtils.readAsString(stream, Charset.forName("UTF-8"), true);
        StringTemplateGroup group = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
        return group;
    }

    private HashMap<String, ArrayList<String>> prepareModel(List<RelationNamePidValue> pids) {
        String model;
        String rels;
        HashMap<String, ArrayList<String>> models = new HashMap<String, ArrayList<String>>();
        for (RelationNamePidValue item : pids) {
            model = KrameriusModels.toString(RDFModels.convertRDFToModel(item.getRelationName()));
            rels = item.getPid();
            if (rels.contains(":")) {
                rels = rels.substring(rels.indexOf(':'));
            }
            if (models.containsKey(model)) {
                models.get(model).add(rels);
            } else {
                ArrayList<String> a = new ArrayList<String>();
                a.add(rels);
                models.put(model, a);
            }
        }
        return models;
    }

    private List<RelationNamePidValue> getRdfPids(KConfiguration configuration,
            String pid, String relation) throws IOException, XPathExpressionException, LexerException {

        ArrayList<RelationNamePidValue> pids = new ArrayList<RelationNamePidValue>();
        Document contentDom = this.fedoraAccess.getRelsExt(pid.substring("uuid:".length()));
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        String xPathStr = "/rdf:RDF/rdf:Description/";
        if (relation.endsWith("*")) {
            xPathStr += "kramerius:" + relation;
        } else {
            xPathStr += "kramerius:" + RDFModels.convertToRdf(KrameriusModels.parseString(relation));
        }
        XPathExpression expr = xpath.compile(xPathStr);
        NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node childnode = nodes.item(i);
            if (childnode.getNodeType() == Node.ELEMENT_NODE) {
                Element elm = (Element) childnode;
                if (elm.hasAttributes()) {
                    String attributeVal = elm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                    if ((attributeVal != null) && (!attributeVal.trim().equals(""))) {
                        PIDParser pidParser = new PIDParser(attributeVal);
                        pidParser.disseminationURI();
                        if (pidParser.getNamespaceId().equals("uuid")) {
                            String objectId = pidParser.getObjectId();
                            pids.add(new RelationNamePidValue(elm.getLocalName(), objectId));
                        }
                    } else {
                        LOGGER.fine("element '" + elm.getLocalName() + "' namespaceURI '" + elm.getNamespaceURI() + "'");
                    }
                }
            } else {
                continue;
            }
        }
        return pids;
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    class RelationNamePidValue {

        private String relationName;
        private String pid;

        public RelationNamePidValue(String relationName, String pid) {
            super();
            this.relationName = relationName;
            this.pid = pid;
        }

        public String getRelationName() {
            return relationName;
        }

        public String getPid() {
            return pid;
        }
    }
}
