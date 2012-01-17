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
import cz.incad.kramerius.editor.share.InputValidator;
import cz.incad.kramerius.editor.share.InputValidator.Validator;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides various properties of the editor.
 * <p>The best place to declare GWT-static properties seems to be the host page
 * {@code editor.jsp}. See {@code EditorConfiguration} associative array.</p>
 * <p>The editor accepts following parameters:
 * <ul>
 * <li><b>pids</b> - comma delimited pids to be open on application startup</li>
 * <li><b>krameriusURL</b> - URL of the Kramerius application</li>
 * </ul></p>
 *
 * @author Jan Pokorsky
 */
public final class EditorConfiguration {

    private static final EditorConfiguration INSTANCE = new EditorConfiguration();
    private static final int OPENIDS_MAX_COUNT = 10;

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

    public String getPreviewURL(String pid) {
        return getKrameriusURL() + "img?pid=" + pid + "&stream=IMG_FULL&action=SCALE&scaledWidth=450";
    }

    public String getThumbnailURL(String pid) {
        return getKrameriusURL() + "img?pid=" + pid + "&stream=IMG_THUMB";//&action=SCALE&scaledHeight=96";
    }

    public Collection<String> getStartupPIDs() {
        Dictionary fetched = fetch();
        String pids = fetched.get("pids");
        return parseOpenIDsParameter(pids);
    }

    private static Dictionary fetch() {
        Dictionary dictionary = Dictionary.getDictionary("EditorConfiguration");
        return dictionary;
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
