/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.utils;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.utils.JNDIUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

/**
 *
 * @author Administrator
 */
public class InitServlet extends GuiceServlet  {

	@Inject
	transient KConfiguration configuration;
	
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    private void doInit(ServletContext ctx) {
    	if (this.configuration != null) {
    		log("adding configuration to servlet context");
    		ctx.setAttribute(IKeys.CONFIGURATION, this.configuration); 
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

	public KConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(KConfiguration configuration) {
		this.configuration = configuration;
	}
    
}
