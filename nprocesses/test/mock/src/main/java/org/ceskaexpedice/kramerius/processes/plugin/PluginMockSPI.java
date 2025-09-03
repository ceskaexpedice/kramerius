/*
 * Copyright (C) 2025  Inovatika
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
package org.ceskaexpedice.kramerius.processes.plugin;

import com.google.auto.service.AutoService;
import org.ceskaexpedice.processplatform.api.PluginSpi;
import org.ceskaexpedice.processplatform.common.model.PayloadFieldSpec;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@AutoService(PluginSpi.class)
public class PluginMockSPI implements PluginSpi {

    private final Properties props = new Properties();

    public PluginMockSPI() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("plugin.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                throw new IllegalStateException("Missing plugin.properties on classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load plugin.properties", e);
        }
    }

    @Override
    public String getPluginId() {
        return props.getProperty("plugin.id");
    }

    @Override
    public String getDescription() {
        return props.getProperty("plugin.description");
    }

    @Override
    public String getMainClass() {
        return props.getProperty("plugin.mainClass");
    }

    @Override
    public Map<String, PayloadFieldSpec> getPayloadSpec() {
        return new HashMap<>();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        String profiles = props.getProperty("plugin.scheduledProfiles", "");
        if (profiles.isBlank()) {
            return Set.of();
        }
        return Set.of(profiles.split(","));
    }
}