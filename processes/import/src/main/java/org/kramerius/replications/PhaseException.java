/*
 * Copyright (C) 2012 Pavel Stastny
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
package org.kramerius.replications;

public class PhaseException extends Exception {

    private Phase phase;
    
    public PhaseException(Phase phase) {
        super();
        this.phase = phase;
    }

    public PhaseException(Phase phase, String message, Throwable cause) {
        super(message, cause);
        this.phase = phase;
    }

    public PhaseException(Phase phase, String message) {
        super(message);
        this.phase = phase;
    }

    public PhaseException(Phase phase, Throwable cause) {
        super(cause);
        this.phase = phase;
    }
    
    /**
     * @return the phase
     */
    public Phase getPhase() {
        return phase;
    }
    
}
