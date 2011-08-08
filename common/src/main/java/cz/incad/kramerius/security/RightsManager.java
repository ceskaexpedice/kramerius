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

import cz.incad.kramerius.ObjectPidsPath;

public interface RightsManager {
    
    // najde prava pro uzivatele
    public Right[] findRights(String[] uuids, String action, User user);

    // interpretuje prava
    public EvaluatingResult resolve(RightCriteriumContext ctx, String uuid, ObjectPidsPath path, String action, User user) throws RightCriteriumException;
    
    // interpretuje prava skrz celou cestu.  Od listu az ke korenu.
    public EvaluatingResult[] resolveAllPath(RightCriteriumContext ctx, String uuid, ObjectPidsPath path, String action, User user) throws RightCriteriumException;

    
    // najde prava pro skupinu
    public Right[] findRightsForGroup(final String[] pids, final String action, final Role group);
    // najde vsechna prava
    public Right[] findAllRights(String[] uuids, String action);

    
    // DAO methods - DAt to jinam !!
    // najde vsechny parametry 
    public RightCriteriumParams[] findAllParams();

    public RightCriteriumParams findParamById(int paramId);

    public RightCriteriumWrapper findRightCriteriumById(int critId);
    
    public List<String> saturatePathAndCreatesPIDs(String uuid, String[] path);

    public int insertRight(Right right) throws SQLException;

    public void updateRight(Right right) throws SQLException;

    public int insertRightCriterium(RightCriteriumWrapper criterium) throws SQLException;

    public void updateRightCriterium(RightCriteriumWrapper criterium) throws SQLException;

    public int insertRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException;

    public void updateRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException;

    public void deleteRight(Right right) throws SQLException;
    
    public Right findRightById(int id);
    
    public int[] findUsedRoleIDs();
}
