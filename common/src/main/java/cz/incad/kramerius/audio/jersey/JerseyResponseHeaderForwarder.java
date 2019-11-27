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
package cz.incad.kramerius.audio.jersey;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import cz.incad.kramerius.audio.ResponseHeaderForwarder;

/**
 * Forwarder forwards (copies) headers from repository's response to audio proxy
 * to audio proxy's response to client.
 *
 * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
 */
public class JerseyResponseHeaderForwarder  implements ResponseHeaderForwarder{

    private final HttpResponse repositoryResponse;
    private final ResponseBuilder proxyResponse;

    /**
     * Initializes Forwarder.
     *
     * @param repositoryResponse response from repository to proxy
     * @param proxyResponse response from proxy to client
     *
     */
    public JerseyResponseHeaderForwarder(HttpResponse repositoryResponse, ResponseBuilder proxyResponse) {
        this.repositoryResponse = repositoryResponse;
        this.proxyResponse = proxyResponse;
    }

    /**
     * Forwards value of header if header is found. Uses only first appearance
     * of the header therefore doesn't work correctly for multiple headers with
     * same name.
     *
     * @param headerName name of header
     * @return value of header if header is found or null
     */
    public String forwardHeaderIfPresent(String headerName) {
        Header header = repositoryResponse.getFirstHeader(headerName);
        if (header != null) {
            //System.err.println("found response header " + header.getName() + ": " + header.getValue());
            proxyResponse.header(header.getName(), header.getValue());
            return header.getValue();
        }
        return null;
    }
}
