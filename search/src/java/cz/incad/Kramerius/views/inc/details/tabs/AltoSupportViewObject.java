/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.Kramerius.views.inc.details.tabs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.utils.ALTOUtils;
import cz.incad.kramerius.utils.ALTOUtils.AltoDisected;
import cz.incad.kramerius.utils.XMLUtils;

/**
 * @author pavels
 *
 */
public class AltoSupportViewObject implements Initializable {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AltoSupportViewObject.class.getName());
    
    private static final String PID = "pid";

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    
    private String imagePid = null;
 
    @Override
    public void init() {
        try {
            HttpServletRequest request = this.requestProvider.get();
            String pid = request.getParameter(PID);
            this.imagePid = fedoraAccess.findFirstViewablePid(pid);
            LOGGER.fine("processing pid '"+this.imagePid+"'");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }



    public String getSearchQuery() {
        HttpServletRequest request = this.requestProvider.get();
        if (request.getParameterMap().containsKey("q")) {
            String par = request.getParameter("q");
            par = par.replace("'", "\\'");
            return "'"+par+"'";
        } else return "null";
    }
    
    public boolean getAltoStreamAvailabilityFlag() throws IOException {
        boolean streamAvailable = fedoraAccess.isStreamAvailable(this.imagePid, "ALTO");
        return streamAvailable;
    }

    public boolean getQueryAvailabilityFlag() {
        HttpServletRequest request = this.requestProvider.get();
        return (request.getParameterMap().containsKey("q"));
    }
    

    private Document getAltoDocument() throws IOException, ParserConfigurationException, SAXException {
        InputStream is = this.fedoraAccess.getDataStream(this.imagePid, "ALTO");
        return XMLUtils.parseDocument(is);
    }

    public String getAltoJSONInitialization() throws IOException, ParserConfigurationException, SAXException {
        HttpServletRequest request = this.requestProvider.get();
        if (request.getParameterMap().containsKey("q")) {
            String par = request.getParameter("q");
            if (getAltoStreamAvailabilityFlag()) {
                Document parsed = getAltoDocument();
                AltoDisected disected = ALTOUtils.disectAlto(par, parsed);
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("alto", disected.toJSON());
                return jsonObj.toString();
            }
        }
        return "";
    }

}
