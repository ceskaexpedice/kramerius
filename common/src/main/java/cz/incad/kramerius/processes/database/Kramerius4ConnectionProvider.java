package cz.incad.kramerius.processes.database;

/**
 * Provides connection to kramerius4 database
 * @author pavels
 */
public class Kramerius4ConnectionProvider extends JNDIConnectionProvider{

    private static String JNDI_NAME="java:comp/env/jdbc/kramerius4";
    
    public Kramerius4ConnectionProvider() {
        super(JNDI_NAME);
    }
}
