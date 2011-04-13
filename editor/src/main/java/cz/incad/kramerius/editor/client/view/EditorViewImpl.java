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
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.incad.kramerius.editor.client.presenter.Presenter.Display;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jan Pokorsky
 */
public final class EditorViewImpl implements EditorView {

    private static Binder uiBinder = GWT.create(Binder.class);

    private final Widget widget;
    @UiField Anchor loadClickable;
    @UiField Anchor saveClickable;
    //@UiField Anchor krameriusClickable;
    @UiField Anchor languagesClickable;
    @UiField FlowPanel clipboardPanel;
    @UiField AdvancedTabLayoutPanel editorTabPanel;
    private PopupPanel languagesPopup;
    private Callback callback;
    private final List<Display> tabsModel = new ArrayList<Display>();

    interface Binder extends UiBinder<Widget, EditorViewImpl> {}

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
        int tabIndex = getTabIndex(item);
        if (tabIndex >= 0) {
            editorTabPanel.remove(tabIndex);
            tabsModel.remove(tabIndex);
        }
    }

    @Override
    public void select(Display item) {
        int tabIndex = getTabIndex(item);
        if (tabIndex >= 0) {
            this.editorTabPanel.selectTab(tabIndex);
        }
    }

    @Override
    public Display getSelected() {
        int selectedIndex = editorTabPanel.getSelectedIndex();
        return tabsModel.get(selectedIndex);
    }

    @Override
    public void setLanguages(String[] languages, int selected) {
        if (languages == null || languages.length < 2) {
            languagesClickable.setVisible(false);
            return;
        }

        languagesPopup = new PopupPanel(true, false);
        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(2);
        for (int i = 0; i < languages.length; i++) {
            String language = languages[i];
            Widget item;
            if (i == selected) {
                item = new Label(language + " \u00AB");
                item.addStyleName("languageMenuItemCurrent");
            } else {
                final Anchor anchor = new Anchor(language);
                anchor.getElement().setPropertyInt("LanguageIndex", i);
                anchor.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        int index = anchor.getElement().getPropertyInt("LanguageIndex");
                        callback.onLanguagesClick(index);
                        languagesPopup.hide();
                    }
                });
                item = anchor;
                item.addStyleName("languageMenuItem");
            }
            vp.add(item);
        }
        languagesPopup.setWidget(vp);
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

    /*
    @UiHandler("krameriusClickable")
    void onKrameriusClick(ClickEvent ce) {
        if (callback != null) {
            callback.onKrameriusClick();
        }
    }
*/
    @UiHandler("languagesClickable")
    void onLanguagesClick(ClickEvent ce) {
        if (callback != null) {
            languagesPopup.showRelativeTo(languagesClickable);
        }
    }

    @UiHandler("editorTabPanel")
    void onTabClose(CloseEvent<Integer> event) {
        if (callback != null) {
            int index = event.getTarget();
            Display tab = tabsModel.get(index);
            callback.onEditorTabClose(tab);
        }
    }
    
    private int getTabIndex(Display tab) {
        for (int i = 0; i < tabsModel.size(); i++) {
            Display d = tabsModel.get(i);
            if (d == tab) {
                return i;
            }
        }
        return -1;
    }

}