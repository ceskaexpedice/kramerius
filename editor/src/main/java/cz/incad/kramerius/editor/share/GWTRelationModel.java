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
package cz.incad.kramerius.editor.share;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jan Pokorsky
 */
public final class GWTRelationModel implements HasValueChangeHandlers<GWTRelationModel> {

    private final GWTKrameriusObject krameriusObject;
    private final EnumMap<Kind, GWTRelationKindModel> kind2ModelMap;
    private final HandlerManager handlerManager;
    private final RelKindModelChangeHandler relKindModelHandler;

    public GWTRelationModel(GWTKrameriusObject krameriusObject) {
        this.krameriusObject = krameriusObject;
        this.handlerManager = new HandlerManager(this);
        this.relKindModelHandler = new RelKindModelChangeHandler();
        Set<Kind> kinds = krameriusObject.getRelationKinds();
        this.kind2ModelMap = new EnumMap(Kind.class);
        for(Kind kind : kinds) {
            GWTRelationKindModel relKindModel = new GWTRelationKindModel(krameriusObject, kind);
            relKindModel.addValueChangeHandler(this.relKindModelHandler);
            this.kind2ModelMap.put(kind, relKindModel);
        }
    }

    public GWTKrameriusObject getKrameriusObject() {
        return this.krameriusObject;
    }

    public GWTRelationKindModel getRelationKindModel(GWTKrameriusObject.Kind relKind) {
        return this.kind2ModelMap.get(relKind);
    }

    public Collection<Kind> getRelationKinds() {
        return this.kind2ModelMap.keySet();
    }

    public boolean isModified() {
        for (GWTRelationKindModel rkm : this.kind2ModelMap.values()) {
            if (rkm.isModified()) {
                return true;
            }
        }
        return false;
    }

    public void save() {
        try {
            relKindModelHandler.listen = false;
            Map relMap = new EnumMap(GWTKrameriusObject.Kind.class);
            for (Map.Entry entry : this.kind2ModelMap.entrySet()) {
                GWTRelationKindModel model = (GWTRelationKindModel) entry.getValue();
                relMap.put(entry.getKey(), model.getRelations());
                model.setModified(false);
            }

            this.krameriusObject.setRelations(relMap);
        } finally {
            relKindModelHandler.listen = true;
        }
        fireModifiedEvent();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<GWTRelationModel> handler) {
        return this.handlerManager.addHandler(ValueChangeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    private void fireModifiedEvent() {
        ValueChangeEvent.fire(this, this);
    }

    private final class RelKindModelChangeHandler implements ValueChangeHandler<GWTRelationKindModel> {

        private boolean listen = true;

        @Override
        public void onValueChange(ValueChangeEvent<GWTRelationKindModel> event) {
            if (this.listen) {
                GWTRelationModel.this.fireModifiedEvent();
            }
        }
    }
}
