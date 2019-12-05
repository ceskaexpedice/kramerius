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
package cz.incad.kramerius.database;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Database model version service 
 * @author pavels
 */
public interface VersionService {
    
    /**
     * Returns current database model version
     * @return Database model version
     * @throws SQLException SQL error has been occurred
     */
    public String getVersion() throws SQLException;

    /**
     * Update database model version
     * @throws IOException IO error has been occured
     * @throws SQLException SQL error has been occurred
     */
    public void updateNewVersion() throws IOException, SQLException;
}
