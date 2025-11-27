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
package cz.incad.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.PluginSpi;
import org.ceskaexpedice.processplatform.common.model.PayloadFieldSpec;
import org.ceskaexpedice.processplatform.common.model.PayloadFieldType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ImportSPI
 * @author ppodsednik
 */
public class CDKMigrationSPI implements PluginSpi {

    @Override
    public String getPluginId() {
        return "migrationCDK";
    }

    @Override
    public String getDescription() {
        return "Migration CDK";
    }

    @Override
    public String getMainClass() {
        return "cz.incad.kramerius.services.CDKMigration";
    }

    @Override
    public Map<String, PayloadFieldSpec> getPayloadSpec() {
        Map<String, PayloadFieldSpec>  map = new HashMap<>();
        map.put("configSource", new PayloadFieldSpec(PayloadFieldType.STRING, true));
        map.put("destinationUrl", new PayloadFieldSpec(PayloadFieldType.BOOLEAN, true));
        map.put("iterationDl", new PayloadFieldSpec(PayloadFieldType.STRING, true));
        map.put("iterationId", new PayloadFieldSpec(PayloadFieldType.STRING, true));
        map.put("iterationUrl", new PayloadFieldSpec(PayloadFieldType.STRING, true));
        map.put("showConfigurationOnly", new PayloadFieldSpec(PayloadFieldType.STRING, false));
        return map;
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return null;
    }

}
