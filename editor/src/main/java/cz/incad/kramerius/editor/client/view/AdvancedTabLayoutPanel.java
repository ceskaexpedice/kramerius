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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

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

    private final TabLayoutPanel delegate;

    public interface Resources extends ClientBundle {
//        @CssResource.NotStrict
        @Source("AdvancedTabLayoutPanel.css")
        Style css();
    }

    public interface Style extends CssResource {

        String modified();
        String tabCloseButton();
    }

    private Style style;

    @UiConstructor
    public AdvancedTabLayoutPanel(String barHeight, String barUnit) {
        this(Double.parseDouble(barHeight), Unit.valueOf(barUnit));
    }

    public AdvancedTabLayoutPanel(double barHeight, Unit barUnit) {
        delegate = new TabLayoutPanel(barHeight, barUnit);
        Resources bundle = GWT.<Resources>create(Resources.class);
        this.style = bundle.css();
        this.style.ensureInjected();
        initWidget(delegate);
    }

    public void add(Widget child, String text, boolean closeable) {
        Widget advancedTab;
        if (closeable) {
            advancedTab = new AdvancedTab(text, createCloseTabWidget(child));
        } else {
            advancedTab = new AdvancedTab(text);
        }
        delegate.add(child, advancedTab);
    }

    public void insert(Widget child, String text, int beforeIndex, boolean closeable) {
        Widget advancedTab;
        if (closeable) {
            advancedTab = new AdvancedTab(text, createCloseTabWidget(child));
        } else {
            advancedTab = new AdvancedTab(text);
        }
        delegate.insert(child, advancedTab, beforeIndex);
    }

    /**
     * @see TabLayoutPanel#remove(int)
     */
    public boolean remove(int index) {
        return delegate.remove(index);
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
        if (tabWidget != null) {
            tabWidget.setTabText(text);
        } else {
            delegate.setTabText(index, text);
        }
    }

    @Override
    public HandlerRegistration addCloseHandler(CloseHandler<Integer> handler) {
        return addHandler(handler, CloseEvent.getType());
    }

    public void setModified(int index, boolean modified) {
        AdvancedTab tabWidget = AdvancedTab.get(delegate.getTabWidget(index));
        if (tabWidget != null) {
            Widget tabLabel = tabWidget.getTabLabel();
            if (modified) {
                tabLabel.getElement().addClassName(this.style.modified());
            } else {
                tabLabel.getElement().removeClassName(this.style.modified());
            }
            tabWidget.setModifiedTab(modified);
        }
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

    private Widget createCloseTabWidget(final Widget child) {
        // XXX fix: temporary solution to remove the tab
        Label closeHandle = new Label("\u2718");
        closeHandle.setTitle("Close tab");
        closeHandle.addStyleName(this.style.tabCloseButton());
        closeHandle.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                int selectedIndex = getSelectedIndex();
                int widgetIndex = delegate.getWidgetIndex(child);
                if (selectedIndex != widgetIndex) {
                    // closing unselected tab; select first
                    selectTab(widgetIndex);
                    selectedIndex = getSelectedIndex();
                    if (selectedIndex != widgetIndex) {
                        // ignore; selection was vetoed
                        return ;
                    }
                }
                CloseEvent.fire(AdvancedTabLayoutPanel.this, selectedIndex, false);
            }
        });
        return closeHandle;
    }

    private static final class AdvancedTab extends HorizontalPanel {
        private final Label label;
        private boolean modified;

        public AdvancedTab(String text) {
            this(text, null);
        }
        public AdvancedTab(String text, Widget closer) {
            this.label = new Label(ViewUtils.makeLabelVisible(text));
            this.label.setTitle(text);
            add(this.label);
            if (closer != null) {
                add(closer);
            }
        }

        public Widget getTabLabel() {
            return label;
        }

        public boolean isModifiedTab() {
            return modified;
        }

        public void setModifiedTab(boolean modified) {
            this.modified = modified;
        }

        private void setTabText(String text) {
            this.label.setText(ViewUtils.makeLabelVisible(text));
            this.label.setTitle(text);
        }

        public static AdvancedTab get(Widget tabWidget) {
            return tabWidget instanceof AdvancedTab
                    ? (AdvancedTab) tabWidget
                    : null;
        }

    }

}
