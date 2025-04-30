/*
 * Copyright (C) 2025 Inovatika
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
package cz.incad.kramerius.workmode;

public class WorkMode {

    private boolean readOnly;
    private WorkModeReason reason;

    public WorkMode() {
        // Required for frameworks like Jackson
    }

    public WorkMode(boolean readOnly, WorkModeReason reason) {
        this.readOnly = readOnly;
        this.reason = reason;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public WorkModeReason getReason() {
        return reason;
    }

    public void setReason(WorkModeReason reason) {
        this.reason = reason;
    }
}