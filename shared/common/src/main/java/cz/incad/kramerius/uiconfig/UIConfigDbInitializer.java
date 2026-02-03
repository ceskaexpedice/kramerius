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
package cz.incad.kramerius.uiconfig;

import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initialize database ui config tables
 *
 * @author pavels
 */
public class UIConfigDbInitializer {

    static Logger LOGGER = Logger.getLogger(UIConfigDbInitializer.class.getName());

    /**
     * Database initialization
     *
     * @param connection DB connection
     */
    public static void initDatabase(final Connection connection) {
        try {
            boolean uiConfigTable = DatabaseUtils.tableExists(connection, "UI_CONFIG");
            if (!uiConfigTable) {
                createUIConfigTable(connection);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static void createUIConfigTable(Connection connection) throws IOException, SQLException {
        InputStream is = UIConfigDbInitializer.class.getResourceAsStream("res/inituiconfig.sql");
        String sql = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        int r = prepareStatement.executeUpdate();
        LOGGER.log(Level.FINEST, "CREATE TABLE ui_config: updated rows {0}", r);
    }

}
