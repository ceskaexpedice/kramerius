/*
 * Copyright (C) Feb 26, 2024 Pavel Stastny
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
package cz.incad.kramerius.security.licenses.lock;

import java.util.Iterator;

import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.licenses.License;

/**
 * This interface defines methods for an exclusive lock mechanism.
 * This lock serves to protect access to licensed content. Only a defined number of users can consume the license at any given time.
 */
public interface ExclusiveLock {
    
    /**
     * An enum representing the type of exclusive lock.
     */
    public enum ExclusiveLockType {
        /**
         * Exclusive lock for single use within a rule.
         */
        RULE,
        
        /**
         * Exclusive lock for the entire instance.
         */
        INSTANCE; 
        
        public static ExclusiveLockType findByType(String t) {
            for (ExclusiveLockType et : values()) {
                if (et.name().equals(t)) {
                    return et;
                }
            }
            return null;
        }
    }
    
    public int getRefreshInterval();
    
    public int getMaxInterval();
    
    public int getMaxReaders();
    
    public ExclusiveLockType getType();

    public String createLockHash(License license, Right right, String pid);
}
