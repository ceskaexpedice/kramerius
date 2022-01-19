package cz.incad.kramerius.security.licenses.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseLicensesManagerImpl implements LicensesManager {


    private Provider<Connection> provider;

    private SolrAccess solrAccess;


    @Inject
    public DatabaseLicensesManagerImpl(@Named("kramerius4") Provider<Connection> provider, @Named("new-index") SolrAccess solrAccess) {
        this.provider = provider;
        this.solrAccess = solrAccess;
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
    public void addLocalLabel(License license) throws LicensesManagerException {
        Connection connection = null;
        int transactionIsolation = -1;
        try {
            connection = provider.get();
            transactionIsolation = connection.getTransactionIsolation();

            new JDBCTransactionTemplate(connection, true).updateWithTransaction(
                    new JDBCCommand() {
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
                    },
                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            List<Integer> priorities = (List<Integer>) getPreviousResult();
                            Integer min = priorities.isEmpty() ? License.DEFAULT_PRIORITY : Collections.max(priorities)+1;
                                                                                                                                                                                                  //    id, group, name, description, priority
                            PreparedStatement prepareStatement = con.prepareStatement("insert into labels_entity(label_id,label_group,label_name, label_description, label_priority) values(nextval('LABEL_ID_SEQUENCE'), ?, ?, ?, ?)");
                            prepareStatement.setString(1, license.getGroup());
                            prepareStatement.setString(2, license.getName());
                            prepareStatement.setString(3, license.getDescription());
                            prepareStatement.setInt(4, min);

                            return prepareStatement.executeUpdate();

                        }
                    });
        } catch (SQLException e) {
            throw new LicensesManagerException(e.getMessage(),e);
        }
    }

    @Override
    public void removeLocalLabel(License license) throws LicensesManagerException {
        try {




            new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(

                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con.prepareStatement("delete from rights_criterium_entity where label_id = ?");
                            prepareStatement.setInt(1, license.getId());
                            return prepareStatement.executeUpdate();
                        }
                    },

                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con.prepareStatement("delete from labels_entity where label_id = ?");
                            prepareStatement.setInt(1, license.getId());
                            return prepareStatement.executeUpdate();
                        }
                    },
                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con.prepareStatement("update labels_entity set label_priority = label_priority-1 where label_priority > ?", license.getPriority());
                            prepareStatement.setInt(1, license.getPriority());
                            return prepareStatement.executeUpdate();
                        }
                    }

                    );
        } catch (SQLException e) {
            throw new LicensesManagerException(e.getMessage(),e);
        }
    }


    @Override
    public License getLabelByPriority(int priority) throws LicensesManagerException {
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
    public License getLabelById(int id) throws LicensesManagerException {
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
    public License getLabelByName(String name) throws LicensesManagerException {
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
    public void updateLabel(License license) throws LicensesManagerException {
        try {
            new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(
                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con.prepareStatement("update labels_entity  " +
                                    "set LABEL_NAME=? , LABEL_DESCRIPTION=? where label_id = ?");

                            prepareStatement.setString(1, license.getName());
                            prepareStatement.setString(2, license.getDescription());

                            prepareStatement.setInt(3, license.getId());

                            return prepareStatement.executeUpdate();

                        }
                    });
        } catch (SQLException e) {
            throw new LicensesManagerException(e.getMessage(),e);
        }
    }

    @Override
    public void moveUp(License license) throws LicensesManagerException {
        int priority = license.getPriority();
        if (priority >= 2) {
            License upPriorityLicense = getLabelByPriority(priority - 1);
            if (upPriorityLicense != null) {
                int upPriority   = upPriorityLicense.getPriority();
                try {
                    new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(
                            new UpdatePriorityCommand(license.getUpdatedPriorityLabel(-1)),
                            new UpdatePriorityCommand(upPriorityLicense.getUpdatedPriorityLabel(-1)),

                            new UpdatePriorityCommand(license.getUpdatedPriorityLabel(upPriority)),
                            new UpdatePriorityCommand(upPriorityLicense.getUpdatedPriorityLabel(priority))
                    );
                } catch (SQLException e) {
                    throw new LicensesManagerException(e.getMessage(),e);
                }
            }
        } else throw new LicensesManagerException("cannot increase the priority for "+ license);
    }

    @Override
    public void moveDown(License license) throws LicensesManagerException {
        int priority = license.getPriority();
        if (priority < getMinPriority()) {
            License downPriorityLicense = getLabelByPriority(priority + 1);
            if (downPriorityLicense != null) {
                int downPriority = downPriorityLicense.getPriority();

                try {
                    new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(
                            new UpdatePriorityCommand(license.getUpdatedPriorityLabel(-1)),
                            new UpdatePriorityCommand(downPriorityLicense.getUpdatedPriorityLabel(-1)),

                            new UpdatePriorityCommand(license.getUpdatedPriorityLabel(downPriority)),
                            new UpdatePriorityCommand(downPriorityLicense.getUpdatedPriorityLabel(priority))
                    );
                } catch (SQLException e) {
                    throw new LicensesManagerException(e.getMessage(),e);
                }
            }
        } else throw new LicensesManagerException("cannot decrease the priority for "+ license);
    }

    @Override
    public List<License> getLabels() {
        return new JDBCQueryTemplate<License>(provider.get()){
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
        if (name == null) name ="";
        String groupName = rs.getString("LABEL_GROUP");
        String description = rs.getString("LABEL_DESCRIPTION");
        int priority = rs.getInt("LABEL_PRIORITY");
        return new LicenseImpl(labelId, name, description, groupName, priority);
    }


    @Override
    public void refreshLabelsFromSolr() throws LicensesManagerException {
        try {
            Document request = this.solrAccess.requestWithSelectReturningXml("facet.field=licenses&fl=licenses&q=*%3A*&rows=0&facet=on");
            Element dnntLabelsFromSolr = XMLUtils.findElement(request.getDocumentElement(), new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String name = element.getAttribute("name");
                    return name != null && name.equals("licenses");
                }
            });
            List<String> labelsUsedInSolr = XMLUtils.getElements(dnntLabelsFromSolr).stream().map(element -> {
                return element.getAttribute("name");
            }).collect(Collectors.toList());

            getLabels().stream().forEach(eLabel-> {
                labelsUsedInSolr.remove(eLabel.getName());
            });

            for (String lname : labelsUsedInSolr) {  this.addLocalLabel(new LicenseImpl(lname, "", LicensesManager.IMPORTED_GROUP_NAME)); }

        } catch (IOException e) {
            throw new LicensesManagerException(e.getMessage(),e);
        }

    }

    private static class UpdatePriorityCommand extends  JDBCCommand {

        private License license;
        public UpdatePriorityCommand(License license) {
            this.license = license;
        }

        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            PreparedStatement prepareStatement = con.prepareStatement("update labels_entity set label_priority = ? where label_id = ? ");

            if (license.getPriority() == -1) {
                prepareStatement.setNull(1, Types.INTEGER);
            } else {
                prepareStatement.setInt(1, license.getPriority());
            }
            prepareStatement.setInt(2, license.getId());

            return prepareStatement.executeUpdate();
        }
    }



}
