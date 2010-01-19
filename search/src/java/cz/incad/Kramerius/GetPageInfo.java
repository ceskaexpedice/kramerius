/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.Kramerius;

import java.io.IOException;
import java.io.PrintWriter;
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

/**
 *
 * @author Administrator
 */
public class GetPageInfo extends HttpServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    
    private static final String fedoraUrl = "http://194.108.215.227:8080/fedora";
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here
            out.println("<h1>Servlet MonographUnits at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
             */
            String pid = request.getParameter("pid");
            writeBiblioModsInfo(pid, out);
        } catch (Exception e) {
            out.println(e.toString());
        } finally {
            out.close();
        }
    }
    private void writeBiblioModsInfo(String unitPid, PrintWriter out) {
        try {
                String command = fedoraUrl + "/get/" + unitPid + "/BIBLIO_MODS";
                Document contentDom = UrlReader.getDocument(command);

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
                
        } catch (Exception e) {
            e.printStackTrace();
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
