package cz.incad.kramerius.rest.apiNew;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.DatabaseUtils;
import cz.incad.kramerius.utils.database.JDBCCommand;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCTransactionTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Access to database table CONFIG(KEY:STRING,VALUE:STRING)
 * TODO: maybe move to different module
 */
public class ConfigManager {

    public static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

    @Inject
    @Named("kramerius4")
    private Provider<Connection> connectionProvider;

    
    public List<String> getKeysByRegularExpression(String regexp) {
        List<String> keys = new JDBCQueryTemplate<String>(this.connectionProvider.get(), true) {
            @Override
            public boolean handleRow(ResultSet rs, List<String> returnsList)
                    throws SQLException {
                String key = rs.getString("key");
                returnsList.add(key);
                return true;
            }
        }.executeQuery("SELECT key FROM config WHERE key ~ ? ",regexp);
        return keys;
    }

    
    public String getProperty(String key) {
        Connection conn = connectionProvider.get();
        if (conn == null) {
            throw new RuntimeException("connection not ready");
        }
        try {
            String SQL = "SELECT value FROM config WHERE key=?;";
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            DatabaseUtils.tryClose(conn);
        }
    }

    public void deleteProperty(String key) {
        try {
            JDBCTransactionTemplate transactions = new JDBCTransactionTemplate(connectionProvider.get(), true);
            List<JDBCCommand> commands = new ArrayList<>();
            commands.add(new JDBCCommand() {
                
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    PreparedStatement prepareStatement = con.prepareStatement("delete from CONFIG where key = ?");
                    prepareStatement.setString(1, key);
                    return prepareStatement.executeUpdate();
                }
            });
            transactions.updateWithTransaction(commands);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    
    
    public Map<String,String> getProperties(Set<String> keys) {
        String in = "("+keys.stream().map(k-> {return "'"+k+"'";}).collect(Collectors.joining(","))+")";

        List<Pair<String, String>> pairs = new JDBCQueryTemplate<Pair<String,String>>(this.connectionProvider.get(), true) {
            @Override
            public boolean handleRow(ResultSet rs, List<Pair<String,String>> returnsList)
                    throws SQLException {
                String key = rs.getString("key");
                String value = rs.getString("value");
                returnsList.add(Pair.of(key, value));
                return true;
            }
        }.executeQuery(String.format("SELECT * FROM config WHERE key IN %s ",in));
        
        Map<String,String> map = new HashMap<>();
        pairs.stream().forEach(p-> {
            map.put(p.getKey(), p.getValue());
        });
        return map;
    }
    
    
    public void deleteProperties(Set<String> keys) {
        try {
            JDBCTransactionTemplate transactions = new JDBCTransactionTemplate(connectionProvider.get(), true);
            List<JDBCCommand> commands = new ArrayList<>();
            
            
            keys.forEach(key-> {
                commands.add(new JDBCCommand() {
                    @Override
                    public Object executeJDBCCommand(Connection con) throws SQLException {
                        // update 
                        String SQL = "DELETE FROM CONFIG WHERE key=?;";
                        PreparedStatement pstmt = con.prepareStatement(SQL);
                        pstmt.setString(1, key);
                        pstmt.executeUpdate();
                        return null;
                    }
                });
            });
            transactions.updateWithTransaction(commands);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    
    
    
        
    public void setProperties(Map<String,String> props) {
        try {
            JDBCTransactionTemplate transactions = new JDBCTransactionTemplate(connectionProvider.get(), true);
            List<JDBCCommand> commands = new ArrayList<>();
            commands.add(new JDBCCommand() {
                
                
                @Override
                public Object executeJDBCCommand(Connection con) throws SQLException {
                    List<String> keys = new JDBCQueryTemplate<String>(con,false) {
                        @Override
                        public boolean handleRow(ResultSet rs, List<String> returnsList) throws SQLException {
                            return returnsList.add(rs.getString("key"));
                        }
                    }.executeQuery("select key from config");
                    return keys;
                }
            });
            
            props.keySet().forEach(key-> {
                commands.add(new JDBCCommand() {
                    
                    @Override
                    public Object executeJDBCCommand(Connection con) throws SQLException {
                        List<String> keys = (List<String>) getPreviousResult();
                        if (keys.contains(key)) {
                            // update 
                            String SQL = "UPDATE config SET value=? WHERE key=?;";
                            PreparedStatement pstmt = con.prepareStatement(SQL);
                            pstmt.setString(1, props.get(key));
                            pstmt.setString(2, key);
                            pstmt.executeUpdate();
                        } else {
                            // insert 
                            String SQL = "INSERT INTO config(key, value) VALUES (?,?)";
                            PreparedStatement pstmt = con.prepareStatement(SQL);
                            pstmt.setString(1, key);
                            pstmt.setString(2, props.get(key));
                            pstmt.executeUpdate();
                        }
                        return keys;
                    }
                });
            });
            
            transactions.updateWithTransaction(commands);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        
    }

    public void setProperty(String key, String value) {
        Connection conn = connectionProvider.get();
        if (conn == null) {
            throw new RuntimeException("connection not ready");
        }
        try {
            String currentValue = getProperty(key);
            if (currentValue == null) {
                String SQL = "INSERT INTO config(key, value) VALUES (?,?)";
                PreparedStatement pstmt = conn.prepareStatement(SQL);
                int index = 1;
                pstmt.setString(index++, key);
                pstmt.setString(index++, value);
                pstmt.executeUpdate();
            } else {
                String SQL = "UPDATE config SET value=? WHERE key=?;";
                PreparedStatement pstmt = conn.prepareStatement(SQL);
                int index = 1;
                pstmt.setString(index++, value);
                pstmt.setString(index++, key);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            DatabaseUtils.tryClose(conn);
        }
    }

}
