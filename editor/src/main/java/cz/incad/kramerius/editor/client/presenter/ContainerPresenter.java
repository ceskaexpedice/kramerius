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

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.DragController;
import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import cz.incad.kramerius.editor.client.view.ContainerView;
import cz.incad.kramerius.editor.client.view.ContainerView.Callback;
import cz.incad.kramerius.editor.client.view.DNDManager;
import cz.incad.kramerius.editor.client.view.DNDManager.DropConsumer;
import cz.incad.kramerius.editor.client.view.DNDManager.SensitiveDropController;
import cz.incad.kramerius.editor.client.view.EditorViewsFactory;
import cz.incad.kramerius.editor.share.GWTKrameriusObject;
import cz.incad.kramerius.editor.share.GWTRelationKindModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic container able to present list of Kramerius objects. It implements
 * Drag&Drop support.
 *
 * @author Jan Pokorsky
 */
public class ContainerPresenter implements Presenter, Callback {

    private final ContainerView display;
    private Map<Display, ElementPresenter> view2elmPresenterMap = Collections.emptyMap();
    private final EditorPresenter ebus;
    private DragController dragController;
    private SensitiveDropController dropController;

    private GWTRelationKindModel model;
    private boolean isBound = false;

    public ContainerPresenter(ContainerView display, EditorPresenter ebus) {
        this.display = display;
        this.ebus = ebus;
    }

    @Override
    public ContainerView getDisplay() {
        return display;
    }

    public void setModel(GWTRelationKindModel model) {
        this.model = model;
    }

    public void bind() {
        // XXX on unbind it should unregister drag and drop
        if (isBound) {
            return;
        }
        isBound = true;

        initDragAndDrop();
        List<Display> elmDisplays = createElementViews(model.getRelations());
        display.setElements(elmDisplays);
        display.setCallback(this);
    }

    public void unbind() {
        if (isBound) {
            isBound = false;
        } else {
            return;
        }

        ElementPresenter[] elmPresenters = this.view2elmPresenterMap.values()
                .toArray(new ElementPresenter[this.view2elmPresenterMap.size()]);
        for (ElementPresenter elmPresenter : elmPresenters) {
            unbindElement(elmPresenter);
        }
        DNDManager.getInstance().unregisterDropTarget(dropController);
    }

    private void unbindElement(ElementPresenter ep) {
        Display elmDisplay = ep.getDisplay();
        view2elmPresenterMap.remove(elmDisplay);
        dragController.makeNotDraggable(elmDisplay.asWidget());
    }

    private void bindElement(ElementPresenter ep) {
        Display elmDisplay = ep.getDisplay();
        view2elmPresenterMap.put(elmDisplay, ep);
        dragController.makeDraggable(elmDisplay.asWidget());
    }

// XXX   @Override
    private void onElementOrderChange() {
        List<? extends Display> elements = display.getElements();
        recreateElmPresentersMap(elements);
    }

    void recreateElmPresentersMap(List<? extends Display> elms) {
        Map<Display, ElementPresenter> currMap = view2elmPresenterMap;
        view2elmPresenterMap = new LinkedHashMap<Display, ElementPresenter>(elms.size());
        List<GWTKrameriusObject> kobjs = new ArrayList<GWTKrameriusObject>(elms.size());
        log("recreate.view2elmPresenterMap.elm.size: " + elms.size());
//        log("recreateElmPresentersMap.elmMap.size: " + elmPresentersMap.size());
        log("recreate.view2elmPresenterMap.currMap.size: " + currMap.size());

        for (Display elmDisplay : elms) {
            ElementPresenter elmPresenter = currMap.get(elmDisplay);
            view2elmPresenterMap.put(elmDisplay, elmPresenter);
//            log("recreateElmPresentersMap.view: " + elmDisplay + ", presenter: " + elmPresenter);
//            log("recreateElmPresentersMap.model: " + (elmPresenter == null ? null: elmPresenter.getModel()));
            kobjs.add(elmPresenter.getModel());
        }
        model.setRelations(kobjs);
    }


