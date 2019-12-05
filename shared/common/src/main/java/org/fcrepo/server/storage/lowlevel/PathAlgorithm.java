/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.storage.lowlevel;

import org.fcrepo.common.PID;
import org.fcrepo.server.errors.LowlevelStorageException;
import org.fcrepo.server.errors.MalformedPidException;

import java.util.Map;


/**
 * @author Bill Niebel
 */
public abstract class PathAlgorithm {

    public PathAlgorithm(Map<String, ?> configuration) {
    };

    public abstract String get(String pid) throws LowlevelStorageException;

    public static String encode(String unencoded)
            throws LowlevelStorageException {
        try {
            int i = unencoded.indexOf("+");
            if (i != -1) {
                return getPID(unencoded.substring(0, i)).toFilename()
                        + unencoded.substring(i);
            } else {
                return getPID(unencoded).toFilename();
            }
        } catch (MalformedPidException e) {
            throw new LowlevelStorageException(true, e.getMessage(), e);
        }
    }

    public static String decode(String encoded) throws LowlevelStorageException {
        try {
            int i = encoded.indexOf('+');
            if (i != -1) {
                return pidFromFilename(encoded.substring(0, i))
                        .toString()
                        + encoded.substring(i);
            } else {
                return pidFromFilename(encoded).toString();
            }
        } catch (MalformedPidException e) {
            throw new LowlevelStorageException(true, e.getMessage(), e);
        }
    }

    // Wraps PID constructor, throwing a ServerException instead
    public static PID getPID(String pidString) throws MalformedPidException {
        try {
            return new PID(pidString);
        } catch (MalformedPidException e) {
            throw new MalformedPidException(e.getMessage());
        }
    }

    // Wraps PID.fromFilename, throwing a ServerException instead
    public static PID pidFromFilename(String filename)
            throws MalformedPidException {
        try {
            return PID.fromFilename(filename);
        } catch (MalformedPidException e) {
            throw new MalformedPidException(e.getMessage());
        }
    }
}
