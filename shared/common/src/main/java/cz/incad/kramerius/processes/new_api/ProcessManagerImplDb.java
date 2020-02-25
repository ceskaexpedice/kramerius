package cz.incad.kramerius.processes.new_api;

import cz.incad.kramerius.processes.NotReadyException;
import cz.incad.kramerius.processes.ProcessManagerException;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
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
            String query = "SELECT count(*) FROM process_batch AS batch" + buildFilterClause(filter);
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
                builder.append(" batch.owner_id='").append(filter.owner).append("'");
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
            String filteredBatchQuery = String.format("SELECT * FROM process_batch AS batch %s order by first_process_id desc OFFSET %d LIMIT %d", buildFilterClause(filter), offset, limit);
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
                            "batch.owner_id AS batch_owner_id," +
                            "batch.owner_name AS batch_owner_name," +

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
                    processInBatch.batchOwnerId = rs.getString("batch_owner_id");
                    processInBatch.batchOwnerName = rs.getString("batch_owner_name");
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


    @Override
    public List<ProcessOwner> getProcessesOwners() {
        Connection connection = connectionProvider.get();
        if (connection == null) {
            throw new NotReadyException("connection not ready");
        }
        try {
            return new JDBCQueryTemplate<ProcessOwner>(connection) {
                @Override
                public boolean handleRow(ResultSet rs, List<ProcessOwner> returnsList) throws SQLException {
                    ProcessOwner owner = new ProcessOwner();
                    owner.id = rs.getString("owner_id");
                    owner.name = rs.getString("owner_name");
                    returnsList.add(owner);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery("SELECT DISTINCT owner_id, owner_name FROM processes ORDER BY owner_name ASC");
        } finally {
            DatabaseUtils.tryClose(connection);
        }
    }

    @Override
    public Batch getBatchByFirstProcessId(int firstProcessId) {
        Connection connection = connectionProvider.get();
        if (connection == null) {
            throw new NotReadyException("connection not ready");
        }
        try {
            String sql = "SELECT * FROM process_batch AS batch WHERE first_process_id = ?";
            List<Batch> batches = new JDBCQueryTemplate<Batch>(connection) {
                @Override
                public boolean handleRow(ResultSet rs, List<Batch> returnsList) throws SQLException {
                    Batch batch = new Batch();
                    batch.token = rs.getString("batch_token");
                    batch.firstProcessId = rs.getString("first_process_id");
                    batch.stateCode = rs.getInt("batch_state");
                    batch.planned = toLocalDateTime(rs.getTimestamp("planned"));
                    batch.started = toLocalDateTime(rs.getTimestamp("started"));
                    batch.finished = toLocalDateTime(rs.getTimestamp("finished"));
                    batch.ownerId = rs.getString("owner_id");
                    batch.ownerName = rs.getString("owner_name");
                    returnsList.add(batch);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql, firstProcessId);
            return !batches.isEmpty() ? batches.get(0) : null;
        } finally {
            DatabaseUtils.tryClose(connection);
        }
    }


    @Override
    public ProcessInBatch getProcessInBatchByProcessId(int processId) {
        Connection connection = connectionProvider.get();
        if (connection == null) {
            throw new NotReadyException("connection not ready");
        }
        try {
            String sql = "SELECT" +
                    "  batch.batch_token AS batch_token," +
                    "  batch.first_process_id AS batch_id," +
                    "  batch.batch_state AS batch_state," +
                    "  batch.planned AS batch_planned," +
                    "  batch.started AS batch_started," +
                    "  batch.finished AS batch_finished," +
                    "  batch.owner_id AS batch_owner_id," +
                    "  batch.owner_name AS batch_owner_name," +
                    "  batch.process_count AS batch_size," +
                    "" +
                    "  process.process_id AS process_id," +
                    "  process.uuid AS process_uuid," +
                    "  process.defid AS process_defid," +
                    "  process.name AS process_name," +
                    "  process.status AS process_state," +
                    "  process.planned AS process_planned," +
                    "  process.started AS process_started," +
                    "  process.finished AS process_finished" +
                    " FROM" +
                    "  process_batch AS batch," +
                    "  processes AS process" +
                    " WHERE" +
                    "  process.process_id = ?" +
                    " AND" +
                    "  batch.first_process_id = process.process_id";
            List<ProcessInBatch> processes = new JDBCQueryTemplate<ProcessInBatch>(connection) {
                @Override
                public boolean handleRow(ResultSet rs, List<ProcessInBatch> returnsList) throws SQLException {
                    ProcessInBatch result = new ProcessInBatch();
                    result.batchToken = rs.getString("batch_token");
                    result.batchId = rs.getString("batch_id");
                    result.batchStateCode = rs.getInt("batch_state");
                    result.batchPlanned = toLocalDateTime(rs.getTimestamp("batch_planned"));
                    result.batchStarted = toLocalDateTime(rs.getTimestamp("batch_started"));
                    result.batchFinished = toLocalDateTime(rs.getTimestamp("batch_finished"));
                    result.batchOwnerId = rs.getString("batch_owner_id");
                    result.batchOwnerName = rs.getString("batch_owner_name");
                    result.batchSize = rs.getInt("batch_size");

                    result.processId = rs.getString("process_id");
                    result.processUuid = rs.getString("process_uuid");
                    result.processDefid = rs.getString("process_defid");
                    result.processName = rs.getString("process_name");
                    result.processStateCode = rs.getInt("process_state");
                    result.processPlanned = toLocalDateTime(rs.getTimestamp("process_planned"));
                    result.processStarted = toLocalDateTime(rs.getTimestamp("process_started"));
                    result.processFinished = toLocalDateTime(rs.getTimestamp("process_finished"));
                    returnsList.add(result);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql, processId);
            return !processes.isEmpty() ? processes.get(0) : null;
        } finally {
            DatabaseUtils.tryClose(connection);
        }
    }

    @Override
    public int deleteBatchByBatchToken(String batchToken) {
        Connection connection = connectionProvider.get();
        if (connection == null) {
            throw new NotReadyException("connection not ready");
        }
        try {
            PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM processes WHERE token = ?");
            prepareStatement.setString(1, batchToken);
            int deleted = prepareStatement.executeUpdate();
            return deleted;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ProcessManagerException(e);
        } finally {
            DatabaseUtils.tryClose(connection);
        }
    }

    @Override
    public void setProcessAuthToken(int processId, String processAuthToken) {
        Connection connection = connectionProvider.get();
        if (connection == null) {
            throw new NotReadyException("connection not ready");
        }
        try {
            PreparedStatement prepareStatement = connection.prepareStatement("INSERT INTO process_auth_token (process_id, auth_token) VALUES (?,?);");
            prepareStatement.setInt(1, processId);
            prepareStatement.setString(2, processAuthToken);
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ProcessManagerException(e);
        } finally {
            DatabaseUtils.tryClose(connection);
        }
    }

    @Override
    public ProcessAboutToScheduleSibling getProcessAboutToScheduleSiblingByAuthToken(String processAuthToken) {
        Connection connection = connectionProvider.get();
        if (connection == null) {
            throw new NotReadyException("connection not ready");
        }
        try {
            String sql = "" +
                    " SELECT" +
                    "  process.process_id AS process_id," +
                    "  process.owner_id AS owner_id," +
                    "  process.owner_name AS owner_name," +
                    "  process.token AS batch_token" +
                    " FROM" +
                    "  processes AS process," +
                    "  process_auth_token AS auth" +
                    " WHERE" +
                    "  auth.auth_token = ?" +
                    "   AND" +
                    "  process.process_id=auth.process_id";
            List<ProcessAboutToScheduleSibling> processes = new JDBCQueryTemplate<ProcessAboutToScheduleSibling>(connection) {
                @Override
                public boolean handleRow(ResultSet rs, List<ProcessAboutToScheduleSibling> returnsList) throws SQLException {
                    int processId = rs.getInt("process_id");
                    String ownerId = rs.getString("owner_id");
                    String ownerName = rs.getString("owner_name");
                    String batchToken = rs.getString("batch_token");
                    ProcessAboutToScheduleSibling process = new ProcessAboutToScheduleSibling(processId, ownerId, ownerName, batchToken);
                    returnsList.add(process);
                    return super.handleRow(rs, returnsList);
                }
            }.executeQuery(sql, processAuthToken);
            return !processes.isEmpty() ? processes.get(0) : null;
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
