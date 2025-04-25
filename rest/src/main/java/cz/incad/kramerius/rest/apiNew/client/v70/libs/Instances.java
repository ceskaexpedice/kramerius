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
 package cz.incad.kramerius.rest.apiNew.client.v70.libs;

import java.util.List;

/**
 * Interface representing a collection of digital library instances.
 * Provides methods to retrieve and manage instances, including filtering
 * by status (enabled/disabled) and finding specific instances by their acronym.
 */
public interface Instances {

    /**
     * Retrieves a list of all instances
     * @return A list of all instances.
     */
    public List<OneInstance> allInstances();

    /**
     * Retrieves a list of all enabled instances.
     * Enabled instances are those that are currently active and available for use.
     * @return A list of enabled instances.
     */
    public List<OneInstance> enabledInstances();

    /**
     * Retrieves a list of all disabled instances.
     * Disabled instances are those that are currently inactive or unavailable.
     * @return A list of disabled instances.
     */
    public List<OneInstance> disabledInstances();



    /**
     * Finds a specific instance by its acronym.
     * The acronym is a unique identifier for each instance.
     *
     * @param acronym The acronym of the instance to find.
     * @return The instance with the specified acronym, or null if not found.
     */
    public OneInstance find(String acronym);


    /**
     * Checks if any instances are currently disabled.
     * @return True if at least one instance is disabled, false otherwise.
     */
    public boolean isAnyDisabled();

    /**
     * Checks if a specific instance is enabled.
     * @param acronym The acronym of the instance to check.
     * @return True if the instance is enabled, false otherwise.
     */
    public boolean isEnabledInstance(String acronym);

    /** reload all items */
    public void refresh();

    public void cronRefresh();

}
