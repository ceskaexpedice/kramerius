/*
 * Copyright (C) Mar 11, 2024 Pavel Stastny
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
package cz.incad.kramerius.security.licenses.lock;

/**
 * Exclusive map item not found
 */
public class ExclusiveMapExceptionItemNotFound extends ExclusiveMapException{

    public ExclusiveMapExceptionItemNotFound() {
        super();
    }

    public ExclusiveMapExceptionItemNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public ExclusiveMapExceptionItemNotFound(String message) {
        super(message);
    }

    public ExclusiveMapExceptionItemNotFound(Throwable cause) {
        super(cause);
    }
}
