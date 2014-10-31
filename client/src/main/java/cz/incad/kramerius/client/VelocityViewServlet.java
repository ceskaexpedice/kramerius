/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author alberto
 */
public class VelocityViewServlet extends org.apache.velocity.tools.view.VelocityViewServlet {

    

    @Override
    protected void setContentType(HttpServletRequest request,
            HttpServletResponse response) {
        if (request.getRequestURI().endsWith(".css")) {
            response.setContentType("text/css");
        } else {
            response.setContentType(getVelocityView().getDefaultContentType());
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
