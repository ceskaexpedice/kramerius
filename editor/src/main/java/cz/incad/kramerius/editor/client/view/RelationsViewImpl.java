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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jan Pokorsky
 */
public final class RelationsViewImpl implements RelationsView {

//    private SimplePanel mainWidget = new SimplePanel();
    private Widget mainWidget = createTabLayoutPanel();
    private List<RelationTab> tabs = new ArrayList<RelationTab>();
    private Callback callback;

    @Override
    public void addTab(RelationTab view) {
        ScrollPanel tabContent = new ScrollPanel(view.asWidget());
//        if (tabs.isEmpty()) {
//            mainWidget.setWidget(tabContent);
//        } else {
            AdvancedTabLayoutPanel tabPanel = getTabLayoutPanel();
            tabPanel.add(tabContent, view.getName(), false);
//        }
        tabs.add(view);
    }

    @Override
    public RelationTab getSelectedTab() {
        switch (tabs.size()) {
            case 0: return null;
//            case 1: return tabs.get(0);
            default: return tabs.get(getTabLayoutPanel().getSelectedIndex());
        }
    }

    @Override
    public void setModified(RelationsView.RelationTab view, boolean modified) {
        int index = this.tabs.indexOf(view);
        getTabLayoutPanel().setModified(index, modified);
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public Widget asWidget() {
        return mainWidget;
    }

    private AdvancedTabLayoutPanel createTabLayoutPanel() {
        AdvancedTabLayoutPanel tabPanel = new AdvancedTabLayoutPanel(2.2, Unit.EM);
        tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {

            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                if (callback != null) {
                    callback.onTabSelection();
                }
            }
        });
//        mainWidget.setVisible(false);
//        Widget currWidget = mainWidget.getWidget();
//        mainWidget.remove(currWidget);
//        tabPanel.add(currWidget, tabs.get(0).getName());
//        mainWidget.setWidget(tabPanel);
//        mainWidget.setVisible(true);
        return tabPanel;
    }

    private AdvancedTabLayoutPanel getTabLayoutPanel() {
        AdvancedTabLayoutPanel tabPanel;
//        if (tabs.size() == 0) {
//            tabPanel = createTabLayoutPanel();
//        } else {
//            tabPanel = (TabLayoutPanel) mainWidget.getWidget();
            tabPanel = (AdvancedTabLayoutPanel) mainWidget;
//        }
        return tabPanel;
    }
}