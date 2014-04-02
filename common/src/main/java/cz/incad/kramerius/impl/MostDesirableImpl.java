package cz.incad.kramerius.impl;

import static cz.incad.kramerius.processes.database.MostDesirableDatabaseUtils.LOGGER;
import static cz.incad.kramerius.processes.database.MostDesirableDatabaseUtils.createTable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.processes.database.MostDesirableDatabaseUtils;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

public class MostDesirableImpl implements MostDesirable {

    private Provider<Connection> provider;
    private FedoraAccess fedoraAccess;

    @Inject
    public MostDesirableImpl(
            @Named("kramerius4") Provider<Connection> provider,
            @Named("securedFedoraAccess") FedoraAccess fa) {
        super();
        this.provider = provider;
        this.fedoraAccess = fa;
    }

    @Override
    public List<String> getMostDesirable(int count, int offset, String model) {
        if (model != null) {
            return new JDBCQueryTemplate<String>(provider.get(), true) {
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList)
                        throws SQLException {
                    returnsList.add(rs.getString("uuid"));
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery("SELECT count(*) as count , uuid, model FROM desirable where model = ? group by uuid, model order by count DESC  LIMIT ? OFFSET ?", model, count, offset);
        } else {
            return new JDBCQueryTemplate<String>(provider.get(), true) {
                @Override
                public boolean handleRow(ResultSet rs, List<String> returnsList)
                        throws SQLException {
                    returnsList.add(rs.getString("uuid"));
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery("SELECT count(*) as count , uuid, model FROM desirable  group by uuid, model order by count DESC  LIMIT ? OFFSET ?", count, offset);
        }
    }

    @Override
    public void saveAccess(String uuid, Date date) {
        try {
            String modelName = this.fedoraAccess.getKrameriusModelName(uuid);
            new JDBCUpdateTemplate(provider.get())
                    .executeUpdate(
                            "insert into DESIRABLE(UUID, ACCESS, MODEL) values(?, ?, ?)",
                            uuid, new Timestamp(date.getTime()), modelName);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
