package org.kramerius;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.logging.Logger;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class ReplicationRights {

    /**
     * @param args
     */
    public static void main(String[] args) {
        log.info("ReplicationRights: "+Arrays.toString(args));
        ReplicationRights rr = new ReplicationRights();
        if (args.length>0 && "reset".equalsIgnoreCase(args[0])){
            rr.resetRights();
        }else{
            rr.setRights();
        }
        log.info("ReplicationRights finished.");

    }

    static Logger log = Logger.getLogger(ReplicationRights.class.getName());

    Connection conn = null;

    public void setRights() {
        initDB();
        try {
            String sigla = KConfiguration.getInstance().getProperty("k3.replication.sigla");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select id from institution where sigla = \'"+ sigla + "\'");
            if (rs.next()) {
                int id = rs.getInt(1);
                log.info("Setting replication rights for SIGLA:" + sigla + ", id:" + id);
                int updated =st.executeUpdate("update replicationright set rright=1 where institution="+id+";");
                log.info("Updated existing rights: " + updated);
                updated = st.executeUpdate("insert into replicationright (select distinct id_cc, "+ id + ", 1 from m_monograph "
                + " where id_cc not in (select replicationright.customizablecomponent from replicationright where replicationright.institution ="+ id + ")); ");
                log.info("Inserted monographs rights: " + updated);
                updated = st.executeUpdate("insert into replicationright (select distinct id_cc, "+ id + ", 1 from p_periodical "
                +  "where id_cc not in (select replicationright.customizablecomponent from replicationright where replicationright.institution ="+ id + ")); ");
                log.info("Inserted periodicals rights: " + updated);
            }
            rs.close();
            conn.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void resetRights() {
        initDB();
        try {
            String sigla = KConfiguration.getInstance().getProperty("k3.replication.sigla");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select id from institution where sigla = \'"+ sigla + "\'");
            if (rs.next()) {
                int id = rs.getInt(1);
                log.info("Deleting replication rights for SIGLA:" + sigla + ", id:" + id);

                int updated = st.executeUpdate("delete from replicationright where institution = "+ id + " ; ");
                log.info("Deleted replication rights: " + updated);

            }
            rs.close();
            conn.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void initDB() {
        try {
            Class.forName(KConfiguration.getInstance().getProperty("k3.db.driver"));
            String url = KConfiguration.getInstance().getProperty("k3.db.url");
            String user = KConfiguration.getInstance().getProperty("k3.db.user");
            String pwd = KConfiguration.getInstance().getProperty("k3.db.password");
            conn = DriverManager.getConnection(url, user, pwd);
            conn.setAutoCommit(true);
            log.info("Database initialized.");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
