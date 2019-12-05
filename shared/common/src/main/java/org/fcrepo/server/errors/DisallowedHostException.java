/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.errors;

/**
 * Thrown when a host requests access to a resource that it doesn't have 
 * permission to access.
 * 
 * @author Chris Wilper
 */
public class DisallowedHostException
        extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a DisallowedHostException.
     * 
     * @param message
     *        An informative message explaining what happened and (possibly) how
     *        to fix it.
     */
    public DisallowedHostException(String message) {
        super( message);
    }

    public DisallowedHostException(String bundleName,
                                   String code,
                                   String[] replacements,
                                   String[] details,
                                   Throwable cause) {
        super( code,  cause);
    }

}
