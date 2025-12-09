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
package cz.incad.kramerius.processes.client;

/**
 * ErrorCode
 * @author ppodsednik
 */
public enum ErrorCode {


    NOT_FOUND{
        public boolean accept(int statusCode) {
            return statusCode == 404;
        }
    },
    INVALID_INPUT {
        public boolean accept(int statusCode) {
            return statusCode == 400;
        }
    },
    INTERNAL_SERVER_ERROR {
        public boolean accept(int statusCode) {
            return statusCode != 400 && statusCode != 404;
        }

    };

    public abstract boolean accept(int statusCode);

    public static ErrorCode findByStatusCode(int statusCode) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.accept(statusCode))
                return errorCode;
        }
        return null;
    }

}
