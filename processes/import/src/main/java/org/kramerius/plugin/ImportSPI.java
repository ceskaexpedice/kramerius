/*
 * Copyright (C) 2025 Inovatika
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
package org.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.PluginSpi;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.common.model.PayloadFieldSpec;
import org.ceskaexpedice.processplatform.common.model.PayloadFieldType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ImportSPI
 * @author ppodsednik
 */
public class ImportSPI implements PluginSpi {

    @Override
    public String getPluginId() {
        return "import";
    }

    @Override
    public String getDescription() {
        return "Import";
    }

    @Override
    public String getMainClass() {
        return "org.kramerius.Import";
    }

    @Override
    public Map<String, PayloadFieldSpec> getPayloadSpec() {
        Map<String, PayloadFieldSpec>  map = new HashMap<>();
        map.put("importDir", new PayloadFieldSpec(PayloadFieldType.STRING, false));
        map.put("startIndexer", new PayloadFieldSpec(PayloadFieldType.BOOLEAN, false));
        map.put("license", new PayloadFieldSpec(PayloadFieldType.STRING, false));
        map.put("addCollection", new PayloadFieldSpec(PayloadFieldType.STRING, false));
        map.put("scheduleStrategy", new PayloadFieldSpec(PayloadFieldType.STRING, false));
        return map;
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of("???");
    } // TODO

}
