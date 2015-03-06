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
package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.rest.api.k5.client.AbstractDecorator;
import cz.incad.kramerius.rest.api.k5.client.AbstractDecorator.TokenizedPath;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Abstract decorator for ~/item context
 * 
 * @author pavels
 */
public abstract class AbstractItemDecorator extends AbstractDecorator {

    public static final Logger LOGGER = Logger
            .getLogger(AbstractItemDecorator.class.getName());

    public static final String ITEM_KEY = "ITEM";

    /**
     * Construct key
     * 
     * @param key
     * @return
     */
    public static String key(String key) {
        return AbstractDecorator.construct(ITEM_KEY, key);
    }

    /**
     * Parse item context
     * 
     * @param input
     *            tokenized path
     * @return
     * @see super{@link #tokenize(String)}
     */
    protected TokenizedPath itemContext(List<String> input) {

        // basic path
        TokenizedPath bcont = super.basicContext(input);
        if (!bcont.isParsed())
            return bcont;

        List<String> atoms = bcont.getRestPath();
        List<String> retvals = new ArrayList<String>(atoms);
        if (!retvals.isEmpty()) {
            if (!retvals.get(0).equals("item"))
                return new TokenizedPath(false, atoms);
            retvals.remove(0);
        } else
            return new TokenizedPath(false, atoms);

        if (!retvals.isEmpty()) {
            try {
                String sform = retvals.get(0);
                if (PIDSupport.isComposedPID(sform)) {
                    String first = PIDSupport.first(sform);
                    String next = PIDSupport.rest(sform);
                    PIDParser pidParser = new PIDParser(first);
                    pidParser.objectPid();
                    retvals.remove(0);
                } else {
                    PIDParser pidParser = new PIDParser(sform);
                    pidParser.objectPid();
                    retvals.remove(0);
                }
            } catch (LexerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                // parse error
                return new TokenizedPath(false, atoms);
            }
        } else
            return new TokenizedPath(false, atoms);

        return new TokenizedPath(true, retvals);
    }
}
