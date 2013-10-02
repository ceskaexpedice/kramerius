/*
 * Copyright (C) 2012 Martin Řehánek <rehan at mzk.cz>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.Kramerius.audio.urlMapping;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.Kramerius.Initializable;
import cz.incad.Kramerius.audio.AudioStreamId;
import cz.incad.Kramerius.audio.XpathEvaluator;
import cz.incad.kramerius.FedoraAccess;
import org.w3c.dom.Document;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This implementation gets PID+DS -> URL from Fedora. Urls are present in
 * objects' datastreams as externally referenced datastreams. Manager reads the
 * datastream's xml content, obtains the url and serves it to client. It also
 * caches found mapping by means of Ehcache. 
 *
 *
 * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
 */
public class CachingFedoraUrlManager implements RepositoryUrlManager, Initializable {

    private static final Logger LOGGER = Logger.getLogger(CachingFedoraUrlManager.class.getName());
    private static final RepositoryUrlCache linkCache = new EhcacheUrlCache();
    private XPathExpression dsLocation;
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Override
    public final void init() {
        //nothing here, initialization in constructor
    }

    public CachingFedoraUrlManager() throws IOException {
        LOGGER.log(Level.INFO, "initializing {0}", CachingFedoraUrlManager.class.getName());
        this.dsLocation = createDsLocationExpression();
    }

    private XPathExpression createDsLocationExpression() {
        try {
            XpathEvaluator xpathEvaluator = new XpathEvaluator();
            //return xpathEvaluator.createExpression("//fedora-management:dsLocation");
            return xpathEvaluator.createExpression("//*[local-name()='dsLocation']");
        } catch (XPathExpressionException ex) {
            //should never happen unless someone breaks xpath expressions
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public URL getAudiostreamRepositoryUrl(AudioStreamId id) throws IOException {
        URL urlFromCache = linkCache.getUrl(id);
        if (urlFromCache != null) { //cache hit
            return urlFromCache;
        } else {//cache miss
            URL urlFromFedora = getUrlFromFedora(id);
            if (urlFromFedora != null) {
                linkCache.storeUrl(id, urlFromFedora);
            }
            return urlFromFedora;
        }
    }

    private URL getUrlFromFedora(AudioStreamId id) throws IOException {
        LOGGER.log(Level.FINE, "getting url for {0}", id);
        try {
            Document datastreamXml = fedoraAccess.getDataStreamXmlAsDocument(id.getPid(), id.getFormat().name());
            URL url = urlFromDatastream(datastreamXml);
            LOGGER.log(Level.FINE, "found url {0} for {1}", new Object[]{url, id});
            return url;
        } catch (SecurityException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
    }

    private URL urlFromDatastream(Document datastreamXml) throws MalformedURLException {
        try {
            String xpathResult = (String) dsLocation.evaluate(datastreamXml, XPathConstants.STRING);
            return new URL(xpathResult);
        } catch (XPathExpressionException ex) {
            //should never happen unless someone breaks xpath expressions
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    //
    @Override
    public void close() {
        LOGGER.log(Level.INFO, "destroying {0}", CachingFedoraUrlManager.class.getName());
        if (linkCache != null) {
            linkCache.close();
        }
    }
}
