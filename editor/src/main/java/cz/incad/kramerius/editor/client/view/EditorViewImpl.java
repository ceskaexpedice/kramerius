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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.incad.kramerius.editor.client.presenter.Presenter.Display;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jan Pokorsky
 */
public final class EditorViewImpl implements EditorView {

    private static EditorViewImplUiBinder uiBinder = GWT.create(EditorViewImplUiBinder.class);

    private final Widget widget;
    @UiField Anchor loadClickable;
    @UiField Anchor saveClickable;
    @UiField Anchor krameriusClickable;
    @UiField Anchor languagesClickable;
    @UiField FlowPanel clipboardPanel;
    @UiField TabLayoutPanel editorTabPanel;
    @UiField StyleAccess styleAccess;
    private Callback callback;
    private final List<Display> tabsModel = new ArrayList<Display>();

    interface EditorViewImplUiBinder extends UiBinder<Widget, EditorViewImpl> {}

    interface StyleAccess extends CssResource {

        String modified();
    }

    public EditorViewImpl() {
        widget = uiBinder.createAndBindUi(this);
    }

    @Override
    public void add(Display item, String name) {
//        final ScrollPanel tabContentPanel = new ScrollPanel(item.asWidget());
        Widget tabContentPanel = item.asWidget();

        editorTabPanel.add(tabContentPanel, createCloseTabWidget(name, tabContentPanel));
        editorTabPanel.selectTab(tabContentPanel);
        tabsModel.add(item);
    }

    @Override
    public void remove(Display item) {
        int selectedIndex = getSelectedIndex(item);
        if (selectedIndex >= 0) {
            editorTabPanel.remove(selectedIndex);
            tabsModel.remove(selectedIndex);
        }
    }

    @Override
    public void select(Display item) {
        int selectedIndex = getSelectedIndex(item);
        if (selectedIndex >= 0) {
            this.editorTabPanel.selectTab(selectedIndex);
        }
    }

    @Override
    public Display getSelected() {
        int selectedIndex = editorTabPanel.getSelectedIndex();
        return tabsModel.get(selectedIndex);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public void setCallback(Callback c) {
        this.callback = c;
    }

    @Override
    public void setClipboard(Display clipboard) {
        clipboardPanel.add(clipboard.asWidget());
    }

    @Override
    public void setModified(Display view, boolean modified) {
        int index = this.tabsModel.indexOf(view);
        if (index < 0) {
            return;
        }
        Widget tabWidget = this.editorTabPanel.getTabWidget(index);
        if (modified) {
            tabWidget.getElement().addClassName(this.styleAccess.modified());
        } else {
            tabWidget.getElement().removeClassName(this.styleAccess.modified());
        }
    }

    @UiHandler("loadClickable")
    void onLoadClick(ClickEvent ce) {
        if (callback != null) {
            callback.onLoadClick();
        }
    }

    @UiHandler("saveClickable")
    void onSaveClick(ClickEvent ce) {
        if (callback != null) {
            callback.onSaveClick();
        }
    }

    @UiHandler("krameriusClickable")
    void onKrameriusClick(ClickEvent ce) {
        if (callback != null) {
            callback.onKrameriusClick();
        }
    }

    private Widget createCloseTabWidget(String name, final Widget tabContent) {
        HorizontalPanel tabHandlePanel = new HorizontalPanel();
        Label nameHandle = new Label(ViewUtils.makeLabelVisible(name));
        nameHandle.setTitle(name);
        tabHandlePanel.add(nameHandle);
        // XXX fix: temporary solution to remove the tab
        Label closeHandle = new Label("\u2718");
        closeHandle.setTitle("Close tab");
        closeHandle.addStyleName("editorTabCloseButton");
        closeHandle.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                callback.onEditorTabClose();
            }
        });
        tabHandlePanel.add(closeHandle);
        return tabHandlePanel;
    }

    private int getSelectedIndex(Display tab) {
        for (int i = 0; i < tabsModel.size(); i++) {
            Display d = tabsModel.get(i);
            if (d == tab) {
                return i;
            }
        }
        return -1;
    }

}