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

package cz.incad.kramerius.editor.server;

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;
import cz.incad.kramerius.editor.share.InputValidator;
import cz.incad.kramerius.editor.share.InputValidator.Validator;
import java.util.EnumMap;
import java.util.Map;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 *
 * @author Jan Pokorsky
 */
public final class EditorServerUtils {

    private static final Map<KrameriusModels, Kind> mapModel2Kind;
    private static final Map<Kind, KrameriusModels> mapKind2Model;
    private static final String UUID_PREFIX = "uuid:";
    static {
        mapModel2Kind = new EnumMap<KrameriusModels, Kind>(KrameriusModels.class);
        mapModel2Kind.put(KrameriusModels.PAGE, Kind.PAGE);
        mapModel2Kind.put(KrameriusModels.MONOGRAPH, Kind.MONOGRAPH);
        mapModel2Kind.put(KrameriusModels.MONOGRAPHUNIT, Kind.MONOGRAPH_UNIT);
        mapModel2Kind.put(KrameriusModels.PERIODICAL, Kind.PERIODICAL);
        mapModel2Kind.put(KrameriusModels.PERIODICALITEM, Kind.PERIODICAL_ITEM);
        mapModel2Kind.put(KrameriusModels.PERIODICALVOLUME, Kind.PERIODICAL_VOLUME);
        mapModel2Kind.put(KrameriusModels.INTERNALPART, Kind.INTERNAL_PART);
        mapModel2Kind.put(KrameriusModels.DONATOR, Kind.DONATOR);

        mapKind2Model = new EnumMap<Kind, KrameriusModels>(Kind.class);
        for (Map.Entry<KrameriusModels, Kind> entry : mapModel2Kind.entrySet()) {
            mapKind2Model.put(entry.getValue(), entry.getKey());
        }
    }

    public static KrameriusModels resolveKrameriusModel(Kind kind) {
        KrameriusModels res = mapKind2Model.get(kind);
        if (res == null) {
            throw new IllegalStateException("Unsupported kind: " + kind);
        }
        return res;
    }

    public static Kind resolveKind(KrameriusModels model) {
        Kind res = mapModel2Kind.get(model);
        if (res == null) {
            throw new IllegalStateException("Unsupported model: " + model);
        }
        return res;
    }

    public static String resolveUUID(String pid) {
        if (pid != null && pid.startsWith(UUID_PREFIX)) {
            return pid.substring(UUID_PREFIX.length());
        }
        return null;
    }

    public static String validatePID(String pid) throws ActionException {
        return validatePID(pid, false);
    }

    public static String validatePID(String pid, boolean isRelation) throws ActionException {
        Validator<String> validator = InputValidator.validatePID(pid);
        if (!validator.isValid()) {
            // donator relations do not use UUID
            if (isRelation && pid != null && pid.startsWith("donator:")) {
                return pid;
            }
            throw new ActionException("Invalid PID :" + pid);
        }
        return validator.getNormalized();
    }

}