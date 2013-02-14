/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.kramerius.statistics.database;

import static cz.incad.kramerius.database.cond.ConditionsInterpretHelper.versionCondition;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.security.database.InitSecurityDatabaseMethodInterceptor;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

/**
 * Statistic tables initialization
 * @author pavels
 */
public class StatisticDatabaseInitializator {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(StatisticDatabaseInitializator.class.getName());
    
    public static void initDatabase(Connection connection, VersionService versionService) {
        try {
            String version = versionService.getVersion();
            if (version == null) {
                createStatisticTables(connection);
                alterStatisticsTableStatAction(connection);
                createDatesDurationViews(connection);
            } else if (versionCondition(version, ">", "5.7.0") &&  versionCondition(version, "<", "6.0.0")){
                createStatisticTables(connection);
                alterStatisticsTableStatAction(connection);
                createDatesDurationViews(connection);
            } else if (versionCondition(version, ">=", "6.0.0")) {
                createDatesDurationViews(connection);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    /**
     * @param connection
     * @throws SQLException 
     */
    private static void createDatesDurationViews(Connection con) throws SQLException {
        List<JDBCCommand> commands = new ArrayList<JDBCCommand>();
        commands.add(new JDBCCommand() {
            
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                PreparedStatement prepareStatement = con.prepareStatement("create view flat_statistic_access_log_detail_map as  select record_id, min(detail_id) as detail_id from statistic_access_log_detail  group by record_id");
                int r = prepareStatement.executeUpdate();
                LOGGER.log(Level.FINEST, "CREATE VIEW: updated rows {0}", r);
                return null;
            }
        });
        commands.add(new JDBCCommand() {
            
            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                PreparedStatement prepareStatement = con.prepareStatement("create view flat_statistic_access_log_detail as  select fa.detail_id, fa.record_id, sa.pid, sa.model, sa.issued_date, sa.rights, sa.lang,sa.title  from flat_statistic_access_log_detail_map fa join statistic_access_log_detail sa using(detail_id)");
                int r = prepareStatement.executeUpdate();
                LOGGER.log(Level.FINEST, "CREATE VIEW: updated rows {0}", r);
                return null;
            }
        });

        new JDBCTransactionTemplate(con, true).updateWithTransaction(commands);
    }

    public static void alterStatisticsTableStatAction(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con.prepareStatement(
            "ALTER TABLE statistics_access_log ADD COLUMN STAT_ACTION VARCHAR(255);");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }    

    /**
     * @param connection
     * @throws IOException 
     * @throws SQLException 
     */
    private static void createStatisticTables(Connection connection) throws SQLException, IOException {
        InputStream is = StatisticDatabaseInitializator.class.getResourceAsStream("res/initstatisticsdb.sql");
        JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, false);
        template.setUseReturningKeys(false);
        template.executeUpdate(IOUtils.readAsString(is, Charset.forName("UTF-8"), true));
    }
}
