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
package cz.incad.kramerius.security;

import cz.incad.kramerius.ObjectPidsPath;

/**
 * Rights resolver.   
 */
public interface IsActionAllowed extends IsActionAllowedBase {

	/**
	 * Returns true if current logged user is permitted to do action defined by first parameter (actionName) for object defined by second and third parameter (uuid and pathOfUuuid)
	 * @param actionName Formal action's name
	 * @param pid Pid of requested object
	 * @param stream Stream of requested object or null
	 * @param path Path of object. (from root to leaf)
	 * @return
	 */
    public boolean isActionAllowed(String actionName, String pid, String stream, ObjectPidsPath path);

	/**
	 * Returns true given user is permitted to do action defined by parameter actionName for object defined by parameters uuid and pathOfUuuid
	 * @param user User
	 * @param actionName Formal action's name
	 * @param pid PID of requested object
	 * @param stream of requested object or null
	 * @param path Path of object. (from root to leaf)
	 * @return
	 */
    public boolean isActionAllowed(User user, String actionName, String pid, String stream, ObjectPidsPath path);

    
    /**
     * Returns array of results for all path (from root to leaf) for current logged user.    
     * @param actionName Action name
     * @param pid PID of requesting object
     * @param stream Requested stream or null
     * @param path Path from root to leaf 
     * @return
     */
    public boolean[] isActionAllowedForAllPath(String actionName, String pid, String stream, ObjectPidsPath path);
    
}
