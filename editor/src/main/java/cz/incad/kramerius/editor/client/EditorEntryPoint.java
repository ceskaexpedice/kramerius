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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import cz.incad.kramerius.editor.client.presenter.EditorPresenter;
import cz.incad.kramerius.editor.share.InputValidator;
import cz.incad.kramerius.editor.share.InputValidator.Validator;
import cz.incad.kramerius.editor.client.view.EditorViewsFactory;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import net.customware.gwt.dispatch.client.DefaultExceptionHandler;
import net.customware.gwt.dispatch.client.standard.StandardDispatchAsync;

/**
 * Main entry point.
 *
 * The editor accepts parameters:
 * <ul>
 * <li>openIDs - comma delimited pids to be open on application startup</li>
 * </ul>
 *
 * XXX params are passed in URL query for now. Later they should be put to host page (jsp/servlet)
 *
 * @author Jan Pokorsky
 */
public class EditorEntryPoint implements EntryPoint {

    private static final int OPENIDS_MAX_COUNT = 10;
    private static final String OPENIDS_PARAM = "openIDs";

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
        processRequestParams(editorPresenter);
    }

    private void processRequestParams(EditorPresenter editorPresenter) {
        String openIDs = Window.Location.getParameter(OPENIDS_PARAM);
        for (String pid : parseOpenIDsParameter(openIDs)) {
            editorPresenter.load(pid);
        }
    }

    //junit access
    Collection<String> parseOpenIDsParameter(String openIDs) {
        Set<String> pids = new LinkedHashSet<String>();
        if (openIDs != null && openIDs.length() >= 0) {
            for (String openID : openIDs.split(",", OPENIDS_MAX_COUNT + 1)) {
                if (pids.size() >= OPENIDS_MAX_COUNT) {
                    return pids;
                }
                Validator<String> pidValidator = InputValidator.validatePID(openID);
                if (pidValidator.isValid()) {
                    pids.add(pidValidator.getNormalized());
                }
            }
        }
        return pids;
    }

}