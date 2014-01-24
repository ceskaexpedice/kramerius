/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.api.k5.client.feeder.decorators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.AbstractDecorator;
import cz.incad.kramerius.rest.api.k5.client.AbstractDecorator.TokenizedPath;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public abstract class AbstractFeederDecorator extends AbstractDecorator {

	public static final String FEED_KEY="FEED";

	public static String key(String key) {
		return AbstractDecorator.construct(FEED_KEY, key);
	}

	public static final String SOLR_PID_DOCUMENT_KEY ="solr_pid_document";
	
	public Document getSolrPidDocument(String pid, Map<String, Object> context, SolrAccess solrAccess) throws IOException {
		String key = SOLR_PID_DOCUMENT_KEY+"_"+pid;
		if (!context.containsKey(key)) {
			context.put(key, solrAccess.getSolrDataDocument(pid));
		}
		return (Document) context.get(key);
	}
	
	protected TokenizedPath feederContext(List<String> input) {

		// basic path
		TokenizedPath bcont = super.basicContext(input);
		if (!bcont.isParsed()) return bcont;
		
		List<String> atoms = bcont.getRestPath();
		List<String> retvals = new ArrayList<String>(atoms);
		if (!retvals.isEmpty()) {
			if (!retvals.get(0).equals("feed")) return new TokenizedPath(false, atoms);	
			retvals.remove(0);
		} else return new TokenizedPath(false, atoms);

		
		return new TokenizedPath(true, retvals);
	}

	protected boolean mostDesirableOrNewest(TokenizedPath fctx) {
		return fctx.getRestPath().get(0).equals("mostdesirable") || fctx.getRestPath().get(0).equals("newest");
	}

	
}
