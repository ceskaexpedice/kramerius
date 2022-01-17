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
package cz.incad.kramerius.database.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default version service implementation
 *
 * @author pavels
 */
public class VersionServiceImpl implements VersionService {

    private static Logger LOGGER = java.util.logging.Logger.getLogger(VersionServiceImpl.class.getName());

    /**
     * @see cz.incad.Kramerius.StartupServlet.init() - jedine misto, kde se vola updateNewVersion(),
     * getVersion() se vola na nekolika mistech (*DbInitializer.initDatabase(), LoggedUserDbHelper.initDatabase()), ale vzdy samotne volane z StartupServlet.init()
     * Navic DatabaseStatisticsAccessLogImpl.
     * Takze vypada bezpecne cachovat verzi
     */
    private static final boolean VERSION_CACHING_ENABLED = true;

    String versionCached = null;

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider = null;

    @Override
    public synchronized String getVersion() throws SQLException {
        if (VERSION_CACHING_ENABLED && versionCached != null) {
            return versionCached;
        } else {
            String version = detectVersion();
            if (VERSION_CACHING_ENABLED) {
                this.versionCached = version;
            }
            return version;
        }
    }

    private String detectVersion() throws SQLException {
        Connection connection = this.connectionProvider.get();
        boolean versionTableExists = DatabaseUtils.tableExists(connection, "DBVERSIONS");
        if (versionTableExists) {
            List<String> ids = new JDBCQueryTemplate<String>(connection, true) {
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                    returnsList.add(rs.getString("ver"));
                    return false;
                }
            }.executeQuery("select DBVER_ID, ver from DBVERSIONS v join MAX_VERSION_VIEW mv " +
                    "on (v.DBVER_ID = mv.MAX_ID) ");
            return ids != null && ids.size() > 0 ? ids.get(0).trim() : null;
        } else {
            return null;
        }
    }

    @Override
    public void updateVersionIfOutdated() throws IOException, SQLException {
        InputStream is = this.getClass().getResourceAsStream("res/current.db.version");
        String latestVersion = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        String curVersion = getVersion();
        if ((curVersion == null) || (!curVersion.equals(latestVersion))) {
            LOGGER.log(Level.INFO, "updating database version ({0} -> {1})", new String[]{curVersion, latestVersion});
            JDBCUpdateTemplate template = new JDBCUpdateTemplate(this.connectionProvider.get(), true);
            template.executeUpdate("insert into DBVERSIONS values(nextval('DB_VERSIONS_SEQUENCE'),'" + latestVersion + "')");
            if (VERSION_CACHING_ENABLED) {
                this.versionCached = null;
            }
        } else {
            LOGGER.log(Level.INFO, "database version is up-to-date: {0}", curVersion);
        }
    }

}
