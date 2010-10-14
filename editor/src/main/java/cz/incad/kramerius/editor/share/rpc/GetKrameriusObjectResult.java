/*
 * Copyright (C) 2010 Jan Pokorsky
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
package cz.incad.kramerius.editor.share.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;
import cz.incad.kramerius.editor.share.GWTKrameriusObject;
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.customware.gwt.dispatch.shared.Result;

/**
 * Result of {@link GetKrameriusObjectQuery}.
 *
 * @author Jan Pokorsky
 */
public class GetKrameriusObjectResult implements Result, IsSerializable {

    private Descriptor desc;
    private Kind kind;
    private Kind[] relKinds;
    private Descriptor[][] relations;

    /* gwt serialization purposes */
    private GetKrameriusObjectResult() {}

    public GetKrameriusObjectResult(Descriptor desc, Kind kind, Kind[] relKinds, Descriptor[][] relations) {
        if (relKinds.length != relations.length) {
            throw new IllegalArgumentException();
        }
        this.desc = desc;
        this.kind = kind;
        this.relKinds = relKinds;
        this.relations = relations;
    }

    public GWTKrameriusObject getResult() {
        Map<Kind, List<GWTKrameriusObject>> relationsMap = new EnumMap<Kind, List<GWTKrameriusObject>>(Kind.class);
        for (int i = 0; i < relKinds.length; i++) {
            Kind relKind = relKinds[i];
            Descriptor[] relDescriptors = relations[i];
            ArrayList<GWTKrameriusObject> relObjects = new ArrayList<GWTKrameriusObject>(relDescriptors.length);
            relationsMap.put(relKind, relObjects);
            int pos = 0;
            for (Descriptor rd : relDescriptors) {
                relObjects.add(new GWTKrameriusObject(rd.getUUID(), relKind, ++pos, rd.getName()));
            }
        }
        GWTKrameriusObject obj = new GWTKrameriusObject(desc.getUUID(), kind, desc.getName(), relationsMap);
        return obj;
    }

    public static class Descriptor implements IsSerializable {
        private String uuid;
        private String name;

        /* gwt serialization purposes */
        private Descriptor() {}

        public Descriptor(String uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getUUID() {
            return uuid;
        }

    }
}
