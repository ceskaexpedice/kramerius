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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Makes possible to bring TabLayoutPanel implementations via deferred binding.
 * See Editor.gwt.xml.
 *
 * @author Jan Pokorsky
 */
public class TabLayoutPanelFactory {

    public TabLayoutPanel create(double barHeight, Unit barUnit) {
        return new TabLayoutPanel(barHeight, barUnit);
    }

    public static class TabLayoutPanelFactoryIE6 extends TabLayoutPanelFactory {

        @Override
        public TabLayoutPanel create(double barHeight, Unit barUnit) {
            return new TabLayoutPanelIE6(barHeight, barUnit);
        }

    }

    private static class TabLayoutPanelIE6 extends TabLayoutPanel {

        public TabLayoutPanelIE6(double barHeight, Unit barUnit) {
            super(barHeight, barUnit);
        }

        @Override
        public void selectTab(int index) {
            final int selectedIndex = super.getSelectedIndex();
            super.selectTab(index);

            // Update the tabs being selected and unselected.
            if (selectedIndex != -1) {
                Widget formelySelectedTab = super.getWidget(selectedIndex);
                setWidgetVisible(formelySelectedTab, false);
            }
            Widget newlySelectedTab = super.getWidget(index);
            setWidgetVisible(newlySelectedTab, true);
        }

        /**
         * XXX HACK HACK -- GWT BUG FIX - REMOVE WHEN UPGRADING GWT.
         * The content of newly selected tab shrinks.
         * Issue: http://code.google.com/p/google-web-toolkit/issues/detail?id=4596
         * Patch: http://code.google.com/p/google-web-toolkit/source/detail?r=7649#
         */
        private static void setWidgetVisible(Widget child, boolean visible) {
            LayoutPanel layoutPanel = (LayoutPanel) child.getParent();
            Element container = layoutPanel.getWidgetContainerElement(child);
            LayoutPanel.setVisible(container, visible);
            child.setVisible(visible);
            layoutPanel.animate(0);
        }
    }

}
