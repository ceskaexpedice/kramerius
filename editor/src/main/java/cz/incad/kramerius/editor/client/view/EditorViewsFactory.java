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

/**
 *
 * @author Jan Pokorsky
 */
public abstract class EditorViewsFactory {

    private static EditorViewsFactory INSTANCE = new DefaultFactory();

    public static EditorViewsFactory getInstance() {
        return INSTANCE;
    }

    public static void setInstance(EditorViewsFactory evf) {
        INSTANCE = evf;
    }

    public ContainerView createContainerView() {
        return INSTANCE.createContainerView();
    }

    public EditorView createEditorView() {
        return INSTANCE.createEditorView();
    }

    public ElementView createElementView() {
        return INSTANCE.createElementView();
    }

    public LoadView createLoadView() {
        return INSTANCE.createLoadView();
    }

    public RelationsView createRelationsView() {
        return INSTANCE.createRelationsView();
    }

    public <T> SaveView<T> createSaveView() {
        return INSTANCE.createSaveView();
    }

    private static final class DefaultFactory extends EditorViewsFactory {

        @Override
        public EditorView createEditorView() {
            return new EditorViewImpl();
        }

        @Override
        public ContainerView createContainerView() {
            return new ContainerViewImpl();
        }

        @Override
        public ElementView createElementView() {
            return new ElementViewImpl();
        }

        @Override
        public LoadView createLoadView() {
            return new LoadViewImpl();
        }

        @Override
        public RelationsView createRelationsView() {
            return new RelationsViewImpl();
        }

        @Override
        public <T> SaveView<T> createSaveView() {
            return new SaveViewImpl();
        }

    }
}