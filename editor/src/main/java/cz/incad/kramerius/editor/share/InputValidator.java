/*
 * Copyright (C) 2010 Jan Pokorsky
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package cz.incad.kramerius.editor.share;

import com.google.gwt.core.client.GWT;

/**
 * Helper class to validate input coming from the outside of the application (user,
 * request parameters, ...).
 *
 * @author Jan Pokorsky
 */
public final class InputValidator {
    
    public interface Validator<T> {
        boolean isValid();
        T getNormalized();
        String getErrorMessage();
    }

    public static Validator<String> validatePID(final String input) {
        return new Validator<String>() {
            
            private static final String ERR_INVALID_PID = "Please enter a valid PID (uuid:<UUID>).";

            private String normalized;
            private Boolean valid;
            private String err;

            @Override
            public boolean isValid() {
                String pid = input;
                if (valid != null) {
                    return valid;
                }
                if (pid == null || pid.length() == 0) {
                    fetchErrorMessage(ERR_INVALID_PID);
                    return false;
                }
                pid = pid.trim();
                // https://wiki.duraspace.org/display/FCR30/Fedora+Identifiers
                if (pid.matches("^[Uu]{2}[Ii][Dd]:(([A-Za-z0-9])|-|\\.|~|_|(%[0-9A-F]{2}))+$")) {
                    // for now we support only UUID as PID
//                if (pid.matches("^([A-Za-z0-9]|-|\\.)+:(([A-Za-z0-9])|-|\\.|~|_|(%[0-9A-F]{2}))+$") {
                    if (!isValidUUID(pid)) {
                        fetchErrorMessage(ERR_INVALID_PID);
                        return false;
                    }
                    normalized = pid;
                    return true;
                } else {
                    fetchErrorMessage(ERR_INVALID_PID);
                    return false;
                }

            }

            private boolean isValidUUID(String pid) {
                try {
//                    UUID uuid = UUID.fromString(pid.substring("uuid:".length()));
//                    return uuid != null;
                    hackUUIDformString(pid.substring("uuid:".length()));
                    return true;
                } catch (Exception ex) {
                    fetchErrorMessage(ERR_INVALID_PID);
                }
                return false;
            }

            @Override
            public String getErrorMessage() {
                return err;
            }

            private void fetchErrorMessage(String code) {
                if (GWT.isClient()) {
                    err = code; // XXX implement I18N
                } else {
                    err = code;
                }
            }

            @Override
            public String getNormalized() {
                isValid();
                return normalized;
            }
        };
    }

    /** replacement for UUID.fromString as it is unsupported by GWT yet */
    private static void hackUUIDformString(String pid) {
        String[] components = pid.split("-");
        if (components.length != 5)
            throw new IllegalArgumentException("Invalid UUID string: "+pid);
        for (int i=0; i<5; i++)
            components[i] = "0x"+components[i];

        long mostSigBits = Long.decode(components[0]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]).longValue();
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]).longValue();

        long leastSigBits = Long.decode(components[3]).longValue();
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]).longValue();
    }

}
