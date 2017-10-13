/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.Kramerius;

import java.awt.print.PrinterException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.printing.PrintingService;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.params.ParamsLexer;
import cz.incad.kramerius.utils.params.ParamsParser;

public class PrintQueue extends GuiceServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PrintQueue.class.getName());
    
    public static final String PID_FROM="pidFrom";
    public static final String HOW_MANY="howMany";
    public static final String PATH="path";

    @Inject
    protected PrintingService printService;
    
    @Inject
    @Named("securedFedoraAccess")
    protected FedoraAccess fedoraAccess;
    @Inject
    protected KConfiguration configuration;
    @Inject
    protected SolrAccess solrAccess;


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.print(req, resp);
        } catch (ProcessSubtreeException e) {
            resp.setContentType("text/plain");
            resp.getWriter().println("{status:'failed'}");
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (NumberFormatException e) {
            resp.setContentType("text/plain");
            resp.getWriter().println("{status:'failed'}");
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (PrinterException e) {
            resp.setContentType("text/plain");
            resp.getWriter().println("{status:'failed'}");
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (PrintException e) {
            resp.setContentType("text/plain");
            resp.getWriter().println("{status:'failed'}");
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }



    public void print(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException, ProcessSubtreeException, NumberFormatException, PrinterException, PrintException {

        String imgServletUrl = ApplicationURL.applicationURL(req)+"/img";
        if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
            imgServletUrl = configuration.getApplicationURL()+"img";
        }
        
        String i18nUrl = ApplicationURL.applicationURL(req)+"/i18n";
        if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
            i18nUrl = configuration.getApplicationURL()+"i18n";
        }
        String action = req.getParameter("action");
        Action.valueOf(action).print(req,resp, this.printService,imgServletUrl, i18nUrl);
    }
    
    
    static enum Action {
        
        PARENT {
            @Override
            protected void print(HttpServletRequest request, HttpServletResponse response, PrintingService service, String imgServlet, String i18nservlet) throws IOException, ProcessSubtreeException, PrinterException, PrintException {
                String from = request.getParameter(PID_FROM);
                service.printMaster(from, imgServlet, i18nservlet);
                response.setContentType("text/plain");
                response.getWriter().println("{status:'printing'}");
            }
        }, SELECTION {
            @Override
            protected void print(HttpServletRequest request, HttpServletResponse response, PrintingService service, String imgServlet, String i18nservlet) throws IOException, ProcessSubtreeException, PrinterException, PrintException {
                try {
                    String par = request.getParameter("pids");
                    ParamsParser parser = new ParamsParser(new ParamsLexer(new StringReader(par)));
                    List params = parser.params();
                    service.printSelection((String[])params.toArray(new String[params.size()]), imgServlet, i18nservlet);
                    response.setContentType("text/plain");
                    response.getWriter().println("{status:'printing'}");
                } catch (RecognitionException e) {
                    response.setContentType("text/plain");
                    response.getWriter().println("{status:'failed'}");
                    throw new IOException(e);
                } catch (TokenStreamException e) {
                    response.setContentType("text/plain");
                    response.getWriter().println("{status:'failed'}");
                    throw new IOException(e);
                }
            }
        };
        
        protected abstract void print(HttpServletRequest request, HttpServletResponse response,PrintingService service, String imgServlet, String i18nservlet) throws IOException, ProcessSubtreeException, PrinterException, PrintException;
    }

}
