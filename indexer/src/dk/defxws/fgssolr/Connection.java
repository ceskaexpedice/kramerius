//$Id: Connection.java 6565 2008-02-07 14:53:30Z gertsp $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fgssolr;

import org.apache.log4j.Logger;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException;

/**
 * connects to the Solr index 
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class Connection {
    
    private static final Logger logger = Logger.getLogger(Connection.class);
    
    /**
     */
    public Connection() throws GenericSearchException {
        init();
    }
    
    private void init() throws GenericSearchException {
    }
    
    protected Statement createStatement() {
        Statement statement = new Statement();
        return statement; 
    }
    
}
