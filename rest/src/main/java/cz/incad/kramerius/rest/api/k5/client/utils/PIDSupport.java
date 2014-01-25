/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.api.k5.client.utils;

public class PIDSupport {

	public static boolean isComposedPID(String pid) {
		if (pid.contains("@")) {
			return true;
		} else return false;
	}

	public static String rest(String pid) {
		if (isComposedPID(pid)) {
			return pid.substring(pid.indexOf('@')+1);
		} else {
			return "";
		}
	}

	public static String first(String pid) {
		if (isComposedPID(pid)) {
			return pid.substring(0, pid.indexOf('@'));
		} else {
			return pid;
		}
	}

}
