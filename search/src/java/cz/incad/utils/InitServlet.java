/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.utils;

import cz.incad.Kramerius.KConfiguration;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Administrator
 */
public class InitServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet InitServlet</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet InitServlet at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
             */
        } finally {
            out.close();
        }
    }

    private void doInit(ServletContext ctx) {
        KConfiguration config = (KConfiguration) ctx.getAttribute(IKeys.CONFIGURATION);
        try{
        if (config == null) {
            String configFile = ctx.getRealPath("WEB-INF/config/config.xml");
            ctx.log("loading configuration from: " + configFile);
            config = new KConfiguration(configFile);
        }
        ctx.setAttribute(IKeys.CONFIGURATION, config);
        }catch(Exception ex){
            ctx.log(ex.toString());
        }
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        doInit(getServletContext());
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
