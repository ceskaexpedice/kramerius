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
package cz.incad.kramerius.processes;

import java.util.List;

/**
 * Scheduler finds all killed process
 * @author pavels
 */
public interface GCScheduler {
    
    /**
     * Initialization
     */
	public void init();

	/**
	 * Schedule finding all process which are propably not running
	 */
	void scheduleFindGCCandidates();
	
	/**
	 * Schedule checking and changing states of the processes
	 * @param procUuids
	 */
	void scheduleCheckFoundGCCandidates(List<String> procUuids);
	
	/**
	 * Shutdown gc scheduler
	 */
	public void shutdown();

}
