/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.defxws.fgssolr;

import cz.incad.kramerius.indexer.IndexParams;
import cz.incad.utils.Formating;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Administrator
 */
public class IndexModel {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    private static final Logger logger = Logger.getLogger(IndexModel.class);
    
    
    String fedoraUrl = "http://194.108.215.227:8080/fedora";
    String fedoraGSearch = "http://194.108.215.227:8080/fedoragsearch";
    java.net.URL url;
    //String base;
    boolean full;
    Document contentDom;
    String command;
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;
    long startTime;
    int totalIndexed;
    int startFrom = -1;

    protected void doIndex(boolean _full, String pid, String model, int _startFrom,
            HashMap<String, String> startParams,
            Document contentDom)
            throws Exception {
        totalIndexed = 0;
        startTime = (new Date()).getTime();
        //base = request.getRequestURL().substring(0, request.getRequestURL().indexOf("IndexModel"));
        
        try {
            full = _full;
            factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
                startFrom = _startFrom;
            if (pid != null) {
                if (startParams!=null) {
                    IndexParams params = new IndexParams(pid, model,
                            startParams.get("path"),
                            startParams.get("pid_path"),
                            startParams.get("parent_model"),
                            startParams.get("parent_pid"),
                            startParams.get("datum"),
                            startParams.get("root_pid"),
                            startParams.get("root_model"),
                            startParams.get("root_title"),
                            startParams.get("language"));
                    indexByPid(pid, params, contentDom);
                } else {
                    IndexParams params = new IndexParams(pid, model, contentDom);
                    indexByPid(pid, params, contentDom);
                }
            } else {
                doIndexModel(model);
            }
            logger.info("Total indexed: " + totalIndexed);
            long timeInMiliseconds = (new Date()).getTime() - startTime;
            logger.info(Formating.formatElapsedTime(timeInMiliseconds));
        } catch(Exception ex) {
            logger.error(ex);
            throw new Exception(ex);
        }
    }

    private void doIndexModel(String model) {
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
                logger.info(inputLine);
                String pid = inputLine.split("/")[1];
                IndexParams params = new IndexParams(pid, model, contentDom);
                indexByPid(pid, params, contentDom);

            }
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    String urlStr;

    private int indexByPid(String pid, IndexParams params, Document contentDom) {

        int num = 0;
        ArrayList<String> pids = new ArrayList<String>();
        ArrayList<String> models = new ArrayList<String>();
        try {
            if (full) {
                urlStr = fedoraUrl + "/get/" + pid + "/RELS-EXT";
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
                        //models.add(KrameriusModels.toString(RDFModels.convertRDFToModel(nodeName)));
                    } else {
                    }
                }

                for (int i = 0; i < pids.size(); i++) {
                    String relpid = pids.get(i);
                    String model = models.get(i);
                    //if(model.equals("page")){
                    //    num += indexByPid(relpid, out, params);
                    //}else{
                    IndexParams childParams = new IndexParams(relpid, model, contentDom);
                    childParams.merge(params);
                    num += indexByPid(relpid, childParams, contentDom);
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
            logger.info(totalIndexed);
            totalIndexed++;

        } catch (Exception e) {
            e.printStackTrace();
            logger.info("error");
        }
        return num;
    }
}
