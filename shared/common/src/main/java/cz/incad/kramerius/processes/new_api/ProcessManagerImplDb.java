package cz.incad.kramerius.processes.new_api;

import cz.incad.kramerius.processes.NotReadyException;
import cz.incad.kramerius.utils.DatabaseUtils;
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
        try {
            String query = "SELECT count(*) FROM process_batch as batch" + buildFilterClause(filter);
            //System.out.println(query);
            List<Integer> list = new JDBCQueryTemplate<Integer>(connection, false) {
                public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                    returnsList.add(rs.getInt("count"));
                    return true;
                }
            }.executeQuery(query);
            return list.get(0);
        } finally {
            DatabaseUtils.tryClose(connection);
        }
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
    public List<ProcessInBatch> getProcessesInBatches(Filter filter, int offset, int limit) {
        Connection connection = connectionProvider.get();
        if (connection == null) {
            throw new NotReadyException("connection not ready");
        }
        try {
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

                            "processes.process_id AS process_id," +
                            "processes.uuid AS process_uuid," +
                            "processes.defid AS process_defid," +
                            "processes.name AS process_name," +
                            "processes.status AS process_state," +
                            "processes.planned AS process_planned," +
                            "processes.started AS process_started," +
                            "processes.finished AS process_finished" +

                            " FROM" +
                            " (" + filteredBatchQuery + ") batch" +
                            " JOIN" +
                            " processes" +
                            " ON" +
                            " batch.batch_token=processes.token" +
                            " ORDER BY " +
                            " batch_id DESC, process_id ASC";
            //System.out.println(joinQuery);

            return new JDBCQueryTemplate<ProcessInBatch>(connection) {
                @Override
                public boolean handleRow(ResultSet rs, List<ProcessInBatch> returnsList) throws SQLException {
                    ProcessInBatch processInBatch = new ProcessInBatch();
                    processInBatch.batchToken = rs.getString("batch_token");
                    processInBatch.batchId = rs.getString("batch_id");
                    processInBatch.batchStateCode = rs.getInt("batch_state");
                    processInBatch.batchPlanned = toLocalDateTime(rs.getTimestamp("batch_planned"));
                    processInBatch.batchStarted = toLocalDateTime(rs.getTimestamp("batch_started"));
                    processInBatch.batchFinished = toLocalDateTime(rs.getTimestamp("batch_finished"));
                    processInBatch.batchOwnerLogin = rs.getString("batch_owner_login");
                    processInBatch.batchOwnerFirstname = rs.getString("batch_owner_firstname");
                    processInBatch.batchOwnerSurname = rs.getString("batch_owner_surname");
                    processInBatch.batchSize = rs.getInt("batch_size");

                    processInBatch.processId = rs.getString("process_id");
                    processInBatch.processUuid = rs.getString("process_uuid");
                    processInBatch.processDefid = rs.getString("process_defid");
                    processInBatch.processName = rs.getString("process_name");
                    processInBatch.processStateCode = rs.getInt("process_state");
                    processInBatch.processPlanned = toLocalDateTime(rs.getTimestamp("process_planned"));
                    processInBatch.processStarted = toLocalDateTime(rs.getTimestamp("process_started"));
                    processInBatch.processFinished = toLocalDateTime(rs.getTimestamp("process_finished"));

                    returnsList.add(processInBatch);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(joinQuery);
        } finally {
            DatabaseUtils.tryClose(connection);
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        } else {
            return timestamp.toLocalDateTime();
        }
    }
}
