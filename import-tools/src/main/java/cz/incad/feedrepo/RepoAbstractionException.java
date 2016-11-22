/*
 * Copyright (C) 2016 Pavel Stastny
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

package cz.incad.feedrepo;

/**
 * Represents a repo operation error
 * @author pavels
 */
public class RepoAbstractionException extends Exception {

    public RepoAbstractionException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public RepoAbstractionException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public RepoAbstractionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public RepoAbstractionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public RepoAbstractionException(Throwable cause) {
        super(cause);
    }
}
