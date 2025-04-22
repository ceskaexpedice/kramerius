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
package cz.incad.kramerius.utils;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public class ITestsConstants {

    public static final String URL_LOCAL_SOLR = "http://localhost:8983/solr/search_v21/";

    public static final String MONOGRAPH_DELETE_TREE = " {\n" +
            "        \"root.pid\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"indexed\":\"2025-03-06T12:07:06.830Z\",\n" +
            "        \"name\":\"User trigger | Reharvest from admin client \",\n" +
            "        \"libraries\":[\"cbvk\"],\n" +
            "        \"pid\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"id\":\"cad43732-9bb8-4fe6-9cf6-b422aa1146d2\",\n" +
            "        \"state\":\"waiting\",\n" +
            "        \"type\":\"delete_tree\",\n" +
            "        \"own_pid_path\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"_version_\":1825846595198910464}";

    public static final String MONOGRAH_DELETE_ROOT = " {\n" +
            "        \"root.pid\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"indexed\":\"2025-03-06T12:07:06.830Z\",\n" +
            "        \"name\":\"User trigger | Reharvest from admin client \",\n" +
            "        \"libraries\":[\"cbvk\"],\n" +
            "        \"pid\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"id\":\"cad43732-9bb8-4fe6-9cf6-b422aa1146d2\",\n" +
            "        \"state\":\"waiting\",\n" +
            "        \"type\":\"delete_root\",\n" +
            "        \"own_pid_path\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"_version_\":1825846595198910464}";
    public static final String PERIODICAL_DELETE_TREE = " {\n" +
            "        \"root.pid\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"indexed\":\"2025-03-06T12:07:06.830Z\",\n" +
            "        \"name\":\"User trigger | Reharvest from admin client \",\n" +
            "        \"libraries\":[\"cbvk\"],\n" +
            "        \"pid\":\"uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f\",\n" +
            "        \"id\":\"cad43732-9bb8-4fe6-9cf6-b422aa1146d2\",\n" +
            "        \"state\":\"waiting\",\n" +
            "        \"type\":\"delete_tree\",\n" +
            "        \"own_pid_path\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f/uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f\",\n" +
            "        \"_version_\":1825846595198910464}";
    public static final String PERIODICAL_ROOT_CHILD = " {\n" +
            "        \"root.pid\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"indexed\":\"2025-03-06T12:07:06.830Z\",\n" +
            "        \"name\":\"User trigger | Reharvest from admin client \",\n" +
            "        \"libraries\":[\"cbvk\"],\n" +
            "        \"pid\":\"uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f\",\n" +
            "        \"id\":\"cad43732-9bb8-4fe6-9cf6-b422aa1146d2\",\n" +
            "        \"state\":\"waiting\",\n" +
            "        \"type\":\"root\",\n" +
            "        \"own_pid_path\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f/uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f\",\n" +
            "        \"_version_\":1825846595198910464}";
    public static final String ONLY_PID = " {\n" +
            "        \"root.pid\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"indexed\":\"2025-03-06T12:07:06.830Z\",\n" +
            "        \"name\":\"User trigger | Reharvest from admin client \",\n" +
            "        \"libraries\":[\"cbvk\"],\n" +
            "        \"pid\":\"uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f\",\n" +
            "        \"id\":\"cad43732-9bb8-4fe6-9cf6-b422aa1146d2\",\n" +
            "        \"state\":\"waiting\",\n" +
            "        \"type\":\"only_pid\",\n" +
            "        \"own_pid_path\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f/uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f\",\n" +
            "        \"_version_\":1825846595198910464}";
    public static final String DELETE_PID = " {\n" +
            "        \"root.pid\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f\",\n" +
            "        \"indexed\":\"2025-03-06T12:07:06.830Z\",\n" +
            "        \"name\":\"User trigger | Reharvest from admin client \",\n" +
            "        \"libraries\":[\"cbvk\"],\n" +
            "        \"pid\":\"uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f\",\n" +
            "        \"id\":\"cad43732-9bb8-4fe6-9cf6-b422aa1146d2\",\n" +
            "        \"state\":\"waiting\",\n" +
            "        \"type\":\"delete_pid\",\n" +
            "        \"own_pid_path\":\"uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f/uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f\",\n" +
            "        \"_version_\":1825846595198910464}";

    protected static CloseableHttpClient buildApacheClient() {
        return HttpClients.createDefault();
    }
}
