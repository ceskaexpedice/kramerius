package org.kramerius;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


import cz.incad.kramerius.utils.conf.KConfiguration;

public class Enumerator {
    
    public static void main(String[] args) {
    	log.info("Enumerator: "+Arrays.toString(args));
        Enumerator en = new Enumerator();
        en.getMonographList();
        en.getPeriodicalList();
        log.info("Enumerator finished.");
    }

    static Logger log = Logger.getLogger(Enumerator.class.getName());

    Connection conn = null;

    public void getMonographList() {
        initDB();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select id from m_monograph where visible = true order by id");
            Writer wr = new FileWriter(KConfiguration.getInstance().getProperty("migration.monographs"));
            while (rs.next()) {
                int id = rs.getInt(1);
                wr.append(Integer.toString(id));
                wr.append("\n");
            }
            wr.close();
            rs.close();
            conn.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void getPeriodicalList() {
        initDB();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select issn from p_periodical where visible = true order by issn");
            Writer wr = new FileWriter(KConfiguration.getInstance().getProperty("migration.periodicals"));
            while (rs.next()) {
                String issn = rs.getString(1);
                try{
	                List<String> volumes = getVolumeList(issn);
	                for (String volume : volumes){
	                    wr.append(issn).append(';').append(volume);
	                    wr.append("\n");
	                }
                }catch(Exception e){
                	log.severe("Error getting issue list for issn "+issn+" (reason: "+e+")");
                	wr.append("\nError getting issue list for issn "+issn+" (reason: "+e+")\n\n");
                }
            }
            wr.close();
            rs.close();
            conn.close();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // "jdbc:postgresql://localhost:5432/kramerius", "kramerius", "f8TasR"
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

    private List<String> getVolumeList(String docId) {

        Download.Replication repl = Download.createReplication(Download.DocType.ISSN, docId, null);
        Download.ReplicationURL fromURL = new Download.ReplicationURL(repl, Download.ReplicationURL.ACTION_PERIODICAL_VOLUME_LIST);
        BufferedReader br = new BufferedReader(new InputStreamReader(Download.getRemoteInputStream(fromURL.toString())));
        String line = null;
        List<String> items = new ArrayList<String>();
        try {
            while ((line = br.readLine()) != null) {
                items.add(line);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return items;

    }

}
