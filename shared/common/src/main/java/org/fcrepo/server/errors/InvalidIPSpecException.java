/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.errors;

/**
 * Thrown when when an IP is bad.
 * 
 * @author Chris Wilper
 */
public class InvalidIPSpecException
        extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an InvalidIPSpecException.
     * 
     * @param message
     *        An informative message explaining what happened and (possibly) how
     *        to fix it.
     */
    public InvalidIPSpecException(String message) {
        super( message);
    }

    public InvalidIPSpecException(String bundleName,
                                  String code,
                                  String[] replacements,
                                  String[] details,
                                  Throwable cause) {
        super( code,  cause);
    }

}
