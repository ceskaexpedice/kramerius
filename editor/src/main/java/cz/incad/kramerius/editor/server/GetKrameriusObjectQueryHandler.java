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
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;
import cz.incad.kramerius.editor.share.rpc.GetKrameriusObjectQuery;
import cz.incad.kramerius.editor.share.rpc.GetKrameriusObjectResult;
import cz.incad.kramerius.editor.share.rpc.GetKrameriusObjectResult.Descriptor;
import cz.incad.kramerius.relation.Relation;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationService;
import java.io.IOException;
import java.util.HashMap;
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
public final class GetKrameriusObjectQueryHandler implements ActionHandler<GetKrameriusObjectQuery, GetKrameriusObjectResult> {

    private RelationService relationsDAO;
    private FedoraAccess fedoraAccess;
    private RemoteServices remotes;

    @Inject
    public GetKrameriusObjectQueryHandler(
            RelationService dao,
            RemoteServices remotes,
            @Named("rawFedoraAccess") FedoraAccess fedoraAccess) {

        this.relationsDAO = dao;
        this.fedoraAccess = fedoraAccess;
        this.remotes = remotes;
    }

    @Override
    public Class<GetKrameriusObjectQuery> getActionType() {
        return GetKrameriusObjectQuery.class;
    }

    @Override
    public GetKrameriusObjectResult execute(GetKrameriusObjectQuery action, ExecutionContext context) throws DispatchException {
        String pidTxt = action.getPID();
        pidTxt = EditorServerUtils.validatePID(pidTxt);
        RelationModel fetchedRelations;
        try {
            fetchedRelations = fetchRelations(pidTxt);
        } catch (IOException ex) {
            // XXX implement better error handling to notify client what is wrong
            Logger.getLogger(GetKrameriusObjectQueryHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new ActionException(ex);
        }
        Map<String, String> fetchedTitles = fetchTitles(pidTxt, fetchedRelations);
        GetKrameriusObjectResult result = buildResult(pidTxt, fetchedRelations, fetchedTitles);
        return result;
    }

    @Override
    public void rollback(GetKrameriusObjectQuery action, GetKrameriusObjectResult result, ExecutionContext context) throws DispatchException {
        throw new UnsupportedOperationException("Not supported.");
    }

    private GetKrameriusObjectResult buildResult(
            String pid, RelationModel rels, Map<String, String> names) {

        Descriptor objDescriptor = new Descriptor(pid, names.get(pid));
        Kind objKind = EditorServerUtils.resolveKind(rels.getKind());
        RelationResult relResult = buildRelationsResult(rels, names);
        GetKrameriusObjectResult result = new GetKrameriusObjectResult(
                objDescriptor, objKind, relResult.relKinds, relResult.relDescriptors);
        return result;
    }

    private RelationResult buildRelationsResult(RelationModel rels, Map<String, String> names) {
        Kind[] relKinds = new Kind[rels.getRelationKinds().size()];
        Descriptor[][] relDescriptors = new Descriptor[relKinds.length][];
        int kindIdx = 0;
        for (KrameriusModels relationKind : rels.getRelationKinds()) {
            List<Relation> kindRelations = rels.getRelations(relationKind);
            Descriptor[] kindRelationDesriptors = new Descriptor[kindRelations.size()];
            relDescriptors[kindIdx] = kindRelationDesriptors;
            relKinds[kindIdx] = EditorServerUtils.resolveKind(relationKind);
            ++kindIdx;

            int relIdx = 0;
            for (Relation relation : kindRelations) {
                kindRelationDesriptors[relIdx++] = new Descriptor(
                        relation.getPID(), names.get(relation.getPID()));
            }
        }
//        result.setRelations(relKinds, relDescriptors);

        return new RelationResult(relKinds, relDescriptors);
    }

    /** helper class to pass complex result */
    private static final class RelationResult {
        Kind[] relKinds;
        Descriptor[][] relDescriptors;

        public RelationResult(Kind[] relKinds, Descriptor[][] relDescriptors) {
            this.relKinds = relKinds;
            this.relDescriptors = relDescriptors;
        }
    }

    private RelationModel fetchRelations(String pid) throws IOException {
        RelationModel rels = relationsDAO.load(pid);
        return rels;
    }

    private Map<String, String> fetchTitles(String pid, RelationModel rels) throws ActionException {
        Map<String, String> result = new HashMap<String, String>();

        result.put(pid, fetchDCName(pid));

        for (KrameriusModels relationKind : rels.getRelationKinds()) {
            for (Relation relation : rels.getRelations(relationKind)) {
                result.put(relation.getPID(), fetchDCName(relation.getPID()));
            }
        }

        return result;
    }

    private String fetchDCName(String pid) throws ActionException {
        try {
            return remotes.fetchDCName(pid);
        } catch (IOException ex) {
            Logger.getLogger(GetKrameriusObjectQueryHandler.class.getName()).log(Level.SEVERE, "pid: " + pid, ex);
            throw new ActionException("The server is out of order.");
        }
    }

}