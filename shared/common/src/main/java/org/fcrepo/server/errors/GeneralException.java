/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.errors;

/**
 * A general exception indicating something went wrong on the server.
 * 
 * <p>This type of exception doesn't characterize the error by java type, but 
 * may still classify it by message and code.
 * 
 * @author Chris Wilper
 */
public final class GeneralException
        extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a GeneralException.
     * 
     * @param message
     *        An informative message explaining what happened and (possibly) how
     *        to fix it.
     */
    public GeneralException(String message) {
        super( message);
    }

    public GeneralException(String message, Throwable cause) {
        super( message, cause);
    }

    public GeneralException(String bundleName,
                            String code,
                            String[] values,
                            String[] details,
                            Throwable cause) {
        super( code,  cause);
    }

}
