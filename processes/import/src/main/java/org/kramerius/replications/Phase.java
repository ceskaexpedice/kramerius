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

/**
 * K4 replication phase
 * @author pavels
 */
public interface Phase {

    /**
     * Start new phase
     * @param url Handle url
     * @param userName User name
     * @param pswd Password
     * @param replicationCollections TODO
     * @param replicateImages copy images to target kramerius (true|false)
     * @throws PhaseException
     */
    public void start(String url, String userName, String pswd, String replicationCollections, String replicateImages) throws PhaseException;
    
    /**
     * Restart previous phase
     * @param previousProcessUUID Previous process uuid
     * @param previousProcessRoot Previous process root
     * @param phaseCompleted TODO
     * @param url Handle url
     * @param userName User name
     * @param pswd Password
     * @param replicationCollections TODO
     * @throws PhaseException
     */
    public void restart(String previousProcessUUID,File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd,
                        String replicationCollections, String replicateImages) throws PhaseException;

}
