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

package cz.incad.kramerius.editor.server;

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.editor.share.GWTKrameriusObject;
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;
import cz.incad.kramerius.relation.Relation;
import cz.incad.kramerius.relation.RelationUtils;
import cz.incad.kramerius.relation.RelationModel;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jan Pokorsky
 */
public class HandlerTestUtils {

    public static GWTKrameriusObjectBuilder createGWTObject(String pid, Kind kind, String title) {
        return new GWTKrameriusObjectBuilder(
                new GWTKrameriusObject(
                        pid, kind, title,
                        new EnumMap<Kind, List<GWTKrameriusObject>>(Kind.class)
                        ));
    }
    public static GWTKrameriusObject createGWTObject(String pid, Kind kind, String title,
            List<GWTKrameriusObject>... relations) {

        Map<Kind, List<GWTKrameriusObject>> relsMap =
                new EnumMap<Kind, List<GWTKrameriusObject>>(Kind.class);

        for (int i = 0; i < relations.length; i++) {
            List<GWTKrameriusObject> kindRelations = relations[i];
            relsMap.put(kindRelations.get(0).getKind(), kindRelations);
        }
        return new GWTKrameriusObject(pid, kind, title, null);
    }

    public static List<GWTKrameriusObject> createGWTRelations(Kind relKind, String... relParams) {
        if (relParams.length % 2 != 0) {
            throw new IllegalArgumentException("wrong params; expected format is relKind, [pid,title]*");
        }

        List<GWTKrameriusObject> objs = new ArrayList<GWTKrameriusObject>(relParams.length / 2);
        for (int i = 0, length = relParams.length, pos = 1; i < length; pos++) {
            String pid = relParams[i++];
            String title = relParams[i++];
            objs.add(new GWTKrameriusObject(pid, relKind, pos, title));
        }
        return objs;
    }

    public static final class GWTKrameriusObjectBuilder {

        private GWTKrameriusObject gko;

        public GWTKrameriusObjectBuilder(String pid, Kind kind, String title) {
            this(new GWTKrameriusObject(pid, kind, title,
                    new EnumMap<Kind, List<GWTKrameriusObject>>(Kind.class)));
        }

        private GWTKrameriusObjectBuilder(GWTKrameriusObject gko) {
            this.gko = gko;
        }

        public GWTKrameriusObjectBuilder addRelations(Kind relKind, String... relParams) {
            gko.setRelations(relKind, createGWTRelations(relKind, relParams));
            return this;
        }

        public GWTKrameriusObject toInstance() {
            return gko;
        }

    }

    public static final class RelationModelBuilder {
        private RelationModel model;

        public RelationModelBuilder(String pid, KrameriusModels kind) {
            this(RelationUtils.emptyModel(pid, kind));
        }

        private RelationModelBuilder(RelationModel model) {
            this.model = model;
        }

        public RelationModelBuilder addRelations(KrameriusModels relKind, String... pids) {
            model.addRelationKind(relKind);
            List<Relation> relations = model.getRelations(relKind);
            for (int i = 0; i < pids.length; i++) {
                String pid = pids[i];
                relations.add(new Relation(pid, relKind));
            }
            return this;
        }

        public RelationModel toInstance() {
            return model;
        }
    }

}