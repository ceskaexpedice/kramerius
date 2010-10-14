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

import cz.incad.kramerius.editor.client.presenter.Presenter.Display;
import java.util.List;

/**
 *
 * @author Jan Pokorsky
 */
public interface SaveView<T> extends Display {

    void setSaveables(List<T> saveables);

    List<T> getSelected();

    void setCallback(Callback callback);

    void setRenderer(Renderer<String, T> renderer);

    void setDiscardable(boolean discard);

    void show();

    void hide();

    public interface Callback {

        void onSaveViewCommit(boolean discard);
    }
}
