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
import cz.incad.kramerius.editor.share.GWTRelationKindModel;
import cz.incad.kramerius.editor.share.GWTRelationModel;
import java.util.Collection;
import java.util.List;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Saves relations of Kramerius object.
 *
 * @author Jan Pokorsky
 */
public class SaveRelationsQuery implements Action<SaveRelationsResult> {

    public static class RelationHandle implements IsSerializable {
        private String pid;
        private Kind kind;

        /* gwt serialization purposes */
        private RelationHandle() {}

        public RelationHandle(String pid, Kind kind) {
            this.pid = pid;
            this.kind = kind;
        }

        public Kind getKind() {
            return kind;
        }

        public String getPID() {
            return pid;
        }

    }

    private String pid;
    private Kind kind;
    private Kind[] relKinds;
    private RelationHandle[][] relations;

    /* gwt serialization purposes */
    private SaveRelationsQuery() {}

    public SaveRelationsQuery(GWTRelationModel relModel) {
        this.pid = relModel.getKrameriusObject().getPID();
        this.kind = relModel.getKrameriusObject().getKind();
        Collection<Kind> relModelKinds = relModel.getRelationKinds();

        this.relKinds = new Kind[relModelKinds.size()];
        this.relations = new RelationHandle[this.relKinds.length][];
        int kindIdx = 0;
        for (Kind relModelKind : relModelKinds) {
            this.relKinds[kindIdx] = relModelKind;
            GWTRelationKindModel relKindModel = relModel.getRelationKindModel(relModelKind);
            List<GWTKrameriusObject> relObjects = relKindModel.getRelations();
            RelationHandle[] relHandles = new RelationHandle[relObjects.size()];
            this.relations[kindIdx++] = relHandles;

            int relIdx = 0;
            for (GWTKrameriusObject relObject : relObjects) {
                relHandles[relIdx++] = new RelationHandle(relObject.getPID(), relObject.getKind());
            }
        }
    }

    public String getPID() {
        return pid;
    }

    public Kind getKind() {
        return kind;
    }

    public Kind[] getRelKinds() {
        return relKinds;
    }

    public RelationHandle[][] getRelations() {
        return relations;
    }

}