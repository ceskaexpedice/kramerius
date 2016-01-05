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
package cz.incad.kramerius.audio;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Forwarder forwards (copies) headers from client's http GET request to audio
 * server to audio proxy's http GET request to repository.
 *
 * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
 */
public class GetRequestHeaderForwarder {

    private final HttpServletRequest clientRequest;
    private final HttpRequestBase proxyRequest;

    /**
     * Initializes Forwarder.
     *
     * @param clientRequest client request to proxy
     * @param proxyRequest proxy request to repository
     *
     */
    public GetRequestHeaderForwarder(HttpServletRequest clientRequest, HttpRequestBase proxyRequest) {
        this.clientRequest = clientRequest;
        this.proxyRequest = proxyRequest;
    }

    /**
     * Forwards value of header if header is found. Uses only first appearance
     * of the header therefore doesn't work correctly for multiple header
     * values.
     *
     * @param headerName name of header
     * @return value of header if header is found or null
     *
     */
    public String forwardHeaderIfPresent(String headerName) {
        String headerValue = clientRequest.getHeader(headerName);
        if (headerValue != null) {
            //System.err.println("found request header " + headerName + ": " + headerValue);
            proxyRequest.setHeader(headerName, headerValue);
            return headerValue;
        }
        return null;
    }
}
