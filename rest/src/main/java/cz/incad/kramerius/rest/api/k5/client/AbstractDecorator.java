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
package cz.incad.kramerius.rest.api.k5.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.json.JSONObject;


public abstract class AbstractDecorator implements JSONDecorator {

	protected static String DELITIMER = ".";

	protected Map<String, Object> context = new HashMap<String, Object>();
	
	@Override
	public Map<String, Object> getRunningContext() {
		return context;
	}

	@Override
	public void before(Map<String, Object> runningContext) {
		this.context = runningContext;
	}

	@Override
	public void after() {}

	protected String getPidFromJSON(JSONObject jsonObject) {
		return jsonObject.getString("pid");
	}

	protected static List<String> tokenize(String input) {
		StringTokenizer tokenizer = new StringTokenizer(input,"/");
		List<String> strs = new ArrayList<String>();
		while(tokenizer.hasMoreTokens()) {
			strs.add(tokenizer.nextToken());
		}
		return strs;
	}

	protected TokenizedPath basicContext(List<String> atoms) {
		List<String> retvals = new ArrayList<String>(atoms);
		if (!retvals.isEmpty()) {
			if (!retvals.get(0).equals("v5.0")) return new TokenizedPath(false, atoms);	
			retvals.remove(0);
		} else return new TokenizedPath(false, atoms);
	
		// version context
		if (!retvals.isEmpty()) {
			if (!retvals.get(0).equals("k5")) return new TokenizedPath(false, atoms);;	
			retvals.remove(0);
		} else return new TokenizedPath(false, atoms);;
		
		return new TokenizedPath(true, retvals);
	}


	protected static String construct(String ... keys) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < keys.length; i++) {
			if (i > 0) builder.append(DELITIMER);
			builder.append(keys[i]);
		}
		return builder.toString();
	}

	
	
	/**
	 * Tokenized path
	 * @author pavels
	 */
	public static final class TokenizedPath {
		
		private boolean parsed = false;
		private List<String> restPath;
		
		public TokenizedPath(boolean parsed, List<String> restPath) {
			super();
			this.parsed = parsed;
			this.restPath = restPath;
		}
		
		public boolean isParsed() {
			return parsed;
		}
		
		public List<String> getRestPath() {
			return restPath;
		}
	
		@Override
		public String toString() {
			return "TokenizedPath [parsed=" + parsed + ", restPath=" + restPath + "]";
		}
	}

}
