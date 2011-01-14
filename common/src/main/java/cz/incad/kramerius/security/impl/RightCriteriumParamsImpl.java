/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.security.impl;

import cz.incad.kramerius.security.RightCriteriumParams;

public class RightCriteriumParamsImpl implements RightCriteriumParams {

    private int criteriumParamId;
    private Object[] objects;
    private String longDesc;
    private String shortDesc;
    
    public RightCriteriumParamsImpl(int criteriumParamId) {
        super();
        this.criteriumParamId = criteriumParamId;
    }

    @Override
    public int getId() {
        return this.criteriumParamId;
    }

    @Override
    public Object[] getObjects() {
        return this.objects;
    }

    @Override
    public void setObjects(Object[] objs) {
        this.objects = objs;
    }

    @Override
    public String getLongDescription() {
        return this.longDesc;
    }

    @Override
    public void setLongDescription(String longDesc) {
        this.longDesc = longDesc;
    }

    @Override
    public String getShortDescription() {
        return this.shortDesc;
    }

    @Override
    public void setShortDescription(String desc) {
        this.shortDesc = desc;
    }

    @Override
    public void setId(int id) {
        this.criteriumParamId = id;
    }
}
