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

package cz.incad.kramerius.editor.client.presenter;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import cz.incad.kramerius.editor.client.view.ContainerView;
import cz.incad.kramerius.editor.client.view.EditorViewsFactory;
import cz.incad.kramerius.editor.client.view.RelationsView;
import cz.incad.kramerius.editor.client.view.RelationsView.RelationTab;
import cz.incad.kramerius.editor.share.GWTKrameriusObject;
import cz.incad.kramerius.editor.share.GWTRelationKindModel;
import cz.incad.kramerius.editor.share.GWTRelationModel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Jan Pokorsky
 */
public final class RelationsPresenter implements Presenter, RelationsView.Callback {

    private final RelationsView display;
    private GWTRelationModel model;
    private final RelKindModelChangeHandler relKindModelHandler;
    private Map<RelationTab, ContainerPresenter> view2PresenterMap;
    private Map<GWTRelationKindModel, RelationsView.RelationTab> model2View;
    private boolean isBound = false;
    private final EditorPresenter ebus;

    public RelationsPresenter(RelationsView display, EditorPresenter ebus) {
        this.display = display;
        this.ebus = ebus;
        this.relKindModelHandler = new RelKindModelChangeHandler();
    }

    @Override
    public RelationsView getDisplay() {
        return display;
    }

    public void setModel(GWTRelationModel model) {
        this.model = model;
    }

    public GWTRelationModel getModel() {
        return this.model;
    }

    public void bind() {
        if (isBound) {
            return;
        }
        isBound = true;

        view2PresenterMap = new LinkedHashMap<RelationTab, ContainerPresenter>();
        model2View = new HashMap<GWTRelationKindModel, RelationTab>();
        for (GWTKrameriusObject.Kind relKind : this.model.getRelationKinds()) {
            ContainerPresenter cp = new ContainerPresenter(
                    EditorViewsFactory.getInstance().createContainerView(), this.ebus);

            GWTRelationKindModel relKindModel = this.model.getRelationKindModel(relKind);
            cp.setModel(relKindModel);
            relKindModel.addValueChangeHandler(this.relKindModelHandler);

            RelationTab tab = new RelationTabImpl(relKind.toLocalizedPluralString(), null, cp.getDisplay());
            this.display.addTab(tab);
            this.view2PresenterMap.put(tab, cp);
            this.model2View.put(relKindModel, tab);
        }

        display.setCallback(this);
        if (!view2PresenterMap.isEmpty()) {
            ContainerPresenter cp = view2PresenterMap.entrySet().iterator().next().getValue();
            cp.bind();
        }
    }

    public void unbind() {
        if (isBound) {
            isBound = false;
        } else {
            return;
        }

        for (ContainerPresenter cp : this.view2PresenterMap.values()) {
            cp.unbind();
        }
    }

    @Override
    public void onTabSelection() {
        RelationTab tab = display.getSelectedTab();
        ContainerPresenter containerPresenter = view2PresenterMap.get(tab);
        containerPresenter.bind();

    }

    private static final class RelationTabImpl implements RelationsView.RelationTab {

        private final ContainerView widgetProvider;
        private final String name;
        private final String tooltip;

        public RelationTabImpl(String name, String tooltip, ContainerView widgetProvider) {
            this.tooltip = tooltip;
            this.widgetProvider = widgetProvider;
            this.name = name;
        }


        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTooltip() {
            return tooltip;
        }

        @Override
        public Widget asWidget() {
            return widgetProvider.asWidget();
        }

    }

    private final class RelKindModelChangeHandler
            implements ValueChangeHandler<GWTRelationKindModel> {

        @Override
        public void onValueChange(ValueChangeEvent<GWTRelationKindModel> event) {
            GWTRelationKindModel relKindModel = event.getValue();
            RelationTab tab = RelationsPresenter.this.model2View.get(relKindModel);
            RelationsPresenter.this.display.setModified(tab, relKindModel.isModified());
        }
    }


}