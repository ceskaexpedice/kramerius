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
 * 
 * @author pavels
 */
public class StatisticDatabaseInitializator {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(StatisticDatabaseInitializator.class.getName());

    public static void initDatabase(Connection connection, VersionService versionService) {
        try {
            String version = versionService.getVersion();
            if (version == null) {
                createStatisticTables(connection);
                alterStatisticsTableStatAction(connection);
                createDatesDurationViews(connection);
                alterStatisticsTableSessionId(connection);
                // Issue 619
                alterStatisticsAuthorTablePrimaryKey(connection);
                createFirstFunction(connection);
                createLastFunction(connection);
                createAuthorsView(connection);
                createLangsView(connection);
            } else if (versionCondition(version, "<", "6.0.0")) {
                createStatisticTables(connection);
                alterStatisticsTableStatAction(connection);
                createDatesDurationViews(connection);
                alterStatisticsTableSessionId(connection);

                // Issue 619
                alterStatisticsAuthorTablePrimaryKey(connection);
                createFirstFunction(connection);
                createLastFunction(connection);
                createAuthorsView(connection);
                createLangsView(connection);
            } else if (versionCondition(version, "=", "6.0.0")) {
                alterStatisticsTableStatAction(connection);
                createDatesDurationViews(connection);
                alterStatisticsTableSessionId(connection);

                // Issue 619
                alterStatisticsAuthorTablePrimaryKey(connection);
                createFirstFunction(connection);
                createLastFunction(connection);
                createAuthorsView(connection);
                createLangsView(connection);
            } else if (versionCondition(version, "=", "6.1.0")) {
                alterStatisticsTableSessionId(connection);

                // Issue 619
                alterStatisticsAuthorTablePrimaryKey(connection);
                createFirstFunction(connection);
                createLastFunction(connection);
                createAuthorsView(connection);
                createLangsView(connection);
            } else if ((versionCondition(version, ">", "6.1.0")) && (versionCondition(version, "<", "6.5.0"))) {
                // Issue 619
                alterStatisticsAuthorTablePrimaryKey(connection);
                createFirstFunction(connection);
                createLastFunction(connection);
                createAuthorsView(connection);
                createLangsView(connection);
            } else if (versionCondition(version, ">=", "6.5.0")&& (versionCondition(version, "<", "6.6.4"))) {
                createFirstFunction(connection);
                createLastFunction(connection);
                createAuthorsView(connection);
                createLangsView(connection);
            } else if ((versionCondition(version, ">=", "6.6.4")) && (versionCondition(version, "<", "6.6.5"))) {
                createAuthorsView(connection);
                createLangsView(connection);
            } else if (versionCondition(version, ">=", "6.6.5")) {
                createLangsView(connection);
                createAuthorsView(connection);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
                PreparedStatement prepareStatement = con.prepareStatement(
                        "create view flat_statistic_access_log_detail_map as  select record_id, min(detail_id) as detail_id from statistic_access_log_detail  group by record_id");
                int r = prepareStatement.executeUpdate();
                LOGGER.log(Level.FINEST, "CREATE VIEW: updated rows {0}", r);
                return null;
            }
        });
        commands.add(new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                PreparedStatement prepareStatement = con.prepareStatement(
                        "create view flat_statistic_access_log_detail as  select fa.detail_id, fa.record_id, sa.pid, sa.model, sa.issued_date, sa.rights, sa.lang,sa.title  from flat_statistic_access_log_detail_map fa join statistic_access_log_detail sa using(detail_id)");
                int r = prepareStatement.executeUpdate();
                LOGGER.log(Level.FINEST, "CREATE VIEW: updated rows {0}", r);
                return null;
            }
        });

        new JDBCTransactionTemplate(con, false).updateWithTransaction(commands);
    }

    public static void alterStatisticsTableStatAction(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con
                .prepareStatement("ALTER TABLE statistics_access_log ADD COLUMN STAT_ACTION VARCHAR(255);");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    public static void alterStatisticsTableSessionId(Connection con) throws SQLException {
        PreparedStatement prepareStatement = con
                .prepareStatement("ALTER TABLE statistics_access_log ADD COLUMN SESSION_ID VARCHAR(255);");
        try {
            int r = prepareStatement.executeUpdate();
            LOGGER.log(Level.FINEST, "ALTER TABLE: updated rows {0}", r);
        } finally {
            DatabaseUtils.tryClose(prepareStatement);
        }
    }

    // change pk
    // Issue 619
    public static void alterStatisticsAuthorTablePrimaryKey(final Connection con) throws SQLException {

        JDBCCommand deletePKCommand = new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(con, false);
                template.setUseReturningKeys(false);
                template.executeUpdate(
                        "ALTER TABLE statistic_access_log_detail_authors DROP CONSTRAINT statistic_access_log_detail_authors_pkey",
                        new Object[0]);
                return null;
            }
        };

        JDBCCommand createPKCommand = new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(con, false);
                template.setUseReturningKeys(false);
                template.executeUpdate("ALTER TABLE statistic_access_log_detail_authors ADD  PRIMARY KEY (author_id)",
                        new Object[0]);
                return null;
            }
        };

        new JDBCTransactionTemplate(con, false).updateWithTransaction(deletePKCommand, createPKCommand);

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
    
    
    /**
     * Create first aggregation function first(col)
     */
    public static void createFirstFunction(Connection connection) throws SQLException, IOException {
        JDBCCommand firstAgg = new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(con, false);
                template.setUseReturningKeys(false);
                template.executeUpdate(
                    "CREATE OR REPLACE FUNCTION public.first_agg ( anyelement, anyelement )"+
                        "RETURNS anyelement LANGUAGE SQL IMMUTABLE STRICT AS $$"+
                        "        SELECT $1;"+
                        "$$;",new Object[0]);
                return null;
            }
        };

        JDBCCommand first = new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(con, false);
                template.setUseReturningKeys(false);
                template.executeUpdate(""
                        + " CREATE AGGREGATE public.FIRST ("
                        +"sfunc    = public.first_agg,"
                        +"basetype = anyelement,"
                        +"stype    = anyelement);",
                        new Object[0]);
                return null;
            }
        };

        new JDBCTransactionTemplate(connection, false).updateWithTransaction(firstAgg, first);
    }

    /**
     * Create last aggregation function first(col)
     */
    public static void createLastFunction(Connection connection) throws SQLException, IOException {
        JDBCCommand lastAgg = new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(con, false);
                template.setUseReturningKeys(false);
                template.executeUpdate(
                        "CREATE OR REPLACE FUNCTION public.last_agg ( anyelement, anyelement )"+
                                "RETURNS anyelement LANGUAGE SQL IMMUTABLE STRICT AS $$"+
                                "        SELECT $2;"+
                                "$$;",new Object[0]);
                return null;
            }
        };

        JDBCCommand last = new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(con, false);
                template.setUseReturningKeys(false);
                template.executeUpdate("CREATE AGGREGATE public.LAST ("
                                +"        sfunc    = public.last_agg,"
                                +"        basetype = anyelement,"
                                +"        stype    = anyelement"
                                +");",
                        new Object[0]);
                return null;
            }
        };

        new JDBCTransactionTemplate(connection, false).updateWithTransaction(lastAgg, last);
    }


    public static void createAuthorsView(Connection connection) throws SQLException, IOException {
        JDBCCommand authorsView = new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(con, false);
                template.setUseReturningKeys(false);
                template.executeUpdate(
                        "CREATE or REPLACE VIEW _authors_view as "+
                        "select last(record_id) as record_id, "+
                        "last(author_id) as author_id, "+
                        "last(author_name) as author_name, "+ 
                        "last(dta.pid) as pid, "+
                        "last(model) as model, "+
                        "last(session_id) as session_id, "+
                        "last(date) as \"date\", "+
                        "last(rights) as rights, "+
                        "last(stat_action) as stat_action, "+
                        "last(remote_ip_address) as remote_ip_address "+
                        "from statistic_access_log_detail_authors auth "+
                        "join statistics_access_log sta using(record_id) "+
                        "join statistic_access_log_detail dta using(record_id) "+
                        "group by record_id "+
                                "",new Object[0]);
                return null;
            }
        };

        new JDBCTransactionTemplate(connection, false).updateWithTransaction(authorsView);
    }

    public static void createLangsView(Connection connection) throws SQLException, IOException {
        JDBCCommand langsView = new JDBCCommand() {

            @Override
            public Object executeJDBCCommand(Connection con) throws SQLException {
                JDBCUpdateTemplate template = new JDBCUpdateTemplate(con, false);
                template.setUseReturningKeys(false);
                template.executeUpdate(
                        "CREATE or REPLACE VIEW _langs_view as "+
                        "SELECT  "+
                        "last(dta.pid) AS pid, "+
                        "last(dta.model) AS model, "+
                        "last(sta.session_id) AS session_id, "+
                        "last(sta.date) AS date, "+
                        "last(dta.rights) AS rights, "+
                        "last(sta.stat_action) AS stat_action, "+
                        "last(dta.lang) as lang, "+
                        "last(remote_ip_address) as remote_ip_address "+
                       "FROM statistics_access_log sta "+
                         "JOIN statistic_access_log_detail dta USING (record_id) "+
                      "GROUP BY sta.record_id;"+
                                "",new Object[0]);
                return null;
            }
        };

        new JDBCTransactionTemplate(connection, false).updateWithTransaction(langsView);
    }
}
