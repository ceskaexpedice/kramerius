/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.Kramerius;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.IKeys;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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


import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 *
 * @author Administrator
 */
public class GetFirstPageThumb extends GuiceServlet {

    private static final String UUID_PARAMETER = "uuid";

    @Inject
	@Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        try {
            String uuid = req.getParameter(UUID_PARAMETER);
            if (uuid == null) {
                throw new ServletException("uuid cannot be null!");
            }
            factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            String page_uuid = findPagePid(uuid);
            if (page_uuid != null) {
                out.print("<img src=\"thumb?uuid="+page_uuid.substring(5)+"&scaledHeight=100\" />");
            }
        } finally {
            out.close();
        }
    }
    Document contentDom;
    String command;
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;

    private String findPagePid(String pid) {
        int num = 0;
        ArrayList<String> pids = new ArrayList<String>();
        try {
            Document contentDom = fedoraAccess.getRelsExt(pid);
            contentDom = UrlReader.getDocument(command);
            expr = xpath.compile("/RDF/Description/*");
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                String nodeName = childnode.getNodeName();
                if (nodeName.contains("hasPage")) {
                    return childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1];
                } else {
                    pids.add(childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                }
            }
            for (String relpid : pids) {
                return findPagePid(relpid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    
	public FedoraAccess getFedoraAccess() {
		return fedoraAccess;
	}

	public void setFedoraAccess(FedoraAccess fedoraAccess) {
		this.fedoraAccess = fedoraAccess;
	}

    
}
