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

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.audio.AudioStreamForwardUtils;
import cz.incad.kramerius.audio.AudioStreamId;
import cz.incad.kramerius.audio.servlets.ServletAudioHttpRequestForwarder;
import cz.incad.kramerius.audio.urlMapping.RepositoryUrlManager;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.User;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Audio proxy servlet. Implements byte-serving of audio files. Since Fedora is
 * not capable of byte-serving (in version 4.6), this servlet cannot access
 * Fedora directly. The actual content of files to be served is stored in
 * exteranll audio repository. Track objects in Fedora contain audio datastreams
 * (MP3 and/or OGG and/or WAV) that are externally referenced datastreams with
 * url to audio repository. Mapping PID+dsID -> URL is realized by
 * RepositoryUrlManager. So this servlet is proxy that also authorizes requestes
 * to resources. 
 * Request: Client -> Audio proxy (gets URL from RepositoryUrlManager) -> external Audio repository
 * Response: Client <- Audio proxy <- external Audio repository
 *
 * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
 */
public class AudioProxyServlet extends GuiceServlet {

    private static final Logger LOGGER = Logger.getLogger(AudioProxyServlet.class.getName());

	@Inject
    IsActionAllowed actionAllowed;
    @Inject
    SolrAccess solrAccess;
    @Inject
    Provider<User> userProvider;
    

    @Inject
    RepositoryUrlManager urlManager;

    @Override
    public void init() throws ServletException {
        super.init();
        LOGGER.log(Level.INFO, "initializing {0}", AudioProxyServlet.class.getName());
    }

    @Override
    public void destroy() {
        LOGGER.log(Level.INFO, "shutting down {0}", AudioProxyServlet.class.getName());
        urlManager.close();
        ServletAudioHttpRequestForwarder.destroy();
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
        AudioStreamForwardUtils.GET(AudioStreamId.fromPathInfo(request.getPathInfo()), request, response, this.solrAccess, this.userProvider.get(),this.actionAllowed, this.urlManager);
    }

    /**
     * Handles the HTTP
     * <code>HEAD</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AudioStreamForwardUtils.HEAD(AudioStreamId.fromPathInfo(request.getPathInfo()), request, response,this.solrAccess, this.userProvider.get(),this.actionAllowed, this.urlManager);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Kramerius 4 Audio proxy servlet";
    }

    private void appendTestHeaders(HttpServletResponse response, AudioStreamId id, URL url) {
        response.setHeader("Test.ImageId", id.toString());
        response.setHeader("Test.ImageLink", url.toString());
    }
}
