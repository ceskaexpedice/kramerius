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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.inject.Inject;
import cz.incad.kramerius.editor.client.view.ContainerViewImpl;
import cz.incad.kramerius.editor.client.view.EditorView;
import cz.incad.kramerius.editor.client.view.EditorViewsFactory;
import cz.incad.kramerius.editor.client.view.LoadView;
import cz.incad.kramerius.editor.client.view.RelationsView;
import cz.incad.kramerius.editor.client.view.Renderer;
import cz.incad.kramerius.editor.client.view.SaveView;
import cz.incad.kramerius.editor.share.GWTKrameriusObject;
import cz.incad.kramerius.editor.share.GWTRelationKindModel;
import cz.incad.kramerius.editor.share.GWTRelationModel;
import cz.incad.kramerius.editor.share.InputValidator;
import cz.incad.kramerius.editor.share.InputValidator.Validator;
import cz.incad.kramerius.editor.share.rpc.GetKrameriusObjectQuery;
import cz.incad.kramerius.editor.share.rpc.GetKrameriusObjectResult;
import cz.incad.kramerius.editor.share.rpc.SaveRelationsQuery;
import cz.incad.kramerius.editor.share.rpc.SaveRelationsResult;
import cz.incad.kramerius.editor.share.rpc.SaveRelationsResult.SaveRelationsState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.shared.BatchAction;
import net.customware.gwt.dispatch.shared.BatchResult;

/**
 *
 * @author Jan Pokorsky
 */
public class EditorPresenter implements Presenter, LoadView.Callback, EditorView.Callback {

    private final EditorView display;
    private LoadView loadView;
    private final SaveView<GWTRelationModel> saveView;
    private ContainerPresenter clipboardPresenter;
    private final RelationModelChangeHandler relationModelChangeHandler;
    private final Map<Display, RelationsPresenter> view2PresenterMap = new LinkedHashMap<Display, RelationsPresenter>();
    private final Map<GWTRelationModel, Presenter.Display> relModel2viewMap = new LinkedHashMap<GWTRelationModel, Display>();
    private final DispatchAsync dispatcher;
    private boolean isBound = false;

//    @Inject
//    public EditorPresenter(EditorDisplay display, Provider<ContainerDisplay> contDisplayProvider) {
    public EditorPresenter(EditorView display, DispatchAsync dispatcher) {
        this.display = display;
        this.clipboardPresenter = new ContainerPresenter(EditorViewsFactory.getInstance().createContainerView(), this);
        this.display.setClipboard(this.clipboardPresenter.getDisplay());
        ((ContainerViewImpl) clipboardPresenter.getDisplay()).debug = true;
        this.dispatcher = dispatcher;
        this.relationModelChangeHandler = new RelationModelChangeHandler();

        this.saveView = EditorViewsFactory.getInstance().createSaveView();
        this.saveView.setRenderer(new Renderer<String, GWTRelationModel>() {

            @Override
            public String render(GWTRelationModel model) {
                GWTKrameriusObject item = model.getKrameriusObject();
                return item.getTitle();
            }

            @Override
            public String renderTitle(GWTRelationModel model) {
                GWTKrameriusObject item = model.getKrameriusObject();
                return item.getKind() + ", " + item.getPID();
            }
        });
    }

    public void bind() {
        if (isBound) {
            return;
        }
        isBound = true;
        display.setCallback(this);
        clipboardPresenter.setModel(new GWTRelationKindModel());
        clipboardPresenter.bind();
    }

    @Override
    public Display getDisplay() {
        return display;
    }

    public void load(String pid) {
        load(pid, null);
    }

