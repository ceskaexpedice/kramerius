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

package cz.incad.kramerius.editor.client.view;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.DragController;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to integrate {@code com.allen_sauer.gwt.dnd} library.
 * 
 * @author Jan Pokorsky
 */
public class DNDManager {

    private static final  DNDManager INSTANCE = new DNDManager();
    private final List<SensitiveDropController> dropTargets;
    private final List<DropTargetListener> dropTargetListeners;

    private DNDManager() {
        dropTargets = new ArrayList<SensitiveDropController>();
        dropTargetListeners = new ArrayList<DropTargetListener>();
    }

    public static DNDManager getInstance() {
        return INSTANCE;
    }

    public SensitiveDropController createController(DropController dc, DropConsumer consumer) {
        return new SensitiveDropControllerImpl(dc, consumer);
    }

    public void registerDropTarget(SensitiveDropController target) {
        dropTargets.add(target);
        fireDropTargetChange(target, true);
    }

    public void unregisterDropTarget(SensitiveDropController target) {
        dropTargets.remove(target);
        disposeSimpleDNDSupport(target);
        fireDropTargetChange(target, false);
    }

    private void disposeSimpleDNDSupport(SensitiveDropController dc) {
        for (DropTargetListener listener : dropTargetListeners) {
            if (listener instanceof SimpleSupportDropTargetListener) {
                SimpleSupportDropTargetListener slistener = (SimpleSupportDropTargetListener) listener;
                if (dc == slistener.getDropController()) {
                    removeDropTargetListener(slistener);
                    slistener.getDragController().unregisterDropControllers();
                    return;
                }
            }
        }
    }

//    public <T> DropController[] getDropTargets(Set<T> kind) {
//        return null;
//    }

    public SensitiveDropController[] getDropTargets() {
        return dropTargets.toArray(new SensitiveDropController[dropTargetListeners.size()]);
    }

    public void addDropTargetListener(DropTargetListener l){
        dropTargetListeners.add(l);
    }

    public void removeDropTargetListener(DropTargetListener l){
        dropTargetListeners.remove(l);
    }

    public DragController createSimpleDNDSupport(SensitiveDropController dropController) {
        final PickupDragController dragController = new PickupDragController(RootPanel.get(), false);
        registerDropTarget(dropController);
        for (SensitiveDropController dc : this.dropTargets) {
            // XXX here should be filter to register only controlers providing proper kinds
            dragController.registerDropController(dc);
        }

        addDropTargetListener(new SimpleSupportDropTargetListener(dragController, dropController));

        // constrained to RootPanel
        dragController.setBehaviorConstrainedToBoundaryPanel(true);
        dragController.setBehaviorMultipleSelection(true);
        dragController.setBehaviorDragStartSensitivity(10);
        dragController.setBehaviorScrollIntoView(true);
        return dragController;
    }

    private static final class SimpleSupportDropTargetListener implements DropTargetListener {
        private final PickupDragController dragController;
        private final SensitiveDropController dropController;

        public SimpleSupportDropTargetListener(PickupDragController dragController,
                SensitiveDropController dropController) {
            this.dragController = dragController;
            this.dropController = dropController;
        }

        @Override
        public void onAddDropTarget(DropController target) {
            dragController.registerDropController(target);
        }

        @Override
        public void onRemoveDropTarget(DropController target) {
            dragController.unregisterDropController(target);
        }

        public PickupDragController getDragController() {
            return dragController;
        }

        public SensitiveDropController getDropController() {
            return dropController;
        }
    }

    private void fireDropTargetChange(DropController target, boolean add) {
        for (DropTargetListener listener : dropTargetListeners) {
            if (add) {
                listener.onAddDropTarget(target);
            } else {
                listener.onRemoveDropTarget(target);
            }
        }
    }

    public interface DropTargetListener {
        void onAddDropTarget(DropController target);
        void onRemoveDropTarget(DropController target);
    }

    public interface SensitiveDropController extends  DropController {

        void setObjects(Object... objs);
    }

    public interface DropConsumer {

        void consumeDroppables(Object... objs);
    }

    private static final class SensitiveDropControllerImpl implements SensitiveDropController {

        private DropController delegate;
        private final DropConsumer dconsumer;

        public SensitiveDropControllerImpl(DropController delegate, DropConsumer dconsumer) {
            this.delegate = delegate;
            this.dconsumer = dconsumer;
        }

        @Override
        public void setObjects(Object... objs) {
            dconsumer.consumeDroppables(objs);
        }

        @Override
        public Widget getDropTarget() {
            return delegate.getDropTarget();
        }

        @Override
        public void onDrop(DragContext context) {
            delegate.onDrop(context);
        }

        @Override
        public void onEnter(DragContext context) {
            delegate.onEnter(context);
        }

        @Override
        public void onLeave(DragContext context) {
            delegate.onLeave(context);
        }

        @Override
        public void onMove(DragContext context) {
            delegate.onMove(context);
        }

        @Override
        public void onPreviewDrop(DragContext context) throws VetoDragException {
            delegate.onPreviewDrop(context);
        }


    }

}