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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import net.sf.json.JSONObject;

import org.kramerius.replications.SecondPhase.DONEController;

import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.utils.IOUtils;

public class ThirdPhase extends AbstractPhase {

    @Override
    public void start(String url, String userName, String pswd) throws PhaseException {
        try {
            String title = "_";
            IOUtils.cleanDirectory(new File(SecondPhase.DONE_FOLDER_NAME));
            String pid = K4ReplicationProcess.pidFrom(url);
            File descFile = getDescriptionFile();
            if ((descFile != null) && (descFile.canRead())) {
                String raw = IOUtils.readAsString(new FileInputStream(descFile), Charset.forName("UTF-8"), true);
                JSONObject jsonObject = JSONObject.fromObject(raw);
                title = jsonObject.getString("title");
            }
            IndexerProcessStarter.spawnIndexer(true, title, pid);
        } catch (FileNotFoundException e) {
            throw new PhaseException(this,e);
        } catch (IOException e) {
            throw new PhaseException(this,e);
        }
    }

    @Override
    public void restart(String previousProcessUUID, File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd) throws PhaseException {
        if (!phaseCompleted) {
            this.start(url, userName, pswd);
        }
    }
}
