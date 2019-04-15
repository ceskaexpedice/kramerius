/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.errors;

/**
 * Abstract superclass for storage-related exceptions.
 * 
 * @author Chris Wilper
 */
public abstract class StorageException
        extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a StorageException.
     * 
     * @param message
     *        An informative message explaining what happened and (possibly) how
     *        to fix it.
     */
    public StorageException(String message) {
        super( message);
    }

    public StorageException(String message, Throwable cause) {
        super( message, cause);
    }

    public StorageException(String bundleName,
                            String code,
                            String[] values,
                            String[] details,
                            Throwable cause) {
        super(code,  cause);
    }

}
