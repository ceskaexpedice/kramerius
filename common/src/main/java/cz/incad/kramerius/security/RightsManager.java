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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import cz.incad.kramerius.ObjectPidsPath;

/** 
 * Rights management
 * @author pavels
 */
public interface RightsManager {
    
    // najde prava pro uzivatele
    /**
     * Find rights associated with given pids, actions and given user
     * @param pids Objects' pids
     * @param action Secured action
     * @param user User
     * @return Found rights
     */
    public Right[] findRights(String[] pids, String action, User user);

    /**
     * Finds all rights stored in database
     * @return
     */
    public Right[] findRights(String[] ids, String[] pids, String[] actions, String[] roleNames);

    
    // interpretuje prava
    /**
     * Right interpretation over given object (pid)
     * @param ctx Runtime interpration context
     * @param pid Object's pid
     * @param path Object's path
     * @param action Secured action
     * @param user User
     * @return Returns result of interpretation
     */
    public EvaluatingResult resolve(RightCriteriumContext ctx, String pid, ObjectPidsPath path, String action, User user) throws RightCriteriumException;
    
    // interpretuje prava skrz celou cestu.  Od listu az ke korenu.
    /**
     * Right interpretation over given object(pid) 
     * @param ctx Interpreation context
     * @param pid Object's pid
     * @param path Object's path
     * @param action Secured action
     * @param user User
     * @return Returns all results for all objects in given path
     */
    public EvaluatingResult[] resolveAllPath(RightCriteriumContext ctx, String pid, ObjectPidsPath path, String action, User user) throws RightCriteriumException;

    
    // najde prava pro skupinu
    /**
     * Find all rights associated with given pids, secured action and given role
     * @param pids Object's pid
     * @param action Secured action
     * @param role Role
     * @return Returns found roles
     */
    public Right[] findRightsForGroup(final String[] pids, final String action, final Role role);

    // najde vsechna prava
    /**
     * Find all rights associated with given pids and secured action
     * @param pids Object's pid
     * @param action Secured action
     */
    public Right[] findAllRights(String[] pids, String action);

    
    // DAO methods - DAt to jinam !!
    // najde vsechny parametry 
    /**
     * Find all params
     */
    public RightCriteriumParams[] findAllParams();

    
    /**
     * Find one param by given id
     * @param paramId Param id
     * @return Found criterium
     */
    public RightCriteriumParams findParamById(int paramId);

    /**
     * Find criterium by given id 
     * @param critId Criterium id
     * @return found criterium
     */
    public RightCriteriumWrapper findRightCriteriumById(int critId);
    
        
    public List<String> saturatePathAndCreatesPIDs(String uuid, String[] path);

    /**
     * Insert new right into db
     * @param right New right
     * @return Returns id of new created right
     * @throws SQLException SQL error has been occurred
     */
    public int insertRight(Right right) throws SQLException;

    /**
     * Update existing right
     * @param right Right which to be updated
     * @throws SQLException SQL error has been occurred
     */
    public void updateRight(Right right) throws SQLException;

    
    /**
     * Creates new criterium in db
     * @param criterium New criterium
     * @return Returns id of new created criterium
     * @throws SQLException SQL error has been occurred
     */
    public int insertRightCriterium(RightCriteriumWrapper criterium) throws SQLException;

    /**
     * Update right criterium in db
     * @param criterium Updated criterium
     * @throws SQLException SQL error has been occurred
     */
    public void updateRightCriterium(RightCriteriumWrapper criterium) throws SQLException;

    /**
     * Creates new parameters
     * @param criteriumParams New inserted params
     * @return Returns new created id
     * @throws SQLException SQL error has been occurred
     */
    public int insertRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException;

    /**
     * Delete parameters from db
     * @param id Params identifier
     * @throws SQLException SQL error has been occurred
     */
    public void deleteRightCriteriumParams(int id) throws SQLException;
    
    
    /**
     * Delete criterium from db
     * @param id Criterium identifier
     * @throws SQLException SQL error has been occurred
     */
    public void deleteRightCriterium(int id) throws SQLException;
    
    /**
     * Updates criterium params
     * @param criteriumParams Updating criteriums
     * @throws SQLException SQL error has been occurred
     */
    public void updateRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException;

    /**
     * Delete rights
     * @param right Deleting rights
     * @throws SQLException SQL error has been occurred
     */
    public void deleteRight(Right right) throws SQLException;
    
    
    /**
     * Find right by given id
     * @param id Right identifier
     * @return found right
     */
    public Right findRightById(int id);
    
    
    /**
     * Find all used role identifiers
     * @return role identifiers
     */
    public int[] findUsedRoleIDs();
    
    
    /**
     * Find all pids which using given params (identified by prams identifier)
     * @param paramId Param identifier
     * @return Map that contains associtaion PID -> SecuredAction
     */
    public List<Map<String,String>> findObjectUsingParams(int paramId);
}
