package cz.incad.kramerius.processes.database;

/**
 * Provides connection to FEDORA 3 database 
 * @author pavels
 */
public class Fedora3ConnectionProvider extends JNDIConnectionProvider {
    
    private static final String JNDI_NAME="java:comp/env/jdbc/fedora3";

    public Fedora3ConnectionProvider() {
        super(JNDI_NAME);
    }
}
