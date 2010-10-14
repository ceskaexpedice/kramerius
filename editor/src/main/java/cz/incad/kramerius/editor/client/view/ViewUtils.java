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
public final class ViewUtils {

    public static final int MAX_TEXT_LENGTH = 30;
    private static final String TEXT_SUFFIX = "...";

    public static String makeLabelVisible(String label) {
        return makeLabelVisible(label, MAX_TEXT_LENGTH);
    }

    public static String makeLabelVisible(String label, int maxLength) {
        if (label.length() > maxLength) {
            label = label.substring(0, maxLength - TEXT_SUFFIX.length())
                    + TEXT_SUFFIX;
        }
        return label;
    }

}