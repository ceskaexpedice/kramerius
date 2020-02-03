package cz.incad.kramerius.processes.new_api;

import cz.incad.kramerius.processes.NotReadyException;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class ProcessManagerImplDb implements ProcessManager {

    public static final Logger LOGGER = Logger.getLogger(ProcessManagerImplDb.class.getName());

    @Inject
    @Named("kramerius4")
    private Provider<Connection> connectionProvider;

    @Override
    public Integer getBatchesCount(Filter filter) {
        Connection connection = connectionProvider.get();
        if (connection == null) {
            throw new NotReadyException("connection not ready");
        }
        List<Integer> list = new JDBCQueryTemplate<Integer>(connection, false) {
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                returnsList.add(rs.getInt("count"));
                return true;
            }
        }.executeQuery("SELECT count(*) FROM process_batch as batch" + buildFilterClause(filter));
        return list.get(0);
    }

    private String buildFilterClause(Filter filter) {
        if (filter.isEmpty()) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append(" WHERE");
            boolean firstCondition = true;
            //owner
            if (filter.owner != null) {
                if (!firstCondition) {
                    builder.append(" AND");
                } else {
                    firstCondition = false;
                }
                builder.append(" batch.ownerlogin='").append(filter.owner).append("'");
            }
            //from
            if (filter.from != null) {
                if (!firstCondition) {
                    builder.append(" AND");
                } else {
                    firstCondition = false;
                }
                builder.append(" batch.planned>='").append(filter.from).append("'");
            }
            //until
            if (filter.until != null) {
                if (!firstCondition) {
                    builder.append(" AND");
                } else {
                    firstCondition = false;
                }
                builder.append(" batch.finished<='").append(filter.until).append("'");
            }
            //state
            if (filter.stateCode != null) {
                if (!firstCondition) {
                    builder.append(" AND");
                } else {
                    firstCondition = false;
                }
                builder.append(" batch.batch_state=").append(filter.stateCode);
            }
            return builder.toString();
        }
    }

    @Override
    public List<Batch> getBatches(Filter filter, int offset, int limit) {
        Connection connection = connectionProvider.get();
        if (connection == null) {
            throw new NotReadyException("connection not ready");
        }
        String filteredBatchQuery = String.format("SELECT * FROM process_batch as batch %s OFFSET %d LIMIT %d", buildFilterClause(filter), offset, limit);
        //System.out.println(filteredBatchQuery);
        String joinQuery =
                "SELECT " +
                        "batch.batch_token AS batch_token," +
                        "batch.first_process_id AS batch_id," +
                        "batch.batch_state AS batch_state," +
                        "batch.process_count AS batch_size," +
                        "batch.planned AS batch_planned," +
                        "batch.started AS batch_started," +
                        "batch.finished AS batch_finished," +
                        "batch.ownerLogin AS batch_owner_login," +
                        "processes.firstname AS batch_owner_firstname," +
                        "processes.surname AS batch_owner_surname," +

                        "processes.process_id," +
                        "processes.uuid," +
                        "processes.defid," +
                        "processes.name," +
                        "processes.status AS state," +
                        "processes.planned," +
                        "processes.started," +
                        "processes.finished" +

                        " FROM" +
                        " (" + filteredBatchQuery + ") batch" +
                        " JOIN" +
                        " processes" +
                        " ON" +
                        " batch.batch_token=processes.token" +
                        " ORDER BY " +
                        " processes.process_id DESC";
        //System.out.println(joinQuery);

        return new JDBCQueryTemplate<Batch>(connection) {
            @Override
            public boolean handleRow(ResultSet rs, List<Batch> returnsList) throws SQLException {
                Batch batch = new Batch();
                batch.token = rs.getString("batch_token");
                batch.id = rs.getString("batch_id");
                batch.stateCode = rs.getInt("batch_state");
                batch.planned = toLocalDateTime(rs.getTimestamp("batch_planned"));
                batch.started = toLocalDateTime(rs.getTimestamp("batch_started"));
                batch.finished = toLocalDateTime(rs.getTimestamp("batch_finished"));
                batch.ownerLogin = rs.getString("batch_owner_login");
                batch.ownerFirstname = rs.getString("batch_owner_firstname");
                batch.ownerSurname = rs.getString("batch_owner_surname");
                //batch. = rs.getString("batch_");
                returnsList.add(batch);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(joinQuery);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        } else {
            return timestamp.toLocalDateTime();
        }
    }
}
