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

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import java.util.Map;

/**
 * Makes available i18n constants.
 *
 * @author Jan Pokorsky
 */
@Generate(format="com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface EditorConstants extends Constants {

    @DefaultStringValue("Load")
    @Description("title of the Load window")
    String loadViewTitle();

    @DefaultStringValue("Close Tab")
    String closeTabHandleTooltip();

    @DefaultStringValue("Nothing to save.")
    @Description("SaveView notification that there is nothing to save")
    String nothingToSaveLabel();

    @DefaultStringValue("Save changes")
    @Description("title of the Save window")
    String saveViewTitle();

    @DefaultStringMapValue({
            "MONOGRAPH", "Monograph",
            "MONOGRAPH_UNIT", "Physical Unit",
            "PERIODICAL", "Periodical",
            "PERIODICAL_VOLUME", "Volume",
            "PERIODICAL_ITEM", "Issue",
            "PAGE", "Page",
            "INTERNAL_PART", "Internal part",
            "DONATOR", "Donator"
    })
    Map<String, String> krameriusObjectKinds();

    @DefaultStringMapValue({
            "RELATION_MONOGRAPH", "Monographs",
            "RELATION_MONOGRAPH_UNIT", "Physical Units",
            "RELATION_PERIODICAL", "Periodicals",
            "RELATION_PERIODICAL_VOLUME", "Volumes",
            "RELATION_PERIODICAL_ITEM", "Issues",
            "RELATION_PAGE", "Pages",
            "RELATION_INTERNAL_PART", "Internal parts",
            "RELATION_DONATOR", "Donators"
    })
    Map<String, String> krameriusRelationTabNames();

}
