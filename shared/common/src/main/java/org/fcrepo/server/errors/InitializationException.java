/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.errors;

/**
 * Superclass for initialization-related exceptions.
 * 
 * @author Chris Wilper
 */
public class InitializationException
        extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates an InitializationException.
     * 
     * @param message
     *        An informative message explaining what happened and (possibly) how
     *        to fix it.
     */
    public InitializationException(String message) {
        super( message);
    }

    public InitializationException(String message, Throwable cause) {
        super( message,cause);
    }

    public InitializationException(String bundleName,
                                   String code,
                                   String[] replacements,
                                   String[] details,
                                   Throwable cause) {
        super( code,cause);
    }

}
