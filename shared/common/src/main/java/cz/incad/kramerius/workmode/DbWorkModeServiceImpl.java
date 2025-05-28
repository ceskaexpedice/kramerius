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
package cz.incad.kramerius.workmode;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DbWorkModeServiceImpl
 * @author ppodsednik
 */
public class DbWorkModeServiceImpl implements WorkModeService {

    public static final Logger LOGGER = Logger.getLogger(DbWorkModeServiceImpl.class.getName());

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;

    public DbWorkModeServiceImpl() {
    }

    @Override
    public void setWorkMode(WorkMode workMode) {
        List<Boolean> result = new JDBCQueryTemplate<Boolean>(this.connectionProvider.get(), true) {
            @Override
            public boolean handleRow(ResultSet rs, List<Boolean> returnsList) throws SQLException {
                returnsList.add(true);
                return false; // Stop after first row
            }
        }.executeQuery("SELECT readOnly FROM workmode WHERE id = 'singleton'");
        try {
            JDBCUpdateTemplate template = new JDBCUpdateTemplate(this.connectionProvider.get(), true);
            if (result.isEmpty()) {
                template.executeUpdate("INSERT INTO workmode (id, readOnly, reason) VALUES ('singleton', ?, ?)",
                        workMode.isReadOnly(), workMode.getReason() == null ? WorkModeReason.noReason.name() : workMode.getReason().name());
            } else {
                String sql = "UPDATE workmode SET readOnly = ?, reason = ? WHERE id = 'singleton'";
                template.executeUpdate(sql, workMode.isReadOnly(),  workMode.getReason() == null ? WorkModeReason.noReason.name() : workMode.getReason().name());
            }
            LOGGER.log(Level.INFO, "workmode status update as readOnly: {0}", workMode.isReadOnly());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public WorkMode getWorkMode() {
        List<WorkMode> result = new JDBCQueryTemplate<WorkMode>(this.connectionProvider.get(), true) {
            @Override
            public boolean handleRow(ResultSet rs, List<WorkMode> returnsList) throws SQLException {
                boolean readOnly = rs.getBoolean("readOnly");
                String reason = rs.getString("reason");
                returnsList.add(new WorkMode(readOnly, WorkModeReason.valueOf(reason)));
                return false; // Stop after first row
            }
        }.executeQuery("SELECT readOnly, reason FROM workmode WHERE id = 'singleton'");

        return result.isEmpty() ? null : result.get(0);
    }

}
