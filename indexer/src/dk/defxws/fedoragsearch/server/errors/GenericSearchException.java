//$Id: GenericSearchException.java 6564 2008-02-07 14:06:24Z gertsp $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server.errors;

/**
 * the most general exception for the search service
 * 
 * @author  gsp@dtv.dk
 * @version 
 */
public class GenericSearchException extends java.rmi.RemoteException {

	private static final long serialVersionUID = 1L;

    /**
     *
     * @param message An informative message explaining what happened and
     *                (possibly) how to fix it.
     */
    public GenericSearchException(String message) {
        super(message);
    }
    
    public GenericSearchException(String message, Throwable cause) {
        super(message, cause);
    }
    
}