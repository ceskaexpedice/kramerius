/*
 * Copyright (C) 2025  Inovatika
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
package cz.inovatika.cdk.cache;

import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes the database tables required for caching CDK queries.
 * This class ensures that the necessary tables exist and applies schema modifications if needed.
 */
public class CDKCacheInitializer {


    static Logger LOGGER = Logger.getLogger(CDKCacheInitializer.class.getName());

    /**
     * Initializes the database for caching if the required table does not exist.
     * Also, if the "pid" column has a NOT NULL constraint, it removes the constraint.
     *
     * @param connection The database connection used for checking and initializing the cache.
     */
    public static void initDatabase(Connection connection) {
        try {
            // Check if the cache table exists, and create it if necessary
            if (!DatabaseUtils.tableExists(connection, "QUERY_CACHE")) {
                createCache(connection);
            }

             // Check if the table exists and if the "pid" column has a NOT NULL constraint, then drop it
            if (DatabaseUtils.tableExists(connection, "QUERY_CACHE") && DatabaseUtils.isColumnNotNull(connection, "query_cache","pid")) {
                //drop not null constraint
                dropPidNotNull(connection);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }


    /**
     * Creates the cache table in the database by executing the SQL initialization script.
     *
     * @param connection The database connection used for executing the script.
     * @throws SQLException If a database error occurs while creating the table.
     * @throws IOException  If an error occurs while reading the SQL script.
     */
    private static void createCache(Connection connection) throws SQLException, IOException {
        InputStream is = CDKCacheInitializer.class.getResourceAsStream("res/init_db.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        String sqlScript = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        template.executeUpdate(sqlScript);
    }

    /**
     * Removes the NOT NULL constraint from the "pid" column in the cache table.
     * This is necessary for cases where "pid" may not always be present.
     *
     * @param connection The database connection used for modifying the table schema.
     * @throws SQLException If a database error occurs while altering the table.
     * @throws IOException  If an error occurs while reading the SQL script.
     */
    private static void dropPidNotNull(Connection connection) throws SQLException, IOException {
        InputStream is = CDKCacheInitializer.class.getResourceAsStream("res/drop_not_null_from_pidcolumn.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        String sqlScript = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        template.executeUpdate(sqlScript);
    }

}
