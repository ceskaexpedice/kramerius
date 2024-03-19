/*
 * Copyright (C) Jun 8, 2023 Pavel Stastny
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
package cz.incad.kramerius.security.licenses;

import java.util.regex.Pattern;

import cz.incad.kramerius.security.licenses.lock.ExclusiveLock;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock.ExclusiveLockType;

/**
 * Represents a license object
 * @author happy
 */
public interface License {

    /**
     * License name must match following pattern 
     */
    public static final Pattern ACCEPTABLE_LABEL_NAME_REGEXP= Pattern.compile("[a-zA-Z][a-zA-Z_0-9-/:]+");

    /**
     * Default license priority
     */
    public static int DEFAULT_PRIORITY = 1;
    
    /**
     * Returns a unique license identifier
     * @return
     */
    public int getId();

    /**
     * Returns the name of the license
     * @return
     */
    public String getName();

    /**
     * Basic the description of the license
     * @return
     */
    public String getDescription();

    /**
     * Returns the group of license
     * @see LicensesManager#GLOBAL_GROUP_NAME
     * @see LicensesManager#LOCAL_GROUP_NAME
     * @return
     */
    public String getGroup();

    /**
     * Returns the priority of license
     * @return
     */
    public int getPriority();
    
    
    // hint for priority rearragement 
    public int getPriorityHint();

    /**
     * Updating priority of license 
     * @param priprity
     * @return
     */
    public License getUpdatedPriorityLabel(int priprity);
    
    public boolean exclusiveLockPresent();

    public ExclusiveLock getExclusiveLock();
    
    public void initExclusiveLock(int refresh, int max, int readers, ExclusiveLockType type);
        
    public void deleteExclusiveLock();
    
//    public boolean exclusiveLockPresent();
//    
//    public int getExclusiveLockRefreshInterval();
//    
//    public int getExclusiveLockMaxInterval();

}
