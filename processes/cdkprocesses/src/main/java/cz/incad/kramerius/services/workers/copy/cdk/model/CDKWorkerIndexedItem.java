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
package cz.incad.kramerius.services.workers.copy.cdk.model;

import cz.inovatika.kramerius.services.workers.WorkerIndexedItem;

import java.util.Map;

public class CDKWorkerIndexedItem extends WorkerIndexedItem  {

    public static final String PID_KEYWORD = "pid";

    private final String cdkLeader;
    private final String composeId;

    private String pid;

    public CDKWorkerIndexedItem(String id, Map<String, Object> document) {
        super(id, document);
        this.pid = document.containsKey(PID_KEYWORD) ?  document.get(PID_KEYWORD).toString() : null;
        this.cdkLeader =  document.containsKey("cdk.leader") ? (String) document.get("cdk.leader") : null;
        this.composeId = document.containsKey("compositeId") ? (String) document.get("compositeId") : null;
    }

    public String getCdkLeader() {
        return cdkLeader;
    }

    public String getComposeId() {
        return composeId;
    }


    public String getPid() {
        return pid;
    }
}
