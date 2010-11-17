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

/**
 * Main view of the application.
 *
 * @author Jan Pokorsky
 */
public interface EditorView extends Display {

    /**
     * Opens and selects component.
     * @param item
     * @param name
     */
    void add(Display item, String name);

    void remove(Display item);
    
    void select(Display item);

    /**
     * Gets selected component.
     *
     * @return {@code null} if none is selected.
     */
    Display getSelected();

//    Collection<Display> getAll();
    /**
     * Registers listener that is notified about user actions.
     *
     * @param c listener
     */
    void setCallback(Callback c);
    
    void setLanguages(String[] languages, int selected);

    /**
     * Adds clipboard component
     * @param clipboard
     */
    void setClipboard(Display clipboard);

    void setModified(Display view, boolean modified);

    public interface Callback {

        void onLoadClick();

        void onSaveClick();

        void onKrameriusClick();
        
        void onLanguagesClick(int index);

        void onEditorTabClose(Display item);
    }
}