/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.Kramerius;

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RDFModels;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.Formating;
import cz.incad.utils.IKeys;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Administrator
 */
public class IndexModel extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    String fedoraUrl = "http://194.108.215.227:8080/fedora";
    String fedoraGSearch = "http://194.108.215.227:8080/fedoragsearch";
    java.net.URL url;
    String base;
    boolean full;
    Document contentDom;
    String command;
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;
    long startTime;
    int totalIndexed;
    int startFrom = -1;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        KConfiguration kconfig = (KConfiguration) this.getServletContext().getAttribute(IKeys.CONFIGURATION);
        fedoraUrl = kconfig.getFedoraHost();
        fedoraGSearch = kconfig.getIndexerHost();
        totalIndexed = 0;
        startTime = (new Date()).getTime();
        base = request.getRequestURL().substring(0, request.getRequestURL().indexOf("IndexModel"));
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            full = Boolean.parseBoolean(request.getParameter("full"));
            factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            String pid = request.getParameter("pid");
            String model = request.getParameter("model");
            try {
                startFrom = Integer.parseInt(request.getParameter("startFrom"));
            } catch (NumberFormatException nex) {
            }
            if (pid != null) {
                boolean withParams = Boolean.parseBoolean(request.getParameter("params"));
                if (withParams) {
                    IndexParams params = new IndexParams(pid, model,
                            request.getParameter("path"),
                            request.getParameter("parent_model"),
                            request.getParameter("parent_pid"),
                            request.getParameter("datum"),
                            request.getParameter("root_pid"),
                            request.getParameter("root_model"),
                            request.getParameter("root_title"),
                            request.getParameter("level"),
                            request.getParameter("language"));
                    indexByPid(pid, out, params);
                } else {
                    IndexParams params = new IndexParams(pid, model);
                    indexByPid(pid, out, params);
                }
            } else {
                doIndexModel(model, out);
            }
            out.println("Total indexed: " + totalIndexed);
            long timeInMiliseconds = (new Date()).getTime() - startTime;
            out.println(Formating.formatElapsedTime(timeInMiliseconds));
        } finally {
            out.close();
        }
    }

    private void doIndexModel(String model, PrintWriter out) {
        try {
            /*
            select $object from <#ri> 
            where  $object <fedora-model:hasModel> <info:fedora/model:monograph> 
            order by $object
            limit 100 
            offset 0
             */
            String query = "select $object from <#ri> " +
                    "where $object <fedora-model:hasModel> <info:fedora/model:" + model + ">  " +
                    "order by $object  " +
                    "limit 100  " +
                    "offset 0 ";
            urlStr = fedoraUrl + "/risearch?type=tuples&flush=true&lang=itql&format=TSV&distinct=off&stream=off" +
                    "&query=" + java.net.URLEncoder.encode(query, "UTF-8");
            //int lines = 0;
            url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine = in.readLine();
            while ((inputLine = in.readLine()) != null) {
                out.println(inputLine);
                sendToIndex(inputLine.split("/")[1]);
            //String pid = inputLine.split("/")[1];
            //IndexParams params = new IndexParams(pid, model);
            //indexByPid(pid, out, params);

            }
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToIndex(String pid) throws Exception {
        //http://194.108.215.227:8080/fedoragsearch/rest?operation=updateIndex&action=fromKrameriusModel&value=uuid:46b60b20-28da-11dd-9fbf-000d606f5dc6
        try {
            urlStr = fedoraGSearch + "/rest?operation=updateIndex&action=fromKrameriusModel&value=" + pid;

            //out.println(urlStr);
            url = new java.net.URL(urlStr);
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
            String inputLine = in.readLine();
            while ((inputLine = in.readLine()) != null) {
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
    String urlStr;

    private int indexByPid(String pid, PrintWriter out, IndexParams params) {

        int num = 0;
        ArrayList<String> pids = new ArrayList<String>();
        ArrayList<String> models = new ArrayList<String>();
        try {
            if (full) {
                urlStr = fedoraUrl + "/get/" + pid + "/RELS-EXT";
                contentDom = UrlReader.getDocument(urlStr);
                expr = xpath.compile("/RDF/Description/*");
                NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node childnode = nodes.item(i);
                    String nodeName = childnode.getNodeName();
                    if (nodeName.contains("hasPage")) {
                        num++;
                    }
                    if (!nodeName.contains("hasModel")) {
                        pids.add(childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                        models.add(KrameriusModels.toString(RDFModels.convertRDFToModel(nodeName)));
                    } else {
                    }
                }

                for (int i = 0; i < pids.size(); i++) {
                    String relpid = pids.get(i);
                    String model = models.get(i);
                    //if(model.equals("page")){
                    //    num += indexByPid(relpid, out, params);
                    //}else{
                    IndexParams childParams = new IndexParams(relpid, model);
                    childParams.merge(params);
                    num += indexByPid(relpid, out, childParams);
                //}
                //    break;
                }
            }
            //out.println(params.toUrlString());
            if (startFrom < totalIndexed) {
                urlStr = fedoraGSearch + "/rest?operation=updateIndex&action=fromPid&value=" + pid +
                        "&restXslt=updateOnlyResult&PAGESCOUNT=" + num + params.toUrlString();
                //out.println(urlStr);
                url = new java.net.URL(urlStr);
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
                String inputLine = in.readLine();
                while ((inputLine = in.readLine()) != null) {
                }
                in.close();
            }
            out.println(totalIndexed);
            totalIndexed++;

        } catch (Exception e) {
            e.printStackTrace();
            out.println("error");
        }
        out.flush();
        return num;
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
