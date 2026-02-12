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

import cz.incad.kramerius.security.licenses.lock.ExclusiveReadersLock;
import cz.incad.kramerius.security.licenses.lock.ExclusiveReadersLock.ExclusiveLockType;
import org.w3c.dom.Document;

/**
 * Represents a license object.
 * A license can define access permissions for documents either through indexed metadata
 * or dynamically (runtime). Licenses may have priorities and exclusive locks for special access rules.
 *
 * @author happy
 */
public interface License {

    /**
     * Pattern that defines the allowed format of a license name.
     * The name must start with a letter and can include letters, digits, underscores, dashes, slashes, or colons.
     */
    public static final Pattern ACCEPTABLE_LABEL_NAME_REGEXP= Pattern.compile("[a-zA-Z][a-zA-Z_0-9-/:]+");


    /**
     * Default priority value assigned to newly created licenses.
     * Higher values may be used to determine which license takes precedence.
     */
    public static int DEFAULT_PRIORITY = 1;

    /**
     * Returns a unique identifier for the license.
     *
     * @return license ID
     */
    public int getId();

    /**
     * Returns the name of the license.
     * If the license is local, the name usually has a library-specific prefix
     * (e.g., knav_vip, nkp_local, etc.).
     *
     * @return license name
     */
    public String getName();

    /**
     * Returns a textual description of the license.
     *
     * @return license description
     */
    public String getDescription();

    /**
     * Returns the license group.
     * This is deprecated and will be removed in future versions.
     *
     * @return license group
     */
    public String getGroup();

    /**
     * Returns the priority of the license.
     * Priority is important when a document could be evaluated under multiple licenses.
     * The license with the higher priority takes precedence.
     *
     * @return license priority
     */
    public int getPriority();
    
    
    // hint for priority rearragement 
    public int getPriorityHint();

    public boolean isOfflineGenerateContentAllowed();

    public void setOfflineGenerateContentAllowed(boolean flag);

    /**
     * Updating priority of license 
     * @param priprity
     * @return
     */
    public License getUpdatedPriorityLabel(int priprity);

    /**
     * Indicates whether the license has an exclusive lock configured.
     *
     * @return true if an exclusive lock is present; false otherwise
     */
    public boolean exclusiveLockPresent();

    /**
     * Returns the exclusive lock associated with the license.
     *
     * @return exclusive lock
     */
    public ExclusiveReadersLock getExclusiveLock();

    /**
     * Initializes an exclusive lock on the license with the specified parameters.
     *
     * @param refresh  refresh interval
     * @param max      maximum number of readers
     * @param readers  number of allowed readers
     * @param type     type of the exclusive lock
     */
    public void initExclusiveLock(int refresh, int max, int readers, ExclusiveLockType type);

    /**
     * Deletes the exclusive lock from the license.
     */
    public void deleteExclusiveLock();


    /**
     * Initializes the license as a runtime license with the specified type.
     *
     * @param type the runtime license type
     */
    public void initRuntime(RuntimeLicenseType type);

    /**
     * Disables or enables the runtime mode of the license.
     *
     * @param flag true to disable runtime, false to enable
     */
    public void disableRuntime(boolean flag);


    /**
     * Returns the type of the runtime license.
     * The type defines the filter used to determine which documents match the license.
     *
     * @return runtime license type
     */
    public RuntimeLicenseType getRuntimeLicenseType();

    /**
     * Returns whether the license is a runtime license.
     *
     * @return true if the license is runtime; false otherwise
     */
    public boolean isRuntimeLicense();


    /**
     * Evaluates whether a given document matches this license.
     * This is used to determine access eligibility based on the document metadata.
     *
     * @param doc the document to evaluate
     * @return true if the document is accepted by this license; false otherwise
     */
    public boolean acceptByLicense(Document doc);
}