    private List<ElementPresenter> resolveElementPresenters(List<? extends Display> elms) {
        List<ElementPresenter> result = new ArrayList<ElementPresenter>(elms.size());
        for (Display elmView : elms) {
            result.add(view2elmPresenterMap.get(elmView));
        }
        return result;
    }

    private List<Display> createElementViews(List<GWTKrameriusObject> elms) {
        List<Display> elmDisplays = new ArrayList<Display>(elms.size());
        view2elmPresenterMap = new LinkedHashMap<Display, ElementPresenter>(elms.size());
        for (GWTKrameriusObject elm : elms) {
            ElementPresenter elmPresenter = null;
            elmPresenter = new ElementPresenter(EditorViewsFactory.getInstance().createElementView(), ebus);
            elmPresenter.setModel(elm);
            Display elmDisplay = elmPresenter.getDisplay();
            elmDisplays.add(elmDisplay);
            elmPresenter.bind();
            bindElement((ElementPresenter) elmPresenter);
//            elmPresentersMap.put(elmDisplay, (ElementPresenter) elmPresenter);
//            dragController.makeDraggable(elmDisplay.asWidget());
        }
        return elmDisplays;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ContainerPresenter {");
        for (Iterator<GWTKrameriusObject> it = model.getRelations().iterator(); it.hasNext();) {
            GWTKrameriusObject gko = it.next();
            sb.append(gko);
            if (it.hasNext()) {
                sb.append(',');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private void log(String s) {
        // GWT will introduce java.util.logging.* support in GWT 2.1
//        System.out.println(s);
//        GWT.log(s);
    }

    private void initDragAndDrop() {
        DNDManager dndManager = DNDManager.getInstance();

        dropController = dndManager.createController(display.getDropController(), new DropConsumer() {

            @Override
            public void consumeDroppables(Object... objs) {
                // someone sends me objects
                List<Display> views = new ArrayList<Display>(objs.length);
                for (Object obj : objs) {
                    if (obj instanceof ElementPresenter) {
                        ElementPresenter ep = (ElementPresenter) obj;
                        bindElement(ep);
                        views.add(ep.getDisplay());
                    }
                }
                display.setDropElements(views);
                onElementOrderChange();
            }
        });

        dragController = dndManager.createSimpleDNDSupport(dropController);
        dragController.addDragHandler(new DragHandlerAdapter() {

            @Override
            public void onDragEnd(DragEndEvent event) {
                super.onDragEnd(event);
                log("onDragEnd: " + ContainerPresenter.this.toString());
                DragContext context = event.getContext();
                SensitiveDropController remoteController;
                if (context.selectedWidgets.isEmpty()) {
                    // ignore; who knows why but it is fired in multiselection
                    // mode when user makes selection of widgets
                    return;
                }
                if (context.finalDropController == dropController) {
                    // drop inside this container
                    // update view ; it is not necessary ; already updated by D&D
//                    display.getDragElements(event);
                    // recompute model
                    onElementOrderChange();
                    dragController.clearSelection();
                } else if (context.finalDropController != null) {
                    // drop to other container
                    // map widgets to displays and update view
                    List<? extends Display> dragElements = display.getDragElements(event);
                    // map displays to presenters
                    List<ElementPresenter> dragPresenters = resolveElementPresenters(dragElements);
                    // discard all bindings to source container
                    for (ElementPresenter ep : dragPresenters) {
                        unbindElement(ep);
                    }
                    // notify remote drop target
                    if (context.finalDropController instanceof SensitiveDropController) {
                        remoteController = (SensitiveDropController) context.finalDropController;
                        remoteController.setObjects(dragPresenters.toArray());
                    }
                    // XXX update model?
                    onElementOrderChange();
                    dragController.clearSelection();
                } else {
                    // do nothing; D&D was cancelled
                }
            }

        });
    }

}