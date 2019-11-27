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
package cz.incad.Kramerius.audio.servlet;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.audio.XpathEvaluator;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martin Řehánek <rehan at mzk.cz>
 */
public class AudioTracksServlet extends GuiceServlet {

	static final Logger LOGGER = java.util.logging.Logger.getLogger(AudioTracksServlet.class.getName());
    private static final String INFO_FEDORA_PREFIX = "info:fedora/";
    @Inject
    TextsService textsService;
    @Inject
    ResourceBundleService resourceBundleService;
    @Inject
    Provider<Locale> localeProvider;
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    private XPathExpression rdfModel;
    private XPathExpression rdfHasTracks;
    private XPathExpression rdfContainsTracks;
    private XPathExpression dcTitles;
    private XPathExpression dcFormats;
    private XPathExpression dsMp3;
    private XPathExpression dsOgg;
    private XPathExpression dsWav;

    @Override
    public void init() {
        XpathEvaluator evaluator = new XpathEvaluator();
        try {
            rdfModel = evaluator.createExpression("//rdf:Description/model:hasModel/@rdf:resource");
            rdfHasTracks = evaluator.createExpression("//rdf:Description/rel:hasTrack/@rdf:resource");
            rdfContainsTracks = evaluator.createExpression("//rdf:Description/rel:containsTrack/@rdf:resource");
            dcTitles = evaluator.createExpression("//dc:title");
            dcFormats = evaluator.createExpression("//dc:format");
            //dsMp3 = evaluator.createExpression("//fedora-access:datastream[@dsid='MP3']");
            dsMp3 = evaluator.createExpression("//*[local-name()='datastream'][@dsid='MP3']");
            //dsOgg = evaluator.createExpression("//fedora-access:datastream[@dsid='OGG']");
            dsOgg = evaluator.createExpression("//*[local-name()='datastream'][@dsid='OGG']");
            //dsWav = evaluator.createExpression("//fedora-access:datastream[@dsid='WAV']");
            dsWav = evaluator.createExpression("//*[local-name()='datastream'][@dsid='WAV']");
        } catch (XPathExpressionException ex) {
            //should never happen unless someone breaks xpath expressions
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String pidPath = request.getParameter("pid_path");
        if (pidPath != null) {
            String[] pid_path_parts = pidPath.split("/");
            int parts = pid_path_parts.length;
            if (parts != 0) {
                String pid = pid_path_parts[parts-1];
                String action = request.getParameter("action");
                processAction(response, pid, action);
            } else {
                response.sendError(400, "empty pid_path");
            }
        } else {
            response.sendError(400, "mandatory parameter 'pid_path' not found");
        }
    }

    private void processAction(HttpServletResponse response, String pid, String action)
            throws ServletException, IOException {
        if ("canContainTracks".equals(action)) {
            boolean canContainTracks = canContainTracks(pid);
            String json = toCanContainTracksJson(pid, canContainTracks);
            response.getOutputStream().print(json);
        } else if ("isTrack".equals(action)) {
        	boolean isTrack = isTrack(pid);
            String json = toIsTrackJson(pid, isTrack);
            response.getOutputStream().print(json);
        } else if ("getTracks".equals(action)) {
            List<String> tracksPids = getTrackPids(pid);
            List<Track> tracks = buildTracksFromPids(tracksPids);
            String json = toJson(pid, tracks);
            response.getOutputStream().print(json);
        } else {
            response.sendError(400, "illegal action '" + action + "'");
        }
    }

    private boolean canContainTracks(String pid) throws IOException {
        Document relsExt = fedoraAccess.getRelsExt(pid);
        String model = getModel(pid, relsExt);
        return "model:soundrecording".equals(model)
                || "model:soundunit".equals(model)
                || "model:track".equals(model);
    }
    
    private boolean isTrack(String pid) throws IOException {
        Document relsExt = fedoraAccess.getRelsExt(pid);
        String model = getModel(pid, relsExt);
        return "model:track".equals(model);
    }

    private String toCanContainTracksJson(String topLevelPid, boolean canContain) {
        Map map = new HashMap();
        map.put("topLevelPid", topLevelPid);
        map.put("canContainTracks", Boolean.valueOf(canContain));
        JSONObject object = JSONObject.fromObject(map);
        return object.toString();
    }
    
    private String toIsTrackJson(String topLevelPid, boolean isTrack) {
        Map map = new HashMap();
        map.put("pid", topLevelPid);
        map.put("isTrack", Boolean.valueOf(isTrack));
        JSONObject object = JSONObject.fromObject(map);
        return object.toString();
    }

    private List<String> getTrackPids(String pid) throws IOException {
        Document relsExt = fedoraAccess.getRelsExt(pid);
        String model = getModel(pid, relsExt);
        if ("model:soundrecording".equals(model)) {
            return getPidsFromRelsExtByXpath(pid, relsExt, rdfHasTracks);
        } else if ("model:soundunit".equals(model)) {
            return getPidsFromRelsExtByXpath(pid, relsExt, rdfContainsTracks);
        } else if ("model:track".equals(model)) {
            List<String> singleItemList = new ArrayList<String>(1);
            singleItemList.add(pid);
            return singleItemList;
        } else {
            LOGGER.log(Level.SEVERE, "unexpected model ({0}) found in rels-ext of object {1}", new Object[]{model, pid});
            return Collections.<String>emptyList();
        }
    }

    private String getModel(String pid, Document relsExt) {
        String modelWithPrefix = getModelFromRelsext(pid, relsExt);
        return removeInfoFedoraPrefix(modelWithPrefix, pid);
    }

    private String getModelFromRelsext(String pid, Document relsExt) {
        try {
            NodeList modelNodes = (NodeList) rdfModel.evaluate(relsExt, XPathConstants.NODESET);
            if (modelNodes.getLength() == 0) {
                LOGGER.log(Level.WARNING, "no model found for {0}", pid);
                return null;
            } else if (modelNodes.getLength() == 1) {
                return modelNodes.item(0).getNodeValue();
            } else {
                StringBuilder error = new StringBuilder();
                error.append("more than one models found for ").append(pid);
                error.append('(');
                for (int i = 0; i < modelNodes.getLength(); i++) {
                    error.append(modelNodes.item(i).getNodeValue());
                    if (i != modelNodes.getLength() - 1) {
                        error.append(',');
                    }
                }
                error.append(')');
                error.append(", using first one");
                LOGGER.warning(error.toString());
                return modelNodes.item(0).getNodeValue();
            }
        } catch (XPathExpressionException ex) {
            //should never happen unless someone breaks xpath expressions
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private List<String> getPidsFromRelsExtByXpath(String pid, Document relsExt, XPathExpression xpath) {
        try {
            Object result = xpath.evaluate(relsExt, XPathConstants.NODESET);
            NodeList nodes = (NodeList) result;
            return pidListFromNodeList(nodes, pid);
        } catch (XPathExpressionException ex) {
            //should never happen unless someone breaks xpath expressions
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.<String>emptyList();
        }
    }

    private List<String> pidListFromNodeList(NodeList nodes, String pid) {
        List<String> result = new ArrayList<String>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            String prefixed = nodes.item(i).getNodeValue();
            String unprefixed = removeInfoFedoraPrefix(prefixed, pid);
            if (unprefixed != null) {
                result.add(unprefixed);
            }
        }
        return result;
    }

    private String removeInfoFedoraPrefix(String prefixed, String pid) {
        if (prefixed == null) {
            return null;
        } else if (!prefixed.startsWith(INFO_FEDORA_PREFIX)) {
            LOGGER.log(Level.SEVERE, "Illegal content ({0}) found in rels-ext of ({1})", new Object[]{prefixed, pid});
            return null;
        } else {
            return prefixed.substring(INFO_FEDORA_PREFIX.length());
        }
    }

    private List<Track> buildTracksFromPids(List<String> pids) {
        List<Track> result = new ArrayList<Track>(pids.size());
        for (String pid : pids) {
            try {
                Track track = buildTrack(pid);
                if (track != null) {
                    result.add(track);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "cannot read datastream DC of " + pid, ex);
            }
        }
        return result;
    }

    private Track buildTrack(String pid) throws IOException {
        try {
            Document dC = fedoraAccess.getDC(pid);
            NodeList titleNodes = (NodeList) dcTitles.evaluate(dC, XPathConstants.NODESET);
            String title = buildTitle(titleNodes, pid);
            NodeList formatNodes = (NodeList) dcFormats.evaluate(dC, XPathConstants.NODESET);
            String trackLength = buildTrackLength(formatNodes, pid);
            Boolean[] formats = getAvailableFormats(pid);
            return new Track(pid, title, trackLength, formats[0], formats[1], formats[2]);
        } catch (XPathExpressionException ex) {
            //should never happen unless someone breaks xpath expressions
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private String buildTitle(NodeList titleNodes, String pid) {
        int length = titleNodes.getLength();
        if (length == 0) {
            LOGGER.log(Level.WARNING, "no element dc:title found in datastream DC of object {0}", pid);
            return "";
        } else if (length == 1) {
            return titleNodes.item(0).getTextContent();
        } else {
            if (length > 2) {
                LOGGER.log(Level.WARNING, "too many dc:title elements ({0}) found in datastream DC of object {1}, using first two", new Object[]{length, pid});
            }
            return titleNodes.item(0).getTextContent()
                    + " ("
                    + titleNodes.item(1).getTextContent()
                    + ")";
        }
    }

    private String buildTrackLength(NodeList formatNodes, String pid) {
        //podle specifikace muze byt v dc:format jen "sound recording" 
        //nebo delka tracku
        for (int i = 0; i < formatNodes.getLength(); i++) {
            String content = formatNodes.item(i).getTextContent();
            if (!"sound recording".equals(content)) {
                return content;
            }
        }
        LOGGER.log(Level.WARNING, "track length not found in datastream DC of object {0}", pid);
        return "";
    }

    private Boolean[] getAvailableFormats(String pid) {
        try {
            Document doc = fedoraAccess.getFedoraDataStreamsListAsDocument(pid);
            boolean mp3 = ((NodeList) dsMp3.evaluate(doc, XPathConstants.NODESET)).getLength() == 1;
            boolean ogg = ((NodeList) dsOgg.evaluate(doc, XPathConstants.NODESET)).getLength() == 1;
            boolean wav = ((NodeList) dsWav.evaluate(doc, XPathConstants.NODESET)).getLength() == 1;
            return new Boolean[]{mp3, ogg, wav};
        } catch (XPathExpressionException ex) {
            //should never happen unless someone breaks xpath expressions
            LOGGER.log(Level.SEVERE, null, ex);
            return new Boolean[]{false, false, false};
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "failed to load list of datastreams of object {0}", pid);
            return new Boolean[]{false, false, false};
        }
    }

    private String toJson(String topLevelPid, List<Track> tracks) {
        Object[] trackArray = toJsonArray(tracks);
        Map map = new HashMap();
        map.put("topLevelPid", topLevelPid);
        map.put("totalTracks", Integer.valueOf(tracks.size()));
        map.put("tracks", trackArray);
        JSONObject object = JSONObject.fromObject(map);
        return object.toString();
    }

    private Object[] toJsonArray(List<Track> tracks) {
        List<JSONObject> result = new ArrayList<JSONObject>(tracks.size());
        for (Track track : tracks) {
            result.add(track.toJsonObject());
        }
        return result.toArray();
    }

    class Track {

        private final String pid;
        private final String title;
        private final String length;
        private final boolean mp3Available;
        private final boolean oggAvailable;
        private final boolean wavAvailable;

        public Track(String pid, String title, String length, boolean mp3Available, boolean oggAvailable, boolean wavAvailable) {
            this.pid = pid;
            this.title = title;
            this.length = length;
            this.mp3Available = mp3Available;
            this.oggAvailable = oggAvailable;
            this.wavAvailable = wavAvailable;
        }

        public String getPid() {
            return pid;
        }

        public String getTitle() {
            return title;
        }

        public String getLength() {
            return length;
        }

        public boolean isMp3Available() {
            return mp3Available;
        }

        public boolean isOggAvailable() {
            return oggAvailable;
        }

        public boolean isWavAvailable() {
            return wavAvailable;
        }

        public JSONObject toJsonObject() {
            Map map = new HashMap();
            map.put("pid", pid);
            map.put("title", title);
            map.put("length", length);
            map.put("mp3", Boolean.valueOf(mp3Available));
            map.put("ogg", Boolean.valueOf(oggAvailable));
            map.put("wav", Boolean.valueOf(wavAvailable));
            return JSONObject.fromObject(map);
        }
    }
}
