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
package cz.incad.kramerius.editor.share.rpc;

import cz.incad.kramerius.editor.client.view.ViewUtils;
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;
import net.customware.gwt.dispatch.shared.Result;

/**
 *
 * @author Jan Pokorsky
 */
public final class GetSuggestionResult extends SuggestOracle.Response implements Result {

    private boolean serverError = false;

    public static final class Suggestion implements SuggestOracle.Suggestion, IsSerializable {

        private transient String pid;
        private String uuid;
        private String title;
        private Kind kind;
        private transient String displayString;

        /* gwt serialization purposes */
        private Suggestion() {
        }

        public Suggestion(String uuid, String title, Kind kind) {
            this.uuid = uuid;
            this.title = title;
            this.kind = kind;
        }

        public Kind getKind() {
            return kind;
        }

        public String getPid() {
            if (pid == null) {
                pid = "uuid:" + uuid;
            }
            return pid;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
//            return String.format("Suggestion[%s, %s, %s]", pid, kind, title);
            return "Suggestion["
                    + uuid
                    + ", " + kind
                    + ", " + title
                    + "]";
        }

        @Override
        public String getDisplayString() {
            initDisplayString();
            return displayString;
        }

        @Override
        public String getReplacementString() {
            return title;
        }

        private void initDisplayString() {
            if (displayString != null) {
                return;
            }
            displayString = "<b>" + ViewUtils.makeLabelVisible(title, 50) + "</b>"
                    + "<br/>" + kind.toLocalizedString()
                    + "<br/>" + uuid;
        }
    }

    /* gwt serialization purposes */
    public GetSuggestionResult() {
    }

    public void setServerFailure() {
        this.serverError = true;
    }

    public boolean isServerFailure() {
        return this.serverError;
    }

}
