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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.editor.share.rpc.SaveRelationsQuery;
import cz.incad.kramerius.editor.share.rpc.SaveRelationsQuery.RelationHandle;
import cz.incad.kramerius.editor.share.rpc.SaveRelationsResult;
import cz.incad.kramerius.editor.share.rpc.SaveRelationsResult.SaveRelationsState;
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;
import cz.incad.kramerius.relation.Relation;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.RelationUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 *
 * @author Jan Pokorsky
 */
public final class SaveRelationsQueryHandler implements ActionHandler<SaveRelationsQuery, SaveRelationsResult> {
    private RelationService relationsDAO;
    private RemoteServices remotes;
    private FedoraAccess fedoraAccess;

    @Inject
    public SaveRelationsQueryHandler(
            RelationService dao,
            RemoteServices remotes,
            @Named("rawFedoraAccess") FedoraAccess fedoraAccess) {

        this.relationsDAO = dao;
        this.remotes = remotes;
        this.fedoraAccess = fedoraAccess;
    }

    @Override
    public Class<SaveRelationsQuery> getActionType() {
        return SaveRelationsQuery.class;
    }

    @Override
    public SaveRelationsResult execute(SaveRelationsQuery action, ExecutionContext context) throws DispatchException {
        try {
            String pid = action.getPID();
            pid = EditorServerUtils.validatePID(pid);
            KrameriusModels kind = EditorServerUtils.resolveKrameriusModel(action.getKind());
            RelationModel model = RelationUtils.emptyModel(pid, kind);
            Map<KrameriusModels, List<Relation>> queryRelations = buildQueryRelations(action);
            applyNewRelations(model, queryRelations);
            relationsDAO.save(pid, model);
            remotes.reindex(pid);
            SaveRelationsResult result = new SaveRelationsResult(SaveRelationsState.OK);
            return result;
        } catch (IOException ex) {
            // XXX implement better error handling to notify client what is wrong
            Logger.getLogger(SaveRelationsQueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new ActionException(ex);
        }
    }

    @Override
    public void rollback(SaveRelationsQuery action, SaveRelationsResult result, ExecutionContext context) throws DispatchException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Map<KrameriusModels, List<Relation>> buildQueryRelations(SaveRelationsQuery action)
            throws ActionException {
        
        Map<KrameriusModels, List<Relation>> map = new EnumMap<KrameriusModels, List<Relation>>(KrameriusModels.class);
        Kind[] relKinds = action.getRelKinds();
        RelationHandle[][] relationHandles = action.getRelations();

        for (int kindIdx = 0; kindIdx < relKinds.length; kindIdx++) {
            Kind relKind = relKinds[kindIdx];
            RelationHandle[] kindRelationHandles = relationHandles[kindIdx];
            List<Relation> kindRelations = new ArrayList<Relation>(kindRelationHandles.length);
            map.put(EditorServerUtils.resolveKrameriusModel(relKind), kindRelations);

            for (RelationHandle handle : kindRelationHandles) {
                kindRelations.add(buildRelation(handle));
            }
        }

        return map;
    }

    private Relation buildRelation(RelationHandle handle) throws ActionException {
        return new Relation(
                EditorServerUtils.validatePID(handle.getPID(), true),
                EditorServerUtils.resolveKrameriusModel(handle.getKind()));
    }

    private void applyNewRelations(RelationModel model, Map<KrameriusModels, List<Relation>> queryRelations) {
        for (Map.Entry<KrameriusModels, List<Relation>> entry : queryRelations.entrySet()) {
            model.addRelationKind(entry.getKey());
            model.getRelations(entry.getKey()).addAll(entry.getValue());
        }
    }

}