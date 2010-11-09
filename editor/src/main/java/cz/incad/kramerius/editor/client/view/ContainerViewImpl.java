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

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.allen_sauer.gwt.dnd.client.drop.FlowPanelDropController;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import cz.incad.kramerius.editor.client.presenter.Presenter.Display;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jan Pokorsky
 */
public final class ContainerViewImpl implements ContainerView {

    private FlowPanel containerPanel;
//    private FlexTable p;
    private FlowPanelDropController dropController;
    private Map<Widget, Display> elementsMap;
    private Callback callback;

    public ContainerViewImpl() {
//        p = new FlexTable();
        containerPanel = new FlowPanel();

        dropController = new FlowPanelDropController(containerPanel);
//        final SensitiveDropController<Kind, Display> sensdnd = DNDManager.createController(dnd, EnumSet.of(Kind.PAGE), new DropConsumer<Display>() {
//
//            @Override
//            public void consumeDroppables(Display... objs) {
//                // map display to presenter
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        });
    }

    @Override
    public void setName(String s) {
//        containerPanel.setTitle(s);
    }

    @Override
    public void setElements(List<? extends Display> l) {
        containerPanel.clear();
        elementsMap = new HashMap<Widget, Display>(l.size());
        for (Display d : l) {
            containerPanel.add(d.asWidget());
            elementsMap.put(d.asWidget(), d);
        }
        insertDropPadding();
    }

    private void insertDropPadding() {
        if (containerPanel.getWidgetCount() == 0) {
            HTML padding = new HTML("<span style='line-height:128px;padding:64px 2px;vertical-align: top;'> </span>");
            padding.getElement().getStyle().setDisplay(com.google.gwt.dom.client.Style.Display.INLINE);
            containerPanel.add(padding);
        }
    }

    @Override
    public List<? extends Display> getElements() {
        List<Display> res = getElements(containerPanel, containerPanel.getWidgetCount());
        return res;
    }

    public boolean debug = false;
    private List<Display> getElements(Iterable<Widget> p, int count) {
        List<Display> res = new ArrayList<Display>(count);
        for (Widget w : p) {
            Display d = elementsMap.get(w);
            if (d != null) {
                res.add(d);
            } else {
                // clear obsolete views
                elementsMap.remove(w);
//                containerPanel.remove(w);
            }
        }
        return res;
    }

    @Override
    public List<? extends Display> getDragElements(DragEndEvent de) {
        List<Widget> selection = de.getContext().selectedWidgets;
        List<Display> elements = getElements(selection, selection.size());
        insertDropPadding();
        return elements;
    }

    @Override
    public void setDropElements(List<? extends Display> l) {
        for (Display d : l) {
            checkChildWidgetExist(d);
            elementsMap.put(d.asWidget(), d);
        }
        for (Widget w : containerPanel) {
            if (elementsMap.get(w) == null) {
                containerPanel.remove(w);
//            System.out.println("remove: " + w);
            }
        }
    }

    @Override
    public DropController getDropController() {
        return dropController;
    }

    @Override
    public Widget asWidget() {
        return containerPanel;
    }

    @Override
    public void setCallback(Callback c) {
        this.callback = c;
    }

    private void checkChildWidgetExist(Display d) {
        Widget asWidget = d.asWidget();
//        System.out.println("panel: " + p);
//        System.out.println("asWidget: " + asWidget);
        for (Widget w : containerPanel) {
//            System.out.println("w: " + w);
            if (w == asWidget) {
                return;
            }
        }
        throw new IllegalStateException("Unknown view: " + d);
    }

//    private void useFlexTable(List<? extends Display> l) {
//        p.clear();
//        int row = 0;
//        int col = 0;
//        for (Display d : l) {
////            p.add(d.asWidget());
//            p.setWidget(row, col, d.asWidget());
//            row = row + (col + 1) / 10;
//            col = (col + 1) % 10;
//        }
//    }

}