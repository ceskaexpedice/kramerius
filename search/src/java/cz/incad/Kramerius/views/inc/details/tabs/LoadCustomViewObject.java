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
package cz.incad.Kramerius.views.inc.details.tabs;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.I18NServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.XSLService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.UnicodeUtil;
import cz.incad.kramerius.utils.XMLUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;

public class LoadCustomViewObject implements Initializable {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LoadCustomViewObject.class.getName());

    @Inject
    Provider<HttpServletRequest> requestProvider;


    @Inject
    Provider<Locale> localesProvider;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    XSLService xslService;

    @Inject
    ResourceBundleService resourceBundleService;


    @Override
    public void init() {
    }


    public String getI18NServlet() {
        String i18nServlet = I18NServlet.i18nServlet(requestProvider.get()) + "?action=bundle&lang=" + localesProvider.get().getLanguage() + "&country=" + localesProvider.get().getCountry() + "&name=labels";
        return i18nServlet;
    }

    private String escapeXML(String s) {
        return s.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;").replaceAll("&", "&amp;");
    }


    // rewritten from jsp - no changes
    public String getContent() throws IOException, ParserConfigurationException, SAXException {
        StringBuilder stringBuilder = new StringBuilder();

        String tab = this.requestProvider.get().getParameter("tab");
        String ds = tab;
        String xsl = tab;
        if (tab.indexOf('.') >= 0) {
            ds = tab.split("\\.")[0];
            xsl = tab.split("\\.")[1] + ".xsl";
        }

        String pid_path = this.requestProvider.get().getParameter("pid_path");
        List<String> pids = Arrays.asList(pid_path.split("/"));
        if (ds.startsWith("-")) {
            Collections.reverse(pids);
            ds = ds.substring(1);
        }
        for (String pid : pids) {
            if (fedoraAccess.isStreamAvailable(pid, ds)) {

                String mime = fedoraAccess.getMimeTypeForStream(pid, ds);
                if (mime.equals("text/plain")) {
                    try {
                        InputStream is = fedoraAccess.getDataStream(pid, ds);
                        byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(is);
                        String enc = UnicodeUtil.getEncoding(bytes);
                        ByteArrayInputStream is2 = new ByteArrayInputStream(bytes);
                        stringBuilder.append("<textarea style=\"width:98%; height:98%; border:0; \">" + IOUtils.readAsString(is2, Charset.forName(enc), true) + "</textarea>");
                    } catch (cz.incad.kramerius.security.SecurityException e) {
                        LOGGER.log(Level.INFO, e.getMessage());
                    }
                } else if (mime.equals("text/xml") || mime.equals("application/rdf+xml")) {
                    try {
                        if (xslService.isAvailable(xsl)) {
                            org.w3c.dom.Document xml = XMLUtils.parseDocument(fedoraAccess.getDataStream(pid, ds), true);
                            String text = xslService.transform(xml, xsl, this.localesProvider.get());
                            stringBuilder.append(text);
                        } else {
                            String xmltext = org.apache.commons.io.IOUtils.toString(fedoraAccess.getDataStream(pid, ds), Charset.forName("UTF-8"));
                            stringBuilder.append(StringEscapeUtils.escapeHtml4(xmltext));
                        }
                    } catch (cz.incad.kramerius.security.SecurityException e) {
                        LOGGER.log(Level.INFO, e.getMessage());
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                } else if (mime.equals("text/html")) {
                    try {
                        String xmltext = org.apache.commons.io.IOUtils.toString(fedoraAccess.getDataStream(pid, ds), Charset.forName("UTF-8"));
                        stringBuilder.append(xmltext);
                    } catch (cz.incad.kramerius.security.SecurityException e) {
                        LOGGER.log(Level.INFO, e.getMessage());
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }
        return stringBuilder.toString();
    }
}
