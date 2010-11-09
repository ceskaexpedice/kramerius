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

import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

/**
 * Makes available i18n messages.
 * 
 * @author Jan Pokorsky
 */
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface EditorMessages extends Messages {

    @DefaultMessage("Remote connection failed: {0}")
    @Description("Notification about unexpected RPC failure.")
    String remoteConnectionFailure(String message);

    @DefaultMessage("Cannot save {0} objects:\n{1}")
    @PluralText({"one", "Cannot save object:\n{1}"})
    @Description("After Save action message.")
    String cannotSaveObject(@PluralCount int objectCount, String objectList);

    @DefaultMessage("Please enter a valid PID (uuid:<UUID>).")
    @Description("Error message when validation of user typed PID fails. uuid:<UUID> is a valid format.")
    String enterValidPid();

    @DefaultMessage("Server failure. Please contact administrator.")
    @Description("Notification about checked failure like Fedora is not running.")
    String serverQueryFailure();

}
