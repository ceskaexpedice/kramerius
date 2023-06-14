/*
 * Copyright (C) Jun 8, 2023 Pavel Stastny
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
package cz.incad.kramerius.security.licenses.impl;

import cz.incad.kramerius.security.licenses.License;

import java.io.Serializable;

public class LicenseImpl implements License, Serializable {

    
    private int priorityHint = -1;
    
    private int id = -1;
    private String name;
    private String group;
    private String description;
    private int labelPrirority = DEFAULT_PRIORITY;
    
    
    public LicenseImpl(int id, String name, String description, String group, int labelPrirority) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.group = group;
        this.labelPrirority = labelPrirority;
    }

    public LicenseImpl(String name, String description, String group, int labelPrirority) {
        this.name = name;
        this.description = description;
        this.group = group;
        this.labelPrirority = labelPrirority;
    }

    public LicenseImpl(String name, String description, String group, int labelPrirority, int priorityHint) {
        this.name = name;
        this.description = description;
        this.group = group;
        this.labelPrirority = labelPrirority;
        this.priorityHint = priorityHint;
    }
    
    
    public LicenseImpl(String name, String description, String group) {
        this.description = description;
        this.name = name;
        this.group = group;
        //validateName(name);
    }

    public LicenseImpl(int id, String name, String description, String group) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.group = group;
        //validateName(name);
    }

    private void validateName(String name) {
        if (!License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher(name).matches()) {
            throw new IllegalArgumentException("Label name must contain only a characters, digits and following set of chars [./-_:]");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getPriority() {
        return this.labelPrirority;
    }

    @Override
    public License getUpdatedPriorityLabel(int pr) {
        return new LicenseImpl(this.id, this.name, this.description, this.group, pr);
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getGroup() {
        return group;
    }


    @Override
    public String getDescription() {
        return this.description;
    }

    
    @Override
    public int getPriorityHint() {
        return this.priorityHint;
    }

    @Override
    public String toString() {
        if (this.id != -1) {
            return "LabelImpl{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", group='" + group + '\'' +
                    ", labelPrirority=" + labelPrirority +
                    '}';

        } else {
            return "LabelImpl{" +
                    ", name='" + name + '\'' +
                    ", group='" + group + '\'' +
                    ", labelPrirority=" + labelPrirority +
                    '}';
        }
    }
}
