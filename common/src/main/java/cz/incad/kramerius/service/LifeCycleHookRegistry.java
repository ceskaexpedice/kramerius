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
package cz.incad.kramerius.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

public class LifeCycleHookRegistry {

	public static Logger LOGGER = Logger.getLogger(LifeCycleHookRegistry.class.getName());
	
	private List<LifeCycleHook> hooks = new ArrayList<LifeCycleHook>();
	
	@Inject
	public LifeCycleHookRegistry(Set<LifeCycleHook> hooks) {
		for (LifeCycleHook sh : hooks) {
			this.hooks.add(sh);
		}
	}
	
	public void shutdownNotification() {
		for (LifeCycleHook sh : this.hooks) {
			LOGGER.info("shutdown inform :"+sh.getClass().getName());
			sh.shutdownNotification();
		}
	}

	public void startNotification() {
		for (LifeCycleHook sh : this.hooks) {
			LOGGER.info("startup inform :"+sh.getClass().getName());
			sh.startNotification();
		}
	}
}
