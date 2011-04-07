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

import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Adapter class for receving informations about processing uuids.  
 * @author pavels
 */
public abstract class AbstractTreeNodeProcessorAdapter implements TreeNodeProcessor {

    @Override
    public void process(String pid, int level) throws ProcessSubtreeException {
        String pageUuid = null;
        try {
            pageUuid = ensureUUID(pid);
            processUuid(pageUuid, level);
        } catch (LexerException e) {
            throw new RuntimeException(e);
        }
    }



    public String ensureUUID(String pid) throws LexerException {
        String pageUuid;
        if (pid.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
            PIDParser pidParse = new PIDParser(pid);
            pidParse.disseminationURI();
            pageUuid = pidParse.getObjectId();
        } else {
            PIDParser pidParse = new PIDParser(pid);
            pidParse.objectPid();
            pageUuid = pidParse.getObjectId();
        }
        return pageUuid;
    }
    
    

    @Override
    public boolean breakProcessing(String pid, int level) {
        return false;
    }



    /**
     * Processing one uuid
     * @param level TODO
     * @param uuid UUID of processing object
     * @throws ProcessSubtreeException 
     */
    public abstract void processUuid(String pageUuid, int level) throws ProcessSubtreeException;
}
