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
package cz.incad.Kramerius.views.rights;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.antlr.stringtemplate.StringTemplate;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class DisplaySecuredStreamsView extends AbstractRightsView {

    @Inject
    KConfiguration configuration;
    
    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    
    public List<StreamAndPid> getSecuredStreams() throws RecognitionException, TokenStreamException {
        List<StreamAndPid> streamsAndPids = new ArrayList<DisplaySecuredStreamsView.StreamAndPid>();
        String[] streams = configuration.getSecuredAditionalStreams();
        List<String> pidsParams = getPidsParams();
        for (String strm : streams) {
            streamsAndPids.add(new StreamAndPid(pidsParams , strm));
        }
        return streamsAndPids;
    }
    
    public static class StreamAndPid {
        
        private List<String> pids;
        
        private String stream;
        
        public StreamAndPid(List<String> pids, String stream) {
            super();
            this.pids = pids;
            this.stream = stream;
        }
        
        public List<String> getPids() {
            return pids;
        }
        
        public String getStreamName() {
            return stream;
        }
        
        public String getPidStructsArguments() {
            StringTemplate tmpl = new StringTemplate("[$pids:{pid| { pid: '$pid$/$stream$' } };separator=\",\"$]");
            tmpl.setAttribute("pids", this.pids);
            tmpl.setAttribute("stream", this.stream);
            return tmpl.toString();
        }
    }
}
