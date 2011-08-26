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
package cz.incad.Kramerius.views;

import java.io.StringReader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.Kramerius.processes.ParamsLexer;
import cz.incad.Kramerius.processes.ParamsParser;

public class AbstractViewObject {

    public static final String PIDS = "pids";

    @Inject
    protected Provider<HttpServletRequest> requestProvider;

    public List getPidsParams() throws RecognitionException, TokenStreamException {
        HttpServletRequest httpServletRequest = this.requestProvider.get();
        String parameter = httpServletRequest.getParameter(PIDS);
    
        ParamsParser params = new ParamsParser(new ParamsLexer(new StringReader(parameter)));
        List paramsList = params.params();
        return paramsList;
    }

}
