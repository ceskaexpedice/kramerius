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

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.incad.kramerius.editor.client.EditorConstants;

/**
 * Improves TabLayoutPanel with new features like tab close handle or
 * tab state presentation.
 * 
 * <p>The composition instead of inheritance was chosen to be able to use
 * the class with UiBinder. Unfortunately it is not possible to declare
 * tab children in ui.xml now.
 * See <a href='http://code.google.com/p/google-web-toolkit/issues/detail?id=4342'>
 * gwt issue 4342</a> for details.</p>
 *
 * @author Jan Pokorsky
 */
public class AdvancedTabLayoutPanel extends Composite implements HasCloseHandlers<Integer> {

    private static final EditorConstants I18N = GWT.create(EditorConstants.class);
    private final TabLayoutPanel delegate;
    private int lastSelectedIndex = -1;

    public interface Resources extends ClientBundle {
//        @CssResource.NotStrict
        @Source("AdvancedTabLayoutPanel.css")
        Style css();
    }

    public interface Style extends CssResource {

        String modified();
        String tabCloseButton();
        String tabCloseButtonSelected();
    }

    private Style style;

    @UiConstructor
    public AdvancedTabLayoutPanel(String barHeight, String barUnit) {
        this(Double.parseDouble(barHeight), Unit.valueOf(barUnit));
    }

