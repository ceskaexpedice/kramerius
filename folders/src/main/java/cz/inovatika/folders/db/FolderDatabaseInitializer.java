/*
 * Copyright (C) Sep 4, 2023 Pavel Stastny
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
package cz.inovatika.folders.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.processes.database.ProcessDbInitializer;
import cz.incad.kramerius.statistics.database.StatisticDbInitializer;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class FolderDatabaseInitializer {


    static Logger LOGGER = Logger.getLogger(FolderDatabaseInitializer.class.getName());

    public static void initDatabase(Connection connection, VersionService versionService) {
        try {
            if (!DatabaseUtils.tableExists(connection, "FOLDER")) {
                createFolders(connection);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    private static void createFolders(Connection connection) throws SQLException, IOException {
        InputStream is = FolderDatabaseInitializer.class.getClassLoader().getResourceAsStream("/init_db.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        String sqlScript = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        template.executeUpdate(sqlScript);
    }
}
