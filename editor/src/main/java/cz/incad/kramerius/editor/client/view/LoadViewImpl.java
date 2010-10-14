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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Jan Pokorsky
 */
public final class LoadViewImpl extends Composite implements LoadView {

    private static LoadViewImplUiBinder uiBinder = GWT.create(LoadViewImplUiBinder.class);

    private DialogBox dialogBox;
    private Callback callback;

    @UiField Button cancelButton;
    @UiField Button okButton;
    @UiField TextBox textBox;
    @UiField Label errorLabel;
    @UiField Button demoButton1;
    @UiField Button demoButton2;


    interface LoadViewImplUiBinder extends UiBinder<Widget, LoadViewImpl> {}

    public LoadViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void show() {
        if (dialogBox == null) {
            dialogBox = new DialogBox();
            dialogBox.setText("New Editor");
            dialogBox.setAnimationEnabled(true);
            dialogBox.setWidget(this);
            dialogBox.setGlassEnabled(true);
        }

        errorLabel.setVisible(false);
        textBox.setText("uuid:");

//        dialogBox.show();
        dialogBox.center();
        textBox.setFocus(true);
    }

    @Override
    public void showError(String s) {
        errorLabel.setVisible(true);
        errorLabel.setText(s);
    }

    @Override
    public void setCallback(Callback c) {
        this.callback = c;
    }

    @Override
    public void hide() {
        dialogBox.hide();
    }

    @UiHandler("cancelButton")
    void onCancelClick(ClickEvent event) {
        dialogBox.hide();
    }

    @UiHandler("okButton")
    void onOkClick(ClickEvent event) {
        callItemAdded();
    }

    @UiHandler("textBox")
    void onEnterPress(KeyPressEvent event) {
        if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
            callItemAdded();
        }
    }

    private void callItemAdded() {
        callback.onLoadViewCommit(textBox.getText());
    }

    @UiHandler({"demoButton1", "demoButton2"})
    void onDemoClick(ClickEvent event) {
        Object source = event.getSource();
        if (source == demoButton1) { // drobnustky
            callback.onLoadViewCommit("uuid:0eaa6730-9068-11dd-97de-000d606f5dc6");
        } else if (source == demoButton2) { // kniha zlata
            callback.onLoadViewCommit("uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6");
        }
    }

}