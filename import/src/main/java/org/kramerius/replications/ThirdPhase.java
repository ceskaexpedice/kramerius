/*
 * Copyright (C) 2012 Pavel Stastny
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
package org.kramerius.replications;

import java.io.File;

import org.kramerius.replications.SecondPhase.DONEController;

import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.utils.IOUtils;

public class ThirdPhase extends AbstractPhase {

    @Override
    public void start(String url, String userName, String pswd) throws PhaseException {
        IOUtils.cleanDirectory(new File(SecondPhase.DONE_FOLDER_NAME));
        String pid = K4ReplicationProcess.pidFrom(url);
        IndexerProcessStarter.spawnIndexer(true, "_", pid);
    }

    @Override
    public void restart(String previousProcessUUID, File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd) throws PhaseException {
        if (!phaseCompleted) {
            this.start(url, userName, pswd);
        }
    }
}
