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

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.I18NServlet.Actions;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.printing.PrintingService;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class PrintQueue extends GuiceServlet {

    public static final String PID_FROM="pidFrom";
    public static final String HOW_MANY="howMany";
    public static final String PATH="path";

    @Inject
    PrintingService printService;

    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration configuration;
    @Inject
    SolrAccess solrAccess;

    

    

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            this.renderDynamicPDF(req, resp);
        } catch (ProcessSubtreeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }





    public void renderDynamicPDF(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException, ProcessSubtreeException {
        String imgServletUrl = ApplicationURL.applicationURL(req)+"/img";
        if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
            imgServletUrl = configuration.getApplicationURL()+"img";
        }
        String i18nUrl = ApplicationURL.applicationURL(req)+"/i18n";
        if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
            i18nUrl = configuration.getApplicationURL()+"i18n";
        }
        String from = req.getParameter(PID_FROM);
        String howMany = req.getParameter(HOW_MANY);
        
        ObjectPidsPath[] paths = this.solrAccess.getPath(from);
        if (paths.length > 0) {
            this.printService.print(paths[0], from, Integer.parseInt(howMany),  imgServletUrl, i18nUrl);
        } else {
            this.printService.print(new ObjectPidsPath(from), from, Integer.parseInt(howMany),  imgServletUrl, i18nUrl);
        }
        
        
//        service.
//        
//        service.dynamicPDFExport(parentUuid(from), from, Integer.parseInt(howMany), from, resp.getOutputStream(), imgServletUrl, i18nUrl);
    }

}
