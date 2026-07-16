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

import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Ignore
public class ReharvestUtilsITTest_FindPidByTypeSmoke {

    @Test
    public void testFindPidByTypeSmoke_ONLY_PID() throws ParseException {
        ReharvestItem item = ReharvestItem.fromJSON(new JSONObject(ITestsConstants.ONLY_PID));
        try (CloseableHttpClient client = ITestsConstants.buildApacheClient()) {
            Map<String, String> iteration = new HashMap<>();
            iteration.put("type", "CURSOR");
            iteration.put("rows", "10");
            iteration.put("url", ITestsConstants.URL_LOCAL_SOLR);

            List<Pair<String, String>> pairs = ReharvestUtils.findPidByType(iteration, client, item, 10);
            List<String> pids = pairs.stream().map(Pair::getLeft).collect(Collectors.toList());

            Assert.assertEquals(1, pairs.size());
            Assert.assertTrue(pids.contains("uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f"));
            Assert.assertEquals("uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f", pairs.get(0).getLeft());
            Assert.assertTrue(pairs.get(0).getRight().endsWith("!uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
