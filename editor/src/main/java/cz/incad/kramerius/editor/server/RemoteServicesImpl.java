/*
 * Copyright (C) 2010 Jan Pokorsky
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

package cz.incad.kramerius.editor.server;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.relation.RelationUtils;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.utils.DCUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

/**
 *
 * @author Jan Pokorsky
 */
public final class RemoteServicesImpl implements RemoteServices {

    private FedoraAccess fedoraAccess;
//    private DefinitionManager definitionManager;
    
    
    
    @Inject
    public RemoteServicesImpl(@Named("rawFedoraAccess") FedoraAccess fedoraAccess) {
        this.fedoraAccess = fedoraAccess;
    }

    @Override
    public void reindex(String pid) {
        //String uuid = EditorServerUtils.resolveUUID(pid);
        if (pid == null) {
            return;
        }
        String title;
        try {
            title = fetchDCName(pid);
        } catch (IOException ex) {
            Logger.getLogger(RemoteServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
            title = pid;
        }

        reindex(pid, title);
    }

    void reindex(String uuid, String title) {
    	// reindexace je nyni pres ajaxcall primo do k4
    	//IndexerProcessStarter.spawnIndexer(true, title, uuid);
    }

    
    
    @Override
    public String fetchDCName(String pid) throws IOException {
        Document dc = RelationUtils.getDC(pid, fedoraAccess);
        return DCUtils.titleFromDC(dc);
    }

}
