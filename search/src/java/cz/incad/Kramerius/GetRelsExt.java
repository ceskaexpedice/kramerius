/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.Kramerius;

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RDFModels;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author Administrator
 */
public class GetRelsExt extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    ResourceBundle res;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        try {
            KConfiguration configuration = KConfiguration.getKConfiguration();
            String language = request.getParameter("language");
            if(language==null || language.equals("")) language = "cs";
            res = ResourceBundle.getBundle("labels", new Locale(language));

            String pid = request.getParameter("pid");
            String relation = request.getParameter("relation");
            String format = request.getParameter("format");
            ArrayList<String> pids = getRdfPids(configuration, pid, relation);
            
            if (format == null) {
                response.setContentType("text/plain;charset=UTF-8");
                for (String relpid : pids) {
                    out.print(relpid + "#");
                }
            } else if (format.equals("json")) {
                //response.setContentType("application/x-javascript");
                response.setContentType("text/plain");
                outputAsJson(out, pids);
            } 
        //writeBiblioModsInfo(pids, out);
        } catch (Exception e) {
            out.println(e.toString());
        } finally {
            out.close();
        }
    }

    private void outputAsJson(PrintWriter out, ArrayList<String> pids) {
        out.println("({\"items\": [");
        HashMap<String, ArrayList<String>> models = new HashMap<String, ArrayList<String>>();
        String model;
        String rels;
        for (String relpid : pids) {
            model = relpid.split(" ")[0];
            rels = "\"" + relpid.split(" ")[1].split(":")[1] + "\"";
            if (models.containsKey(model)) {
                models.get(model).add(rels);
            } else {
                ArrayList<String> a = new ArrayList<String>();
                a.add(rels);
                models.put(model, a);
            }
        }
        Iterator iterator = models.keySet().iterator();

        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            out.print("{\"");
            
            out.print(KrameriusModels.toString(RDFModels.convertRDFToModel(key)));
            out.print("\":[\"");
            try{
                out.print(res.getString(KrameriusModels.toString(RDFModels.convertRDFToModel(key))));
            }catch(Exception ex){
                System.out.println(ex);
                out.print(key);
            }
            out.print("\",");
            for (int i = 0; i < models.get(key).size() - 1; i++) {
                out.println(models.get(key).get(i) + ",");
            }
            out.print(models.get(key).get(models.get(key).size() - 1));
            out.println("]}");
            if (iterator.hasNext()) {
                out.println(",");
            }
        }
        out.println("]})");
    }

    private ArrayList getRdfPids(KConfiguration configuration, String pid, String relation) {
        ArrayList<String> pids = new ArrayList<String>();
        try {
            String command = configuration.getFedoraHost() + "/get/" + pid + "/RELS-EXT";
            Document contentDom = getDocument(command);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String xPathStr = "/RDF/Description/";
            if (relation.endsWith("*")) {
                xPathStr += relation;
            } else {
                xPathStr += RDFModels.convertToRdf(KrameriusModels.parseString(relation));
            }
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                if (!childnode.getNodeName().contains("hasModel") &&
                    childnode.hasAttributes() &&
                    childnode.getAttributes().getNamedItem("rdf:resource")!=null  ) {
                    pids.add(childnode.getNodeName() + " " +
                            childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            pids.add(e.toString());
        }
        return pids;
    }

    private Document getDocument(String urlStr) {
        try {
            StringBuffer result = new StringBuffer();
            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream(),
                    java.nio.charset.Charset.forName("UTF-8")));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            InputSource source = new InputSource(new StringReader(result.toString()));
            return builder.parse(source);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
