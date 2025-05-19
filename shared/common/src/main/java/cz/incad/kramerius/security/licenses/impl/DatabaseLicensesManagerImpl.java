package cz.incad.kramerius.security.licenses.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.ibm.icu.impl.Pair;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
import cz.incad.kramerius.security.licenses.RuntimeLicenseType;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLock.ExclusiveLockType;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;

public class DatabaseLicensesManagerImpl implements LicensesManager {

    /** Must correspond with register.digitalniknihovna.cz */
    public static final String ACRONYM_LIBRARY_KEY = "acronym";

    private Provider<Connection> provider;

    private SolrAccess solrAccess;

    private String acronym;
    
    private ExclusiveLockMaps maps;
    
    @Inject
    public DatabaseLicensesManagerImpl(@Named("kramerius4") Provider<Connection> provider,
            @Named("new-index") SolrAccess solrAccess, ExclusiveLockMaps maps) {
        this.provider = provider;
        this.solrAccess = solrAccess;
        this.acronym = KConfiguration.getInstance().getConfiguration().getString(ACRONYM_LIBRARY_KEY, "");
        this.maps = maps;
    }

    @Override
    public int getMinPriority() throws LicensesManagerException {
        List<Integer> priorities = new JDBCQueryTemplate<Integer>(this.provider.get(), true) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                int max_priority = rs.getInt("label_priority");
                returnsList.add(max_priority);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select max(label_priority) label_priority from labels_entity");
        return priorities.isEmpty() ? License.DEFAULT_PRIORITY : priorities.get(0);
    }

    @Override
    public int getMaxPriority() throws LicensesManagerException {
        return License.DEFAULT_PRIORITY;
    }

    @Override
    public void addLocalLicense(License license) throws LicensesManagerException {
        checkAcronymExists();
        checkGroup(license);
        checkStartNameWithAcronym(license);
        
        Connection connection = null;
        int transactionIsolation = -1;
        try {
            connection = provider.get();
            transactionIsolation = connection.getTransactionIsolation();

            new JDBCTransactionTemplate(connection, true).updateWithTransaction(new JDBCCommand() {
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    List<Integer> priorities = new JDBCQueryTemplate<Integer>(con, false) {
                        @Override
                        public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                            int max_priority = rs.getInt("label_priority");
                            returnsList.add(max_priority);
                            return super.handleRow(rs, returnsList);
                        }
                    }.executeQuery("select max(label_priority) label_priority from labels_entity");
                    return priorities;
                }
            }, new JDBCCommand() {
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    List<Integer> priorities = (List<Integer>) getPreviousResult();
                    Integer min = priorities.isEmpty() ? License.DEFAULT_PRIORITY : Collections.max(priorities) + 1;

                    // id, group, name, description, priority
                    if (license.exclusiveLockPresent()) {
                        PreparedStatement prepareStatement = con.prepareStatement(
                                "insert into labels_entity(label_id," +
                                        "label_group," +
                                        "label_name, " +
                                        "label_description, " +
                                        "label_priority, " +
                                        "lock, " +
                                        "lock_maxreaders, " +
                                        "lock_refreshinterval, " +
                                        "lock_maxinterval," +
                                        "runtime," +
                                        "runtime_type) values(nextval('LABEL_ID_SEQUENCE'), ?, ?, ?, ?, ?, ?, ?, ?,?,?)");

                        prepareStatement.setString(1, license.getGroup());
                        prepareStatement.setString(2, license.getName());
                        prepareStatement.setString(3, license.getDescription());
                        prepareStatement.setInt(4, min);

                        prepareStatement.setBoolean(5, true);
                        prepareStatement.setInt(6, license.getExclusiveLock().getMaxReaders());
                        prepareStatement.setInt(7, license.getExclusiveLock().getRefreshInterval());
                        prepareStatement.setInt(8, license.getExclusiveLock().getMaxInterval());

                        prepareStatement.setBoolean(9, license.isRuntimeLicense());
                        if (license.isRuntimeLicense()) {
                            prepareStatement.setString(10, license.getRuntimeLicenseType() != null ? license.getRuntimeLicenseType().name() : null);
                        }

                        return prepareStatement.executeUpdate();

                    } else {
                        PreparedStatement prepareStatement = con.prepareStatement(
                                "insert into labels_entity(label_id,label_group,label_name, label_description, label_priority, " +
                                        "runtime, " +
                                        "runtime_type) values(nextval('LABEL_ID_SEQUENCE'), ?, ?, ?, ?,?,?)");
                        prepareStatement.setString(1, license.getGroup());
                        prepareStatement.setString(2, license.getName());
                        prepareStatement.setString(3, license.getDescription());
                        prepareStatement.setInt(4, min);

                        prepareStatement.setBoolean(5, license.isRuntimeLicense());
                        if (license.isRuntimeLicense()) {
                            prepareStatement.setString(6, license.getRuntimeLicenseType() != null ? license.getRuntimeLicenseType().name() : null);
                        }
                        return prepareStatement.executeUpdate();
                    }


                }
            });
        } catch (SQLException e) {
            throw new LicensesManagerException(e.getMessage(), e);
        }
    }

    private void checkGroup(License license) throws LicensesManagerException {
        if (!license.getGroup().equals(LicensesManager.LOCAL_GROUP_NAME)) {
            throw new LicensesManagerException("Given license is not local license");
        }
    }

    private void checkStartNameWithAcronym(License license) throws LicensesManagerException {
        if (!license.getName().startsWith(this.acronym + "_")) {
            throw new LicensesManagerException("Local license must start with '" + this.acronym + "_'");
        }
    }

    private void checkAcronymExists() throws LicensesManagerException {
        if (!StringUtils.isAnyString(this.acronym)) {
            throw new LicensesManagerException("property acronym must be defined in configuration.properties");
        }
    }

    @Override
    public void removeLocalLicense(License license) throws LicensesManagerException {
        checkGroup(license);
        checkAcronymExists();
        
        try {
            new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(

                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con
                                    .prepareStatement("delete from rights_criterium_entity where label_id = ?");
                            prepareStatement.setInt(1, license.getId());
                            return prepareStatement.executeUpdate();
                        }
                    },

                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con
                                    .prepareStatement("delete from labels_entity where label_id = ?");
                            prepareStatement.setInt(1, license.getId());
                            return prepareStatement.executeUpdate();
                        }
                    }, new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con.prepareStatement(
                                    "update labels_entity set label_priority = label_priority-1 where label_priority > ?",
                                    license.getPriority());
                            prepareStatement.setInt(1, license.getPriority());
                            return prepareStatement.executeUpdate();
                        }
                    }

            );
        } catch (SQLException e) {
            throw new LicensesManagerException(e.getMessage(), e);
        }
    }

    
    
    
    
    
    @Override
    public void rearrangePriorities(List<License> globalLicenses, Connection conn) throws LicensesManagerException {
        globalLicenses.sort((left, right) -> {
            int leftHint = left.getPriorityHint();
            int rightHint = right.getPriorityHint();
            return Integer.compare(leftHint, rightHint);
        });
        
        
        String condition = globalLicenses.stream().map( lic -> {
            return "label_name='"+lic.getName()+"'";
        }).collect(Collectors.joining(" OR "));
        

        List<License> realLicenses =new JDBCQueryTemplate<License>(conn, false) {
            @Override
            public boolean handleRow(ResultSet rs, List<License> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(String.format("select * from labels_entity where  %s order by label_priority asc", condition));
        

        // real priorities
        Map<String, License> realLienseMappings = new HashMap<>();
        realLicenses.forEach(lic-> {realLienseMappings.put(lic.getName(), lic); });
        
        List<JDBCCommand> commands = new ArrayList<>();
        for (int i = 0; i < globalLicenses.size(); i++) {
            License globalLicense = globalLicenses.get(i);
            int realPriority = realLicenses.get(i).getPriority();
            License realLicense = realLienseMappings.get(globalLicense.getName());
            commands.add(new  UpdateLicensePriorityCommand(realLicense.getUpdatedPriorityLabel(realPriority)));
        }

        try {
            new JDBCTransactionTemplate(conn, false).updateWithTransaction(commands);
        } catch (SQLException e) {
            throw new LicensesManagerException(e.getMessage(), e);
        }
        
    }

    @Override
    public void rearrangePriorities(List<License> globalLicenses) throws LicensesManagerException {

        globalLicenses.sort((left, right) -> {
            int leftHint = left.getPriorityHint();
            int rightHint = right.getPriorityHint();
            return Integer.compare(leftHint, rightHint);
        });
        
        
        String condition = globalLicenses.stream().map( lic -> {
            return "label_name='"+lic.getName()+"'";
        }).collect(Collectors.joining(" OR "));
        

        List<License> realLicenses =new JDBCQueryTemplate<License>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<License> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(String.format("select * from labels_entity where  %s order by label_priority asc", condition));
        

        // real priorities
        Map<String, License> realLienseMappings = new HashMap<>();
        realLicenses.forEach(lic-> {realLienseMappings.put(lic.getName(), lic); });
        
        List<JDBCCommand> commands = new ArrayList<>();
        for (int i = 0; i < globalLicenses.size(); i++) {
            License globalLicense = globalLicenses.get(i);
            int realPriority = realLicenses.get(i).getPriority();
            License realLicense = realLienseMappings.get(globalLicense.getName());
            commands.add(new  UpdateLicensePriorityCommand(realLicense.getUpdatedPriorityLabel(realPriority)));
        }

        try {
            new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(commands);
        } catch (SQLException e) {
            throw new LicensesManagerException(e.getMessage(), e);
        }

    }

    @Override
    public License getLicenseByPriority(int priority) throws LicensesManagerException {
        List<License> licenses = new JDBCQueryTemplate<License>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<License> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select * from labels_entity where LABEL_PRIORITY = ? ", priority);
        return licenses.isEmpty() ? null : licenses.get(0);
    }

    @Override
    public License getLicenseById(int id) throws LicensesManagerException {
        List<License> licenses = new JDBCQueryTemplate<License>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<License> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select * from labels_entity where LABEL_ID = ? ", id);
        return licenses.isEmpty() ? null : licenses.get(0);
    }

    @Override
    public License getLicenseByName(String name) throws LicensesManagerException {
        List<License> licenses = new JDBCQueryTemplate<License>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<License> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select * from labels_entity where LABEL_NAME = ? ", name);
        return licenses.isEmpty() ? null : licenses.get(0);
    }

    @Override
    public void updateLocalLicense(License license) throws LicensesManagerException {
        try {
            
            new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(new JDBCCommand() {
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    if (!license.exclusiveLockPresent()) {
                        PreparedStatement prepareStatement = con.prepareStatement(
                                "update labels_entity  " + "set LABEL_NAME=? , LABEL_DESCRIPTION=?, LOCK=false, lock_maxreaders=-1,lock_refreshinterval=-1, lock_maxinterval=-1," +
                                        "runtime=?, runtime_type=?"
                                        + " where label_id = ?");

                        prepareStatement.setString(1, license.getName());
                        prepareStatement.setString(2, license.getDescription());


                        prepareStatement.setBoolean(3, license.isRuntimeLicense());
                        if (license.isRuntimeLicense()) {
                            prepareStatement.setString(4, license.getRuntimeLicenseType() != null ? license.getRuntimeLicenseType().name() : null);
                        } else {
                            prepareStatement.setString(4, null);
                        }

                        prepareStatement.setInt(5, license.getId());


                        return prepareStatement.executeUpdate();
                        
                    } else {
                        /**
                         *  "insert into labels_entity(label_id,label_group,label_name, label_description, label_priority, lock, lock_maxreaders, lock_refreshinterval, lock_maxinterval) values(nextval('LABEL_ID_SEQUENCE'), ?, ?, ?, ?, ?, ?, ?, ?)");
                         */
                        PreparedStatement prepareStatement = con.prepareStatement(
                                "update labels_entity  " + "set LABEL_NAME=? , "
                                        + " LABEL_DESCRIPTION=?, "
                                        + " LOCK=true, "
                                        + " lock_maxreaders=?, "
                                        + " lock_refreshinterval=?, "
                                        + " lock_maxinterval=?,"
                                        + " lock_type=?," +
                                        "runtime=?, runtime_type=?"
                                        + " where label_id = ?");

                        prepareStatement.setString(1, license.getName());
                        prepareStatement.setString(2, license.getDescription());
                        prepareStatement.setInt(3, license.getExclusiveLock().getMaxReaders());
                        prepareStatement.setInt(4, license.getExclusiveLock().getRefreshInterval());
                        prepareStatement.setInt(5, license.getExclusiveLock().getMaxInterval());

                        prepareStatement.setString(6, license.getExclusiveLock().getType().name());
                        

                        prepareStatement.setBoolean(7, license.isRuntimeLicense());
                        if (license.isRuntimeLicense()) {
                            prepareStatement.setString(8, license.getRuntimeLicenseType() != null ? license.getRuntimeLicenseType().name() : null);
                        } else {
                            prepareStatement.setString(8, null);
                        }

                        prepareStatement.setInt(9, license.getId());

                        int executeUpdate = prepareStatement.executeUpdate();
                        maps.refreshLicense(license);
                        return executeUpdate;
                    }

                }
            });
        } catch (SQLException e) {
            throw new LicensesManagerException(e.getMessage(), e);
        }
    }
    
    

    @Override
    public void changeOrdering(List<License> licenses) throws LicensesManagerException {
        try {
            UpdatePriorityCommand[] commands = licenses.stream().map(l -> {
                return new UpdatePriorityCommand(l.getId(), l.getPriority());
            }).toArray(UpdatePriorityCommand[]::new);
            new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(commands);
        } catch (SQLException e) {
            throw new LicensesManagerException(e.getMessage(), e);
        }
    }

    @Override
    public void moveUp(License license) throws LicensesManagerException {

        List<License> licenses = getLicenses();
        List<Integer> ids = licenses.stream().map(License::getId).collect(Collectors.toList());
        List<Integer> priorities = licenses.stream().map(License::getPriority).collect(Collectors.toList());

        /**  index posunujici licence  */ 
        Integer movingIdIndex = ids.indexOf(license.getId());
        // priorita posunujici licence
        Integer movingPriority = priorities.get(movingIdIndex);
        if (movingIdIndex >= 1) {
            // predchozi index 
            Integer previousId = ids.get(movingIdIndex-1);
            // predchozi priorita
            Integer previousPriority = priorities.get(movingIdIndex-1);
            if (movingIdIndex != null && movingPriority != null && previousId != null && previousPriority != null) {
                try {
                    new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(
                            new UpdatePriorityCommand(license.getId(), previousPriority),
                            new UpdatePriorityCommand(previousId, movingPriority));
                } catch (SQLException e) {
                    throw new LicensesManagerException(e.getMessage(), e);
                }
            }
        } else
            throw new LicensesManagerException("cannot increase the priority for " + license);
    }

    @Override
    public void moveDown(License license) throws LicensesManagerException {

        List<License> licenses = getLicenses();
        List<Integer> ids = licenses.stream().map(License::getId).collect(Collectors.toList());
        List<Integer> priorities = licenses.stream().map(License::getPriority).collect(Collectors.toList());

        Integer movingIndex = ids.indexOf(license.getId());
        Integer movingPriority = priorities.get(movingIndex);
        if (movingIndex < ids.size()-1) {
            Integer nextId = ids.get(movingIndex+1);
            Integer previousPriority = priorities.get(movingIndex+1);
            
            if (movingIndex != null && movingPriority != null && nextId != null && previousPriority != null) {
                try {
                    new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(
                            new UpdatePriorityCommand(license.getId(), previousPriority),
                            new UpdatePriorityCommand(nextId, movingPriority));
                } catch (SQLException e) {
                    throw new LicensesManagerException(e.getMessage(), e);
                }
            }
        } else
            throw new LicensesManagerException("cannot increase the priority for " + license);

        //check(license);
//        int priority = license.getPriority();
//        if (priority < getMinPriority()) {
//            License downPriorityLicense = getLicenseByPriority(priority + 1);
//            if (downPriorityLicense != null) {
//                int downPriority = downPriorityLicense.getPriority();
//
//                try {
//                    new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(
//                            new UpdatePriorityCommand(license.getUpdatedPriorityLabel(-1)),
//                            new UpdatePriorityCommand(downPriorityLicense.getUpdatedPriorityLabel(-1)),
//
//                            new UpdatePriorityCommand(license.getUpdatedPriorityLabel(downPriority)),
//                            new UpdatePriorityCommand(downPriorityLicense.getUpdatedPriorityLabel(priority)));
//                } catch (SQLException e) {
//                    throw new LicensesManagerException(e.getMessage(), e);
//                }
//            }
//        } else
//            throw new LicensesManagerException("cannot decrease the priority for " + license);
    }

    @Override
    public List<License> getLicenses() {
        return new JDBCQueryTemplate<License>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<License> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select * from labels_entity order by LABEL_PRIORITY ASC NULLS LAST ");
    }

    private License createLabelFromResultSet(ResultSet rs) throws SQLException {
        int labelId = rs.getInt("label_id");
        String name = rs.getString("LABEL_NAME");
        if (name == null)
            name = "";
        String groupName = rs.getString("LABEL_GROUP");
        String description = rs.getString("LABEL_DESCRIPTION");
        int priority = rs.getInt("LABEL_PRIORITY");
        
        boolean lock = rs.getBoolean("LOCK");
        int maxreaders = rs.getInt("LOCK_MAXREADERS");
        int refreshinterval = rs.getInt("LOCK_REFRESHINTERVAL");
        int maxinterval = rs.getInt("LOCK_MAXINTERVAL");
        String lockTypeStr = rs.getString("LOCK_TYPE");

        boolean runtime = rs.getBoolean("RUNTIME");
        String runtimeType = rs.getString("RUNTIME_TYPE");

        LicenseImpl licenseImpl = new LicenseImpl(labelId, name, description, groupName, priority);
        
        if (lock) {
            licenseImpl.initExclusiveLock(refreshinterval, maxinterval, maxreaders,ExclusiveLockType.findByType(lockTypeStr));
        }

        if (runtime) {
            Optional<RuntimeLicenseType> typeOpt = RuntimeLicenseType.fromString(runtimeType);
            typeOpt.ifPresent(type -> {
                licenseImpl.initRuntime(type);
            });
        }

        return licenseImpl;
    }

    @Override
    public void refreshLabelsFromSolr() throws LicensesManagerException {
        throw new UnsupportedOperationException("unsupported");
    }


    private static class UpdatePriorityCommand extends JDBCCommand {

        private Integer id;
        private Integer priority;
        
        public UpdatePriorityCommand(Integer id, Integer priority) {
            this.id = id;
            this.priority = priority;
        }

        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            PreparedStatement prepareStatement = con
                    .prepareStatement("update labels_entity set label_priority = ? where label_id = ? ");

            if (priority == -1) {
                prepareStatement.setNull(1, Types.INTEGER);
            } else {
                prepareStatement.setInt(1, priority);
            }
            prepareStatement.setInt(2, this.id);

            return prepareStatement.executeUpdate();
        }
    }

    private static class UpdateLicensePriorityCommand extends JDBCCommand {

        private License license;

        public UpdateLicensePriorityCommand(License license) {
            this.license = license;
        }

        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            PreparedStatement prepareStatement = con
                    .prepareStatement("update labels_entity set label_priority = ? where label_id = ? ");

            if (license.getPriority() == -1) {
                prepareStatement.setNull(1, Types.INTEGER);
            } else {
                prepareStatement.setInt(1, license.getPriority());
            }
            prepareStatement.setInt(2, license.getId());

            return prepareStatement.executeUpdate();
        }
    }

    @Override
    public List<License> getGlobalLicenses() throws LicensesManagerException {
        return new JDBCQueryTemplate<License>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<License> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery(
                "select * from labels_entity where label_group = ? OR label_group = ? order by LABEL_PRIORITY ASC NULLS LAST ",
                LicensesManager.GLOBAL_GROUP_NAME_IMPORTED, LicensesManager.GLOBAL_GROUP_NAME_EMBEDDED);
    }

    @Override
    public List<License> getLocalLicenses() throws LicensesManagerException {
        return new JDBCQueryTemplate<License>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<License> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select * from labels_entity where label_group = ? order by LABEL_PRIORITY ASC NULLS LAST  ",
                LicensesManager.LOCAL_GROUP_NAME);
    }

    @Override
    public List<License> getAllLicenses() throws LicensesManagerException {
        return new JDBCQueryTemplate<License>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<License> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select * from labels_entity order by LABEL_PRIORITY ASC NULLS LAST ");
    }

}
