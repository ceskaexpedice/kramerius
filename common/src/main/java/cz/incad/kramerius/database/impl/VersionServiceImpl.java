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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.database.VersionInitializer;
import cz.incad.kramerius.database.VersionService;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

/**
 * Default version service implementation
 * @author pavels
 */
public class VersionServiceImpl implements VersionService {
    
    
    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider = null;

    
    @Override
    public String getVersion() throws SQLException {
        Connection connection = this.connectionProvider.get();
        try {
            boolean versionTable = DatabaseUtils.tableExists(connection, "DBVERSIONS");
            if (versionTable) {
                List<String> ids = new JDBCQueryTemplate<String>(connection, false) {
                    @Override
                    public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                        returnsList.add(rs.getString("ver"));
                        return false;
                    }
                }.executeQuery("select DBVER_ID, ver from DBVERSIONS v join MAX_VERSION_VIEW mv " +
                		"on (v.DBVER_ID = mv.MAX_ID) ");
                return ids != null && ids.size() > 0 ? ids.get(0).trim() : null;
            } else return null;
        } finally {
                if (connection != null) DatabaseUtils.tryClose(connection);
        }
        
    }


    @Override
    public void updateNewVersion() throws IOException, SQLException {
        InputStream is = this.getClass().getResourceAsStream("res/current.db.version");
        String version = IOUtils.readAsString(is, Charset.forName("UTF-8"), true);
        String curVersion = getVersion();
        if ((curVersion == null) || (!curVersion.equals(version))) {
            JDBCUpdateTemplate template = new JDBCUpdateTemplate(this.connectionProvider.get(), true);
            template.executeUpdate("insert into DBVERSIONS values(nextval('DB_VERSIONS_SEQUENCE'),'"+version+"')");
        }
    }
    
}
