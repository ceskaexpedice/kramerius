//$Id: ConfigException.java 6564 2008-02-07 14:06:24Z gertsp $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server.errors;

/**
 * an exception for configuration errors
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class ConfigException extends GenericSearchException {

	private static final long serialVersionUID = 1L;

    /**
     *
     * @param message An informative message explaining what happened and
     *                (possibly) how to fix it.
     */
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        
    }
    
}
