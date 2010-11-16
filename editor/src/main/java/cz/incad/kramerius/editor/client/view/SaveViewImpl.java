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

import cz.incad.kramerius.editor.client.EditorConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jan Pokorsky
 */
public final class SaveViewImpl<T> implements SaveView<T> {

    interface Binder extends UiBinder<Widget, SaveViewImpl> {}
    
    interface StyleAccess extends CssResource {
        String listItem();
        String listItemEven();
        String listItemOdd();
    }

    private static final EditorConstants I18N = GWT.create(EditorConstants.class);
    private static Binder uiBinder = GWT.create(Binder.class);
    private SaveView.Callback callback;
    private Renderer<String, T> renderer;
    private List<T> saveables;
    private Widget saveViewWidget;
    private DialogBox dialogBox;
    @UiField Button okButton;
    @UiField Button discardButton;
    @UiField FlowPanel saveablePanel;
    @UiField ScrollPanel scrollPanel;
    @UiField StyleAccess style;

    public SaveViewImpl() {
        this.saveViewWidget = (Widget) uiBinder.createAndBindUi(this);
    }

    @Override
    public void setSaveables(List<T> saveables) {
        this.saveablePanel.clear();

        int saveablesSize = saveables.size();
        if (saveablesSize == 0) {
            this.saveablePanel.add(new Label(I18N.nothingToSaveLabel()));
            this.okButton.setEnabled(false);
//            for (int i = 0; i < 10; i++) {
//                CheckBox checkBox = new CheckBox("Very long CheckBox Very long CheckBox");
//                checkBox.setValue(Boolean.valueOf(true));
//                checkBox.addStyleName(style.listItem());
//                String styleName = saveablePanel.getWidgetCount() % 2 == 0
//                        ? style.listItemOdd(): style.listItemEven();
//                checkBox.addStyleName(styleName);
//                this.saveablePanel.add(checkBox);
//            }
        } else {
            this.okButton.setEnabled(true);
        }

        for (T saveable : saveables) {
            Widget saveableWidget = saveablesSize == 1
                    ? createSaveableLabel(saveable)
                    : createSaveableCheckBox(saveable);
            saveableWidget.addStyleName(style.listItem());
            String styleName = saveablePanel.getWidgetCount() % 2 == 0
                    ? style.listItemOdd(): style.listItemEven();
            saveableWidget.addStyleName(styleName);
            this.saveablePanel.add(saveableWidget);
        }

        this.saveables = saveables;
    }

    private Widget createSaveableLabel(T saveable) {
        Label widget = new Label(this.renderer.render(saveable));
        widget.setTitle(this.renderer.renderTitle(saveable));
        return widget;
    }

    private Widget createSaveableCheckBox(T saveable) {
        CheckBox widget = new CheckBox(this.renderer.render(saveable));
        widget.setTitle(this.renderer.renderTitle(saveable));
        widget.setValue(true);
        return widget;
    }

    @Override
    public List<T> getSelected() {
        List selected = new ArrayList(this.saveablePanel.getWidgetCount());
        int index = 0;
        for (Widget w : this.saveablePanel) {
            if (isSelected(w)) {
                selected.add(this.saveables.get(index));
            }
            index++;
        }
        return selected;
    }

    private boolean isSelected(Widget w) {
        if (w instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) w;
            return checkBox.getValue();
        } else if (w instanceof Label && !this.saveables.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setRenderer(Renderer<String, T> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void setDiscardable(boolean discard) {
        this.discardButton.setVisible(discard);
    }

    @Override
    public void show() {
        if (this.dialogBox == null) {
            this.dialogBox = new DialogBox();
            this.dialogBox.setText(I18N.saveViewTitle());
            this.dialogBox.setAnimationEnabled(true);
            this.dialogBox.setGlassEnabled(true);
            this.dialogBox.setWidget(asWidget());
        }

        this.dialogBox.center();
        this.okButton.setFocus(true);
    }

    @Override
    public void hide() {
        this.dialogBox.hide();
    }

    @Override
    public Widget asWidget() {
        return this.saveViewWidget;
    }

    @UiHandler({"okButton", "discardButton"})
    void onOkClick(ClickEvent event) {
        boolean discard = discardButton == event.getSource();
        this.callback.onSaveViewCommit(discard);
    }

    @UiHandler("cancelButton")
    void onCancelClick(ClickEvent event) {
        hide();
    }

}
