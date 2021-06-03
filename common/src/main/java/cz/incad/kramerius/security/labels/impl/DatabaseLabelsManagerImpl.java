package cz.incad.kramerius.security.labels.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.labels.Label;
import cz.incad.kramerius.security.labels.LabelsManager;
import cz.incad.kramerius.security.labels.LabelsManagerException;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseLabelsManagerImpl implements LabelsManager {


    private Provider<Connection> provider;

    private SolrAccess solrAccess;


    @Inject
    public DatabaseLabelsManagerImpl(@Named("kramerius4") Provider<Connection> provider, SolrAccess solrAccess) {
        this.provider = provider;
        this.solrAccess = solrAccess;
    }


    @Override
    public int getMinPriority() throws LabelsManagerException {
        List<Integer> priorities = new JDBCQueryTemplate<Integer>(this.provider.get(), true) {
            @Override
            public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
                int max_priority = rs.getInt("label_priority");
                returnsList.add(max_priority);
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select max(label_priority) label_priority from labels_entity");

        return priorities.isEmpty() ? Label.DEFAULT_PRIORITY : priorities.get(0);
    }

    @Override
    public int getMaxPriority() throws LabelsManagerException {
        return Label.DEFAULT_PRIORITY;
    }

    @Override
    public void addLocalLabel(Label label) throws LabelsManagerException {
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
                            Integer min = priorities.isEmpty() ? Label.DEFAULT_PRIORITY : Collections.max(priorities)+1;
                                                                                                                                                                                                  //    id, group, name, description, priority
                            PreparedStatement prepareStatement = con.prepareStatement("insert into labels_entity(label_id,label_group,label_name, label_description, label_priority) values(nextval('LABEL_ID_SEQUENCE'), ?, ?, ?, ?)");
                            prepareStatement.setString(1, label.getGroup());
                            prepareStatement.setString(2, label.getName());
                            prepareStatement.setString(3, label.getDescription());
                            prepareStatement.setInt(4, min);

                            return prepareStatement.executeUpdate();

                        }
                    });
        } catch (SQLException e) {
            throw new LabelsManagerException(e.getMessage(),e);
        }
    }

    @Override
    public void removeLocalLabel(Label label) throws LabelsManagerException {
        try {




            new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(

                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con.prepareStatement("delete from rights_criterium_entity where label_id = ?");
                            prepareStatement.setInt(1, label.getId());
                            return prepareStatement.executeUpdate();
                        }
                    },

                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con.prepareStatement("delete from labels_entity where label_id = ?");
                            prepareStatement.setInt(1, label.getId());
                            return prepareStatement.executeUpdate();
                        }
                    },
                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con.prepareStatement("update labels_entity set label_priority = label_priority-1 where label_priority > ?", label.getPriority());
                            prepareStatement.setInt(1, label.getPriority());
                            return prepareStatement.executeUpdate();
                        }
                    }

                    );
        } catch (SQLException e) {
            throw new LabelsManagerException(e.getMessage(),e);
        }
    }


    @Override
    public Label getLabelByPriority(int priority) throws LabelsManagerException {
        List<Label> labels = new JDBCQueryTemplate<Label>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Label> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select * from labels_entity where LABEL_PRIORITY = ? ", priority);
        return labels.isEmpty() ? null : labels.get(0);
    }


    @Override
    public Label getLabelById(int id) throws LabelsManagerException {
        List<Label> labels = new JDBCQueryTemplate<Label>(provider.get()) {
            @Override
            public boolean handleRow(ResultSet rs, List<Label> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select * from labels_entity where LABEL_ID = ? ", id);
        return labels.isEmpty() ? null : labels.get(0);
    }

    @Override
    public void updateLabel(Label label) throws LabelsManagerException {
        try {
            new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(
                    new JDBCCommand() {
                        @Override
                        public Object executeJDBCCommand(Connection con) throws SQLException {
                            PreparedStatement prepareStatement = con.prepareStatement("update labels_entity  " +
                                    "set LABEL_NAME=? , LABEL_DESCRIPTION=? where label_id = ?");

                            prepareStatement.setString(1, label.getName());
                            prepareStatement.setString(2, label.getDescription());

                            prepareStatement.setInt(3, label.getId());

                            return prepareStatement.executeUpdate();

                        }
                    });
        } catch (SQLException e) {
            throw new LabelsManagerException(e.getMessage(),e);
        }
    }

    @Override
    public void moveUp(Label label) throws LabelsManagerException {
        int priority = label.getPriority();
        if (priority >= 2) {
            Label upPriorityLabel = getLabelByPriority(priority - 1);
            if (upPriorityLabel != null) {
                int upPriority   = upPriorityLabel.getPriority();
                try {
                    new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(
                            new UpdatePriorityCommand(label.getUpdatedPriorityLabel(-1)),
                            new UpdatePriorityCommand(upPriorityLabel.getUpdatedPriorityLabel(-1)),

                            new UpdatePriorityCommand(label.getUpdatedPriorityLabel(upPriority)),
                            new UpdatePriorityCommand(upPriorityLabel.getUpdatedPriorityLabel(priority))
                    );
                } catch (SQLException e) {
                    throw new LabelsManagerException(e.getMessage(),e);
                }
            }
        } else throw new LabelsManagerException("cannot increase the priority for "+label);
    }

    @Override
    public void moveDown(Label label) throws LabelsManagerException {
        int priority = label.getPriority();
        if (priority < getMinPriority()) {
            Label downPriorityLabel = getLabelByPriority(priority + 1);
            if (downPriorityLabel != null) {
                int downPriority = downPriorityLabel.getPriority();

                try {
                    new JDBCTransactionTemplate(provider.get(), true).updateWithTransaction(
                            new UpdatePriorityCommand(label.getUpdatedPriorityLabel(-1)),
                            new UpdatePriorityCommand(downPriorityLabel.getUpdatedPriorityLabel(-1)),

                            new UpdatePriorityCommand(label.getUpdatedPriorityLabel(downPriority)),
                            new UpdatePriorityCommand(downPriorityLabel.getUpdatedPriorityLabel(priority))
                    );
                } catch (SQLException e) {
                    throw new LabelsManagerException(e.getMessage(),e);
                }
            }
        } else throw new LabelsManagerException("cannot decrease the priority for "+label);
    }

    @Override
    public List<Label> getLabels() {
        return new JDBCQueryTemplate<Label>(provider.get()){
            @Override
            public boolean handleRow(ResultSet rs, List<Label> returnsList) throws SQLException {
                returnsList.add(createLabelFromResultSet(rs));
                return super.handleRow(rs, returnsList);
            }
        }.executeQuery("select * from labels_entity order by LABEL_PRIORITY ASC NULLS LAST ");
    }

    private Label createLabelFromResultSet(ResultSet rs) throws SQLException {
        int labelId = rs.getInt("label_id");
        String name = rs.getString("LABEL_NAME");
        String groupName = rs.getString("LABEL_GROUP");
        String description = rs.getString("LABEL_DESCRIPTION");
        int priority = rs.getInt("LABEL_PRIORITY");
        return new LabelImpl(labelId, name, description, groupName, priority);
    }


    @Override
    public void refreshLabelsFromSolr() throws LabelsManagerException{
        try {
            Document request = this.solrAccess.request("facet.field=dnnt-labels&fl=dnnt-labels&q=*%3A*&rows=0&facet=on");
            Element dnntLabelsFromSolr = XMLUtils.findElement(request.getDocumentElement(), new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String name = element.getAttribute("name");
                    return name != null && name.equals("dnnt-labels");
                }
            });
            List<String> labelsUsedInSolr = XMLUtils.getElements(dnntLabelsFromSolr).stream().map(element -> {
                return element.getAttribute("name");
            }).collect(Collectors.toList());

            getLabels().stream().forEach(eLabel-> {
                labelsUsedInSolr.remove(eLabel.getName());
            });

            for (String lname : labelsUsedInSolr) {  this.addLocalLabel(new LabelImpl(lname, "", LabelsManager.IMPORTED_GROUP_NAME)); }

        } catch (IOException e) {
            throw new LabelsManagerException(e.getMessage(),e);
        }

    }

    private static class UpdatePriorityCommand extends  JDBCCommand {

        private Label label;
        public UpdatePriorityCommand(Label label) {
            this.label = label;
        }

        @Override
        public Object executeJDBCCommand(Connection con) throws SQLException {
            PreparedStatement prepareStatement = con.prepareStatement("update labels_entity set label_priority = ? where label_id = ? ");

            if (label.getPriority() == -1) {
                prepareStatement.setNull(1, Types.INTEGER);
            } else {
                prepareStatement.setInt(1, label.getPriority());
            }
            prepareStatement.setInt(2, label.getId());

            return prepareStatement.executeUpdate();
        }
    }



}