    public AdvancedTabLayoutPanel(double barHeight, Unit barUnit) {
        TabLayoutPanelFactory factory = GWT.create(TabLayoutPanelFactory.class);
        delegate = factory.create(barHeight, barUnit);
//        delegate = new TabLayoutPanel(barHeight, barUnit);
        Resources bundle = GWT.<Resources>create(Resources.class);
        this.style = bundle.css();
        this.style.ensureInjected();
        initWidget(delegate);

        delegate.addSelectionHandler(new SelectionHandler<Integer>() {

            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                int oldIndex = lastSelectedIndex;
                lastSelectedIndex = event.getSelectedItem();
                followDelegateSelection(oldIndex, lastSelectedIndex);
            }
        });
    }

    public void add(Widget child, String text, boolean closeable) {
        Widget advancedTab;
        if (closeable) {
            advancedTab = new AdvancedTab(text, createCloseTabWidget(child), style);
        } else {
            advancedTab = new AdvancedTab(text, style);
        }
        delegate.add(child, advancedTab);
    }

    public void insert(Widget child, String text, int beforeIndex, boolean closeable) {
        Widget advancedTab;
        if (closeable) {
            advancedTab = new AdvancedTab(text, createCloseTabWidget(child), style);
        } else {
            advancedTab = new AdvancedTab(text, style);
        }
        delegate.insert(child, advancedTab, beforeIndex);
    }

    /**
     * @see TabLayoutPanel#remove(int)
     */
    public boolean remove(int index) {
        int selectedIndex = getSelectedIndex();
        boolean removed = delegate.remove(index);

        if (removed) {
            reselectIfNecessary(index, selectedIndex);
        }
        return removed;
    }

    /**
     * @see TabLayoutPanel#remove(com.google.gwt.user.client.ui.Widget)
     */
    public boolean remove(Widget w) {
        return delegate.remove(w);
    }

    /**
     * @see TabLayoutPanel#setTabText(int, java.lang.String) 
     */
    public void setTabText(int index, String text) {
        AdvancedTab tabWidget = AdvancedTab.get(delegate.getTabWidget(index));
        tabWidget.setTabText(text);
    }

    @Override
    public HandlerRegistration addCloseHandler(CloseHandler<Integer> handler) {
        return addHandler(handler, CloseEvent.getType());
    }

    public void setModified(int index, boolean modified) {
        AdvancedTab tabWidget = AdvancedTab.get(delegate.getTabWidget(index));
        Widget tabLabel = tabWidget.getTabLabel();
        if (modified) {
            tabLabel.getElement().addClassName(this.style.modified());
        } else {
            tabLabel.getElement().removeClassName(this.style.modified());
        }
        tabWidget.setModifiedTab(modified);
    }

    /**
     * @see TabLayoutPanel#getSelectedIndex()
     */
    public int getSelectedIndex() {
        return delegate.getSelectedIndex();
    }

    /**
     * @see TabLayoutPanel#selectTab(int)
     */
    public void selectTab(int index) {
        delegate.selectTab(index);
    }

    /**
     * @see TabLayoutPanel#selectTab(com.google.gwt.user.client.ui.Widget) 
     */
    public void selectTab(Widget child) {
        delegate.selectTab(child);
    }

    /**
     * @see TabLayoutPanel#addSelectionHandler(com.google.gwt.event.logical.shared.SelectionHandler)
     */
    public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
        return delegate.addSelectionHandler(handler);
    }

    private void followDelegateSelection(int oldIndex, int newIndex) {
        if (oldIndex >= 0 && oldIndex < delegate.getWidgetCount()) {
            AdvancedTab tabWidget = AdvancedTab.get(delegate.getTabWidget(oldIndex));
            tabWidget.setSelected(false);
        }
        AdvancedTab tabWidget = AdvancedTab.get(delegate.getTabWidget(newIndex));
        tabWidget.setSelected(true);
    }

    /**
     * It is necessary to adjust tab selection as TabLayoutPanel.remove always
     * selects index 0 when index == selectedIndex
     *
     * @param removedIndex removed tab
     * @param selectedIndex tab selected before remove
     */
    private void reselectIfNecessary(int removedIndex, int selectedIndex) {
        int select = -1;
        if (removedIndex == selectedIndex) {
            if (selectedIndex > 0) {
                select = selectedIndex - 1;
            } else if (delegate.getWidgetCount() > 0) {
                // ignore
                // select = 0;
            }
        } else {
            // ignore
            // select = selectedIndex < removedIndex ? selectedIndex : selectedIndex - 1;
        }
        if (select >= 0) {
            delegate.selectTab(select);
        }
    }

    private Widget createCloseTabWidget(final Widget child) {
        Label closeHandle = new InlineLabel();
        closeHandle.setTitle(I18N.closeTabHandleTooltip());
        closeHandle.addStyleName("ui-icon ui-icon-close");
        closeHandle.addStyleName(this.style.tabCloseButton());
        closeHandle.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int widgetIndex = delegate.getWidgetIndex(child);
                CloseEvent.fire(AdvancedTabLayoutPanel.this, widgetIndex, false);
            }
        });
        return closeHandle;
    }

    /**
     * do not extend FlowPanel as it does not work with IE7 and older
     */
    private static final class AdvancedTab extends HorizontalPanel {
        private final Label label;
        private final Widget closer;
        private boolean modified;
        private final Style style;

        public AdvancedTab(String text, Style style) {
            this(text, null, style);
        }
        public AdvancedTab(String text, Widget closer, Style style) {
            this.label = new InlineLabel(ViewUtils.makeLabelVisible(text));
            this.label.setTitle(text);
            add(this.label);
            this.closer = closer;
            if (closer != null) {
                add(closer);
            }
            this.style = style;
        }

        public Widget getTabLabel() {
            return label;
        }

        public Widget getTabCloser() {
            return closer;
        }

        public boolean isModifiedTab() {
            return modified;
        }

        public void setModifiedTab(boolean modified) {
            this.modified = modified;
        }

        public void setSelected(boolean selected) {
            if (closer != null) {
                if (selected) {
                    closer.addStyleName(style.tabCloseButtonSelected());
                } else {
                    closer.removeStyleName(style.tabCloseButtonSelected());
                }
            }
        }

        private void setTabText(String text) {
            this.label.setText(ViewUtils.makeLabelVisible(text));
            this.label.setTitle(text);
        }

        public static AdvancedTab get(Widget tabWidget) {
            return (AdvancedTab) tabWidget;
        }

    }

}
