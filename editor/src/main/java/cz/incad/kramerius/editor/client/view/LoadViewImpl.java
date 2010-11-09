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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Jan Pokorsky
 */
public final class LoadViewImpl extends Composite implements LoadView {

    private static final EditorConstants I18N = GWT.create(EditorConstants.class);;
    private static Binder uiBinder = GWT.create(Binder.class);

    private DialogBox dialogBox;
    private Callback callback;

    @UiField Button cancelButton;
    @UiField Button okButton;
    @UiField TextBox textBox;
    @UiField(provided=true)
    SuggestBox oracleTextBox;
    @UiField Label errorLabel;

    interface Binder extends UiBinder<Widget, LoadViewImpl> {}

    public LoadViewImpl() {
        this.oracleTextBox = new SuggestBox(new HtmlOracle());
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void show() {
        if (dialogBox == null) {
            dialogBox = new DialogBox();
            dialogBox.setText(I18N.loadViewTitle());
            dialogBox.setAnimationEnabled(true);
            dialogBox.setWidget(this);
            dialogBox.setGlassEnabled(true);
            dialogBox.setWidth("400px");
        }

        errorLabel.setVisible(false);
        clearError();
        textBox.setText("uuid:");
        oracleTextBox.setText("");
        waiting(false);

//        dialogBox.show();
        dialogBox.center();
        oracleTextBox.setFocus(true);
    }

    @Override
    public void showError(String s) {
        waiting(false);
        if (!errorLabel.isVisible()) {
            errorLabel.setVisible(!s.isEmpty());
        }
        errorLabel.setText(s);
    }

    private void clearError() {
        errorLabel.setText("");
    }

    @Override
    public void setCallback(Callback c) {
        this.callback = c;
    }

    @Override
    public void hide() {
        dialogBox.hide();
    }

    @Override
    public HasValue<String> pid() {
        return textBox;
    }

    @Override
    public HasValue<String> title() {
        return oracleTextBox;
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
        clearError();
        if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
            callItemAdded();
        }
    }

    private void callItemAdded() {
        waiting(true);
        callback.onLoadViewCommit(textBox.getText());
    }

    @UiHandler("oracleTextBox")
    void onSuggestionCommit(SelectionEvent<SuggestOracle.Suggestion> event) {
        waiting(true);
        callback.onLoadViewSuggestionCommit(event.getSelectedItem());
    }

    private void waiting(boolean flag) {
        okButton.setEnabled(!flag);
        cancelButton.setEnabled(!flag);
    }

    private final class HtmlOracle extends SuggestOracle {

        @Override
        public void requestSuggestions(Request request, Callback callback) {
            clearError();
            LoadViewImpl.this.callback.onLoadViewSuggestionRequest(request, callback);
        }

        @Override
        public boolean isDisplayStringHTML() {
            return true;
        }

    }

}