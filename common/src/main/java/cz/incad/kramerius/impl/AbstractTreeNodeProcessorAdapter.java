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
package cz.incad.kramerius.impl;

import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Adapter class for receving informations about processing uuids.  
 * @author pavels
 */
public abstract class AbstractTreeNodeProcessorAdapter implements TreeNodeProcessor {

    @Override
    public void process(String pid) {
        String pageUuid = null;
        try {
            if (pid.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
                PIDParser pidParse = new PIDParser(pid);
                pidParse.disseminationURI();
                pageUuid = pidParse.getObjectId();
            } else {
                PIDParser pidParse = new PIDParser(pid);
                pidParse.objectPid();
                pageUuid = pidParse.getObjectId();
            }
            
            processUuid(pageUuid);
        } catch (LexerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Processing one uuid
     * @param uuid UUID of processing object
     */
    public abstract void processUuid(String pageUuid);
}
