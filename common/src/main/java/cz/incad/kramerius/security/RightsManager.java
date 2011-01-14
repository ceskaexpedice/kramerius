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

public interface RightsManager {

    public Right[] findRights(String[] uuids, String action, User user);

    public EvaluatingResult resolve(RightCriteriumContext ctx, String uuid, String[] path, String action, User user) throws RightCriteriumException;

    // DAO methods - DAt to jinam !!

    public RightCriteriumParams[] findAllParams();

    public RightCriteriumParams findParamById(int paramId);

    public RightCriterium findRightCriteriumById(int critId);
    
    public List<String> saturatePathAndCreatesPIDs(String uuid, String[] path);

    public int insertRight(Right right) throws SQLException;

    public void updateRight(Right right) throws SQLException;

    public int insertRightCriterium(RightCriterium criterium) throws SQLException;

    public void updateRightCriterium(RightCriterium criterium) throws SQLException;

    public int insertRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException;

    public void updateRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException;

    public void deleteRight(Right right) throws SQLException;
    
    public Right findRightById(int id);
}
