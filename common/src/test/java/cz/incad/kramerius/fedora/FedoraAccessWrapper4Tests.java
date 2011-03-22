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
package cz.incad.kramerius.fedora;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.utils.XMLUtils;

public class FedoraAccessWrapper4Tests implements FedoraAccess {

    private FedoraAccess fedoraAccess;

    
    @Inject
    public FedoraAccessWrapper4Tests(@Named("rawFedoraAccess") FedoraAccess fedoraAccess) {
        super();
        this.fedoraAccess = fedoraAccess;
    }

    public Document getRelsExt(String uuid) throws IOException {
        try {
            Map<String, String> map = new HashMap<String, String>(){{
               put("0eaa6730-9068-11dd-97de-000d606f5dc6","res/0eaa6730-9068-11dd-97de-000d606f5dc6.xml");
            }};
            
            String path = "res/"+uuid+".xml";
            return XMLUtils.parseDocument(this.getClass().getResourceAsStream(path));
            //return fedoraAccess.getRelsExt(uuid);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public String getKrameriusModelName(Document relsExt) throws IOException {
        return fedoraAccess.getKrameriusModelName(relsExt);
    }

    public String getKrameriusModelName(String uuid) throws IOException {
        return fedoraAccess.getKrameriusModelName(uuid);
    }


    public List<String> getModelsOfRel(Document relsExt) {
        return fedoraAccess.getModelsOfRel(relsExt);
    }

    public List<String> getModelsOfRel(String uuid) throws IOException {
        return fedoraAccess.getModelsOfRel(uuid);
    }

    public String getDonator(Document relsExt) {
        return fedoraAccess.getDonator(relsExt);
    }

    public String getDonator(String uuid) throws IOException {
        return fedoraAccess.getDonator(uuid);
    }

    public void processRelsExt(String uuid, RelsExtHandler handler) throws IOException {
        fedoraAccess.processRelsExt(uuid, handler);
    }

    public void processRelsExt(Document relsExtDocument, RelsExtHandler handler) throws IOException {
        fedoraAccess.processRelsExt(relsExtDocument, handler);
    }

    public Document getBiblioMods(String uuid) throws IOException {
        return fedoraAccess.getBiblioMods(uuid);
    }

    public Document getDC(String uuid) throws IOException {
        return fedoraAccess.getDC(uuid);
    }

    public String findFirstViewablePid(String uuid) throws IOException {
        return fedoraAccess.findFirstViewablePid(uuid);
    }

    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException {
        return fedoraAccess.getFirstViewablePath(pids, models);
    }

    public List<Element> getPages(String uuid, boolean deep) throws IOException {
        return fedoraAccess.getPages(uuid, deep);
    }

    public List<Element> getPages(String uuid, Element rootElementOfRelsExt) throws IOException {
        return fedoraAccess.getPages(uuid, rootElementOfRelsExt);
    }

    public InputStream getSmallThumbnail(String uuid) throws IOException {
        return fedoraAccess.getSmallThumbnail(uuid);
    }

    public Document getSmallThumbnailProfile(String uuid) throws IOException {
        return fedoraAccess.getSmallThumbnailProfile(uuid);
    }

    public String getSmallThumbnailMimeType(String uuid) throws IOException, XPathExpressionException {
        return fedoraAccess.getSmallThumbnailMimeType(uuid);
    }

    public boolean isFullthumbnailAvailable(String uuid) throws IOException {
        return fedoraAccess.isFullthumbnailAvailable(uuid);
    }

    public InputStream getFullThumbnail(String uuid) throws IOException {
        return fedoraAccess.getFullThumbnail(uuid);
    }

    public Document getFullThumbnailProfile(String uuid) throws IOException {
        return fedoraAccess.getFullThumbnailProfile(uuid);
    }

    public String getFullThumbnailMimeType(String uuid) throws IOException, XPathExpressionException {
        return fedoraAccess.getFullThumbnailMimeType(uuid);
    }

    public InputStream getImageFULL(String uuid) throws IOException {
        return fedoraAccess.getImageFULL(uuid);
    }

    public Document getImageFULLProfile(String uuid) throws IOException {
        return fedoraAccess.getImageFULLProfile(uuid);
    }

    public String getImageFULLMimeType(String uuid) throws IOException, XPathExpressionException {
        return fedoraAccess.getImageFULLMimeType(uuid);
    }

    public boolean isImageFULLAvailable(String uuid) throws IOException {
        return fedoraAccess.isImageFULLAvailable(uuid);
    }

    public boolean isStreamAvailable(String uuid, String streamName) throws IOException {
        return fedoraAccess.isStreamAvailable(uuid, streamName);
    }

    public boolean isContentAccessible(String uuid) throws IOException {
        return fedoraAccess.isContentAccessible(uuid);
    }

    public FedoraAPIA getAPIA() {
        return fedoraAccess.getAPIA();
    }

    public FedoraAPIM getAPIM() {
        return fedoraAccess.getAPIM();
    }

    public ObjectFactory getObjectFactory() {
        return fedoraAccess.getObjectFactory();
    }

    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException {
        fedoraAccess.processSubtree(pid, processor);
    }

    public Set<String> getPids(String pid) throws IOException {
        return fedoraAccess.getPids(pid);
    }

    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        return fedoraAccess.getDataStream(pid, datastreamName);
    }

    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        return fedoraAccess.getMimeTypeForStream(pid, datastreamName);
    }
    
    
}