    private void load(String pid, final Runnable callback) {
        // first check if pid is already loaded
        for (GWTRelationModel rm : this.relModel2viewMap.keySet()) {
            if (rm.getKrameriusObject().getPID().equals(pid)) {
                Display relView = this.relModel2viewMap.get(rm);
                this.display.select(relView);
                if (callback != null) {
                    callback.run();
                }
                return;
            }
        }
        // run query
        dispatcher.execute(new GetKrameriusObjectQuery(pid), new AsyncCallback<GetKrameriusObjectResult>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed remote connection: " + caught.getMessage());
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(GetKrameriusObjectResult result) {
                editRelations(new GWTRelationModel(result.getResult()));
                if (callback != null) {
                    callback.run();
                }
            }

        });
    }

    private void editRelations(GWTRelationModel relationModel) {
        GWTKrameriusObject container = relationModel.getKrameriusObject();
        RelationsPresenter relationsPresenter = new RelationsPresenter(
                EditorViewsFactory.getInstance().createRelationsView(), this);
        RelationsView relView = relationsPresenter.getDisplay();
        this.display.add(relView, container.getTitle());
        relationsPresenter.setModel(relationModel);
        this.relModel2viewMap.put(relationModel, relView);
        this.view2PresenterMap.put(relView, relationsPresenter);
        relationsPresenter.bind();
        relationModel.addValueChangeHandler(this.relationModelChangeHandler);
    }

    private void save(final List<GWTRelationModel> relModels) {
        save(relModels, null);
    }

    /**
     * Saves relation models and runs callback iff the save is successful.
     * The callback may be {@code null}.
     */
    private void save(final List<GWTRelationModel> relModels, final Runnable callback) {
        SaveRelationsQuery[] queries = new SaveRelationsQuery[relModels.size()];
        for (int i = 0; i < queries.length; i++) {
            GWTRelationModel relModel = relModels.get(i);
            queries[i] = new SaveRelationsQuery(relModel);
        }

        this.dispatcher.execute(
                new BatchAction(BatchAction.OnException.CONTINUE, queries),
                new AsyncCallback<BatchResult>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed remote connection: " + caught.getMessage());
            }

            @Override
            public void onSuccess(BatchResult result) {
                StringBuilder err = new StringBuilder();
                int i = 0;
                for (int size = result.size(); i < size; i++) {
                    SaveRelationsResult saveResult = result.getResult(i, SaveRelationsResult.class);
                    GWTRelationModel relModel = relModels.get(i);
                    if (saveResult != null) {
                        relModel.save();
                    } else {
                        GWTKrameriusObject kobj = relModel.getKrameriusObject();
                        if (err.length() > 0) {
                            err.append(", ");
                        }
                        err.append(kobj.getTitle()).append('(').append(kobj.getPID()).append(')');
                        GWT.log(kobj.getPID(), result.getException(i));
                    }
                }

                if (err.length() != 0) {
                    Window.alert("Cannot save object(s): " + err.toString());
                } else if (callback != null) {
                    callback.run();
                }
            }
        });
    }

    private void save(GWTRelationModel model) {
        this.dispatcher.execute(new SaveRelationsQuery(model), new AsyncCallback<SaveRelationsResult>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Failed remote connection: " + caught.getMessage());
            }

            @Override
            public void onSuccess(SaveRelationsResult result) {
                if (result.get() == SaveRelationsState.OK) {
                    Window.alert("Model has been saved.");
                } else {
                    Window.alert("Model has been already modified by someone else.");
                }
            }
        });
    }

    @Override
    public void onLoadClick() {
        if (loadView == null) {
            loadView = EditorViewsFactory.getInstance().createLoadView();
        }
        loadView.setCallback(this);
        loadView.show();
    }

    @Override
    public void onSaveClick() {
        this.saveView.setCallback(new SaveView.Callback() {

            @Override
            public void onSaveViewCommit(boolean discard) {
                EditorPresenter.this.saveView.hide();
                EditorPresenter.this.save(EditorPresenter.this.saveView.getSelected());
            }
        });

        List<GWTRelationModel> saveables = new ArrayList<GWTRelationModel>();
        for (GWTRelationModel relModel : this.relModel2viewMap.keySet()) {
            if (relModel.isModified()) {
                saveables.add(relModel);
            }
        }

        this.saveView.setSaveables(saveables);
        this.saveView.setDiscardable(false);
        this.saveView.show();
    }

    @Override
    public void onLoadViewCommit(String input) {
        Validator<String> validator = InputValidator.validatePID(input);
        if (validator.isValid()) {
            load(validator.getNormalized(), new Runnable() {

                @Override
                public void run() {
                    loadView.hide();
                }
            });
        } else {
            loadView.showError(validator.getErrorMessage());
        }
    }

    @Override
    public void onEditorTabClose() {
        final Display selected = this.display.getSelected();
        final RelationsPresenter relPresenter = this.view2PresenterMap.get(selected);
        final GWTRelationModel relModel = relPresenter.getModel();
        final Runnable doEditorTabClose = new Runnable() {

            @Override
            public void run() {
                display.remove(selected);
                view2PresenterMap.remove(selected);
                relModel2viewMap.remove(relModel);
                relPresenter.unbind();
            }
        };

        if (relModel.isModified()) {
            this.saveView.setCallback(new SaveView.Callback() {

                @Override
                public void onSaveViewCommit(boolean discard) {
                    saveView.hide();
                    if (discard) {
                        doEditorTabClose.run();
                    } else {
                        save(saveView.getSelected(), doEditorTabClose);
                    }
                }
            });
            this.saveView.setSaveables(Collections.singletonList(relModel));
            this.saveView.setDiscardable(true);
            this.saveView.show();
        } else {
            doEditorTabClose.run();
        }
    }

    private void setModified(GWTRelationModel relModel) {
        Display relView = this.relModel2viewMap.get(relModel);
        this.display.setModified(relView, relModel.isModified());
    }

    private final class RelationModelChangeHandler implements ValueChangeHandler<GWTRelationModel> {

        @Override
        public void onValueChange(ValueChangeEvent<GWTRelationModel> event) {
            GWTRelationModel relModel = event.getValue();
            EditorPresenter.this.setModified(relModel);
        }
    }

}