package org.kramerius;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class ReplicationRights {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ReplicationRights rr = new ReplicationRights();
		rr.setRights();

	}

	Logger log = Logger.getLogger(ReplicationRights.class.getName());

	Connection conn = null;

	public void setRights() {
		initDB();
		try {
			String sigla = KConfiguration.getInstance().getProperty("k3.replication.sigla");
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select id from institution where sigla = \'"+ sigla + "\'");
			if (rs.next()) {
				int id = rs.getInt(1);
				log.info("SIGLA:" + sigla + ", id:" + id);

				int updated = st.executeUpdate("insert into replicationright select distinct id_cc, "+ id + ", 1 from m_monograph; ");
				log.info("Updated monographs: " + updated);
				updated = st.executeUpdate("insert into replicationright select distinct id_cc, "+ id + ", 1 from p_periodical; ");
				log.info("Updated periodicals: " + updated);
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
