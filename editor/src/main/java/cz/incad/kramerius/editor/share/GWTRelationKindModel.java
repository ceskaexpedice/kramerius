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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

/**
 *
 * @author Jan Pokorsky
 */
public final class GWTRelationKindModel implements HasValueChangeHandlers<GWTRelationKindModel> {

    private final GWTKrameriusObject krameriusObject;
    private final GWTKrameriusObject.Kind relKind;
    private final HandlerManager handlerManager;
    private List<GWTKrameriusObject> relations;
    private boolean modified = false;

    private static GWTKrameriusObject initClipboard() {
        EnumMap<Kind, List<GWTKrameriusObject>> map = new EnumMap<Kind, List<GWTKrameriusObject>>(Kind.class);
        map.put(Kind.ALL, new ArrayList<GWTKrameriusObject>());
        return new GWTKrameriusObject("clipboard", Kind.ALL, "Clipboard", map);
    }

    public GWTRelationKindModel() {
        this(initClipboard(), Kind.ALL);
    }

    GWTRelationKindModel(GWTKrameriusObject krameriusObject, Kind relKind) {
        this.krameriusObject = krameriusObject;
        this.relKind = relKind;
        this.relations = Collections.unmodifiableList(krameriusObject.getRelations(relKind));
        this.handlerManager = new HandlerManager(this);
    }

    public boolean isModified() {
        return this.modified;
    }

    public List<GWTKrameriusObject> getRelations() {
        return this.relations;
    }

    public void setRelations(List<GWTKrameriusObject> relations) {
        boolean _modified = isModified(relations);
        if (_modified) {
            this.relations = Collections.unmodifiableList(relations);
        } else {
            this.relations = Collections.unmodifiableList(this.krameriusObject.getRelations(this.relKind));
        }
        setModified(_modified);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<GWTRelationKindModel> handler) {
        return this.handlerManager.addHandler(ValueChangeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    void setModified(boolean modified) {
        this.modified = modified;
        ValueChangeEvent.fire(this, this);
    }

    private boolean isModified(List<GWTKrameriusObject> newRels) {
        return !this.krameriusObject.getRelations(this.relKind).equals(newRels);
    }
}
