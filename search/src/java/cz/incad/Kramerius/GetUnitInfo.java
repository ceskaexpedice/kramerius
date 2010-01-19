/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.Kramerius;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Administrator
 */
public class GetUnitInfo extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    private static final String fedoraUrl = "http://194.108.215.227:8080/fedora";
    private static HashMap cache;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String pid = request.getParameter("pid");
            String xsl = (String) request.getParameter("xsl");
            String language = (String) request.getParameter("language");

            String path = getServletContext().getRealPath("results/xsl");
            
            
            String command = fedoraUrl + "/get/" + pid + "/BIBLIO_MODS";
            Document contentDom = UrlReader.getDocument(command);
            Document xslDom = UrlReader.getDocument("http://localhost:8080/Kramerius_Search/results/xsl/" + xsl +
                    "?language=" + language);
            //out.println(xslDom);
            if (cache == null) {
                cache = new HashMap();
            }
            Transformer t = null;

// Get the XML input document and the stylesheet.
            Source xmlSource = new DOMSource(contentDom);

// check if the XSL sheet was found in cache, and use that if available
            if (cache.containsKey(xsl)) {
                t = (Transformer) cache.get(xsl + "_" + language);
            } else {

// otherwise, load the XSL sheet from disk, compile it and store the compiled
// sheet in the cache
                TransformerFactory tFactory = TransformerFactory.newInstance();
                //Source xslSource = new StreamSource(new File(path, xsl));
                Source xslSource = new DOMSource(xslDom);
                t = tFactory.newTransformer(xslSource);
                cache.put(xsl + "_" + language, t);

            }
            t.transform(xmlSource, new StreamResult(out));

        /*
        MonographUniInfo info = new MonographUniInfo(unitPid);
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("/modsCollection/mods/titleInfo/title/text()");
        info.title = ((Node) expr.evaluate(contentDom, XPathConstants.NODE)).getNodeValue();
        
        expr = xpath.compile("/modsCollection/mods/part/detail/number/text()");
        info.mods_number = ((Node) expr.evaluate(contentDom, XPathConstants.NODE)).getNodeValue();
        
        expr = xpath.compile("/modsCollection/mods/originInfo/dateIssued/text()");
        info.dateIssued = ((Node) expr.evaluate(contentDom, XPathConstants.NODE)).getNodeValue();
        
        out.print(info.pid + "#");
        out.print(info.title + "#");
        out.print(info.mods_number + "#");
        out.print(info.dateIssued + "#");
        out.println("");
         */
        } catch (Exception e) {
            e.printStackTrace(out);
        }finally {
            out.close();
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
