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

import com.google.gwt.i18n.client.Dictionary;

/**
 * Provides various properties of the editor.
 * <p>The best place to declare GWT-static properties seems to be the host page
 * {@code editor.jsp}. See {@code EditorConfiguration} associative array.</p>
 *
 * @author Jan Pokorsky
 */
public final class EditorConfiguration {

    private static final EditorConfiguration INSTANCE = new EditorConfiguration();

    private EditorConfiguration() {
    }

    public static EditorConfiguration getInstance() {
        return INSTANCE;
    }

    public String getKrameriusURL() {
        Dictionary config = fetch();
        String krameriusURL = config.get("krameriusURL");
        if (!krameriusURL.endsWith("/")) {
            krameriusURL += '/';
        }
        return krameriusURL;
    }

    public String getPreviewURL(String uuid) {
//        return "http://localhost:8080/search/djvu?uuid=" + uuid + "&scaledWidth=450";
        return getKrameriusURL() + "djvu?uuid=" + uuid + "&scaledWidth=450";
    }

    public String getThumbnailURL(String uuid) {
//        return "http://localhost:8080/search/titlePage?uuid=" + uuid;
        return getKrameriusURL() + "titlePage?uuid=" + uuid;
    }

    private static Dictionary fetch() {
        Dictionary dictionary = Dictionary.getDictionary("EditorConfiguration");
        return dictionary;
    }

}
