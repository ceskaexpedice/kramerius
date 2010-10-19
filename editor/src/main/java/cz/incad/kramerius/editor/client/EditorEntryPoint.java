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

package cz.incad.kramerius.editor.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import cz.incad.kramerius.editor.client.presenter.EditorPresenter;
import cz.incad.kramerius.editor.client.view.EditorViewsFactory;
import net.customware.gwt.dispatch.client.DefaultExceptionHandler;
import net.customware.gwt.dispatch.client.standard.StandardDispatchAsync;

/**
 * Main entry point.
 *
 * @author Jan Pokorsky
 */
public class EditorEntryPoint implements EntryPoint {

    /**
     * Creates a new instance of EditorEntryPoint
     */
    public EditorEntryPoint() {
    }

    /**
     * The entry point method, called automatically by loading a module
     * that declares an implementing class as an entry-point
     */
    @Override
    public void onModuleLoad() {
        initializeUI();
    }

    private void initializeUI() {
        EditorPresenter editorPresenter = new EditorPresenter(
                EditorViewsFactory.getInstance().createEditorView(),
                new StandardDispatchAsync(new DefaultExceptionHandler()));
        RootLayoutPanel.get().add(editorPresenter.getDisplay().asWidget());
        editorPresenter.bind();
        openEditorsOnStrartup(editorPresenter);
    }

    private void openEditorsOnStrartup(EditorPresenter editorPresenter) {
        for (String pid : EditorConfiguration.getInstance().getStartupPIDs()) {
            editorPresenter.load(pid);
        }
    }

}