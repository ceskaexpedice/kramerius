/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.errors;

/**
 * Signals that a PID is malformed.
 * 
 * @author Chris Wilper
 */
public class MalformedPidException
        extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a MalformedPIDException.
     * 
     * @param message
     *        An informative message explaining what happened and (possibly) how
     *        to fix it.
     */
    public MalformedPidException(String message) {
        super( message);
    }

    public MalformedPidException(String message, Throwable cause) {
        super( message,cause);
    }

    public MalformedPidException(String bundleName,
                                 String code,
                                 String[] values,
                                 String[] details,
                                 Throwable cause) {
        super( code,  cause);
    }

}
