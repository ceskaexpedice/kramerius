/*
 * Copyright (C) 2025  Inovatika
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
package cz.incad.kramerius.rest.oai.record;

/**
 * SupplementType defines the types of supplemental data
 * that can be attached to a record in the OAI export process.
 */
public enum SupplementType {

    /**
     * Indicates that the supplement contains first pid
     */
    REPRESENTATIVE_PAGE_PID,

    /**
     * Indicates that the supplement contains root_pid
     */
    ROOT_PID,

    /**
     * Indicates that the supplement contains onw_pid_path
     */
    OWN_PID_PATH,

    //own_parent.pid
    OWN_PARENT_PID,

    /**
     * Mimetype of the repre page
     */
    REPRESENTATIVE_PAGE_MIME_TYPE,

    /**
     * CDK Collections if OAI is running on CDK side
     */
    CDK_COLLECTIONS;

}
