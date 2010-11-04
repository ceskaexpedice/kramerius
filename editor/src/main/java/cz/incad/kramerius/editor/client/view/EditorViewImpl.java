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
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
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
    @UiField AdvancedTabLayoutPanel editorTabPanel;
    private Callback callback;
    private final List<Display> tabsModel = new ArrayList<Display>();

    interface EditorViewImplUiBinder extends UiBinder<Widget, EditorViewImpl> {}

    public EditorViewImpl() {
        widget = uiBinder.createAndBindUi(this);
    }

    @Override
    public void add(Display item, String name) {
//        final ScrollPanel tabContentPanel = new ScrollPanel(item.asWidget());
        Widget tabContentPanel = item.asWidget();

        editorTabPanel.add(tabContentPanel, name, true);
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
        this.editorTabPanel.setModified(index, modified);
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

    @UiHandler("editorTabPanel")
    void onTabClose(CloseEvent<Integer> event) {
        if (callback != null) {
            callback.onEditorTabClose();
        }
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