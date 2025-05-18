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

    /**
     * Returns the refresh interval in seconds that the client must maintain
     * by sending periodic refresh requests in order to retain access.
     *
     * @return refresh interval in seconds
     */
    public int getRefreshInterval();

    /**
     * Returns the maximum interval in seconds that a user is allowed
     * to continuously read the content under the lock.
     *
     * @return maximum reading duration in seconds
     */
    public int getMaxInterval();

    /**
     * Returns the maximum number of users (readers) allowed to access
     * the licensed content concurrently.
     *
     * @return maximum number of concurrent readers
     */
    public int getMaxReaders();

    /**
     * Returns the type of the exclusive lock.
     * The type defines how the reader counter is maintained:
     * <ul>
     *     <li>{@code RULE} - separate counter for each rule using the lock</li>
     *     <li>{@code INSTANCE} - single shared counter for the entire Kramerius instance</li>
     * </ul>
     *
     * @return type of the lock
     */
    public ExclusiveLockType getType();

    /**
     * Creates a hash code used to identify the lock instance.
     * This hash is used for lookups and uniquely represents the lock based on
     * the license, user right, and document PID.
     *
     * @param license the license associated with the lock
     * @param right the right being evaluated
     * @param pid the PID of the document
     * @return a unique hash string representing the lock instance
     */
    public String createLockHash(License license, Right right, String pid);
}
