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

import java.sql.Connection;
import java.util.List;

/**
 * License allows basic manipulation with licenses
 * @author happy
 */
public interface LicensesManager {

    
    
    /** Local license **/
    public static final String LOCAL_GROUP_NAME="local";

    
    /** Global license **/
    public static final String GLOBAL_GROUP_NAME_IMPORTED ="imported";
    public static final String GLOBAL_GROUP_NAME_EMBEDDED ="embedded";

    
    /**
     * Adds a new local license object
     * @param license License object
     * @throws LicensesManagerException Cannot add new license object
     */
    public void addLocalLicense(License license) throws LicensesManagerException;
    
    /**
     * Removes the license 
     * @param license License object
     * @throws LicensesManagerException Cannot remove the license object
     */
    public void removeLocalLicense(License license) throws LicensesManagerException;

    /**
     * Returns minimum priority of stored licenses
     * @return
     * @throws LicensesManagerException
     */
    public int getMinPriority() throws LicensesManagerException;
    
    /**
     * Returns maximum minim priority of stored licenses
     * @return
     * @throws LicensesManagerException Cannot return the priority
     */
    public int getMaxPriority() throws LicensesManagerException;
    
    /**
     * Finds the license corresponding to the given priority
     * @param priority
     * @return
     * @throws LicensesManagerException Cannot return the priority
     */
    public License getLicenseByPriority(int priority) throws LicensesManagerException;
    
    
    
    
    /**
     * Finds the licenses by given id 
     * @param id
     * @return
     * @throws LicensesManagerException Cannot find or return license object
     */
    public License getLicenseById(int id) throws LicensesManagerException;
    
    /**
     * Finds the licens by given name 
     * @param name
     * @return
     * @throws LicensesManagerException Cannot find or return license object
     */
    public License getLicenseByName(String name) throws LicensesManagerException;

    /**
     * Returns all stored licenses
     * @return
     * @throws LicensesManagerException
     */
    public List<License> getLicenses() throws LicensesManagerException;

    /**
     * Returns all global licenses
     * @return
     * @throws LicensesManagerException
     */
    public List<License> getGlobalLicenses() throws LicensesManagerException;
    
    /**
     * Returns only local liceses
     * @return
     * @throws LicensesManagerException
     */
    public List<License> getLocalLicenses() throws LicensesManagerException;
    
    /**
     * Returns only local liceses
     * @return
     * @throws LicensesManagerException
     */
    public List<License> getAllLicenses() throws LicensesManagerException;

    
    /**
     * Update local license
     * @param license License object
     * @throws LicensesManagerException Cannot update license
     */
    public void updateLocalLicense(License license) throws LicensesManagerException;
    
    /**
     * Change priority of the license; Increase priority
     * @param license
     * @throws LicensesManagerException
     */
    public void moveUp(License license) throws LicensesManagerException;
    
    /**
     * Change priority of the licensel; Decrease priority
     * @param license
     * @throws LicensesManagerException
     */
    public void moveDown(License license) throws LicensesManagerException;
    
    
    /**
     * Goes through and refresh the licenses used in solr  
     * @throws LicensesManagerException
     */
    public void refreshLabelsFromSolr() throws LicensesManagerException;
    
    
    
    /** 
     * Rearrange global priorities
     * @param globalLicenses
     */
    public void rearrangePriorities(List<License> globalLicenses) throws LicensesManagerException;

    /** 
     * TODO: 
     * Rearrange global priorities
     * @param globalLicenses
     */
    public void rearrangePriorities(List<License> globalLicenses, Connection conn) throws LicensesManagerException;
    
    
}
