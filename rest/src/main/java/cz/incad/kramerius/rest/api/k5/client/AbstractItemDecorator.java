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
import java.util.List;

import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;



public abstract class AbstractItemDecorator extends AbstractDecorator  {

	
	protected TokenizedPath itemContext(List<String> input) {

		// basic path
		TokenizedPath bcont = super.basicContext(input);
		if (!bcont.isParsed()) return bcont;
		
		List<String> atoms = bcont.getRestPath();
		List<String> retvals = new ArrayList<String>(atoms);
		if (!retvals.isEmpty()) {
			if (!retvals.get(0).equals("item")) return new TokenizedPath(false, atoms);	
			retvals.remove(0);
		} else return new TokenizedPath(false, atoms);

		if (!retvals.isEmpty()) {
			try {
				PIDParser pidParser = new PIDParser(retvals.get(0));
				pidParser.objectPid();
				retvals.remove(0);
			} catch (LexerException e) {
				// parse error 
				return new TokenizedPath(false, atoms);
			}
		} else return new TokenizedPath(false, atoms);
		
		return new TokenizedPath(true, retvals);
	}
}
