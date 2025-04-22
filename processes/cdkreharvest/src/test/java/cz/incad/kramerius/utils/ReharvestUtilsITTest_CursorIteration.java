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
public class ReharvestUtilsITTest_CursorIteration {

    @Test
    public void testReharvestUtilsITTest_DELETE_TREE_ROOT() throws ParseException {
        ReharvestItem item = ReharvestItem.fromJSON(new JSONObject(ITestsConstants.MONOGRAPH_DELETE_TREE));
        try(CloseableHttpClient client = ITestsConstants.buildApacheClient()) {
            Map<String, String> map = new HashMap<>();
            map.put("url", ITestsConstants.URL_LOCAL_SOLR);
            List<Pair<String, String>> pairs = ReharvestUtils.findPidByType(map, client, item, 4000);

            List<String> pids = pairs.stream().map(Pair::getKey).collect(Collectors.toList());

            Assert.assertTrue(pids.size() == 3276);
            Assert.assertTrue(pids.contains("uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f"));
            Assert.assertTrue(pids.contains("uuid:001a52c0-8591-11ec-b93a-5ef3fc9bb22f"));

            System.out.println(pids.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testReharvestUtilsITTest_DELETE_ROOT_ROOT() throws ParseException {

        ReharvestItem item = ReharvestItem.fromJSON(new JSONObject(ITestsConstants.MONOGRAH_DELETE_ROOT));
        try(CloseableHttpClient client = ITestsConstants.buildApacheClient()) {
            Map<String, String> map = new HashMap<>();
            map.put("url", ITestsConstants.URL_LOCAL_SOLR);
            List<Pair<String, String>> pairs = ReharvestUtils.findPidByType(map, client, item, 4000);
            List<String> pids = pairs.stream().map(Pair::getKey).collect(Collectors.toList());

            Assert.assertTrue(pids.size() == 3276);
            Assert.assertTrue(pids.contains("uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f"));
            Assert.assertTrue(pids.contains("uuid:001a52c0-8591-11ec-b93a-5ef3fc9bb22f"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReharvestUtilsITTest_DELETE_TREE_CHILD() throws ParseException {

        ReharvestItem item = ReharvestItem.fromJSON(new JSONObject(ITestsConstants.PERIODICAL_DELETE_TREE));
        try(CloseableHttpClient client = ITestsConstants.buildApacheClient()) {
            Map<String, String> map = new HashMap<>();
            map.put("url", ITestsConstants.URL_LOCAL_SOLR);
            List<Pair<String, String>> pairs = ReharvestUtils.findPidByType(map, client, item, 4000);
            List<String> pids = pairs.stream().map(Pair::getKey).collect(Collectors.toList());
            Assert.assertTrue(pids.contains("uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f"));
            Assert.assertFalse(pids.contains("uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReharvestUtilsITTest_ROOT_CHILD() throws ParseException {

        ReharvestItem item = ReharvestItem.fromJSON(new JSONObject(ITestsConstants.PERIODICAL_ROOT_CHILD));
        try(CloseableHttpClient client = ITestsConstants.buildApacheClient()) {
            Map<String, String> map = new HashMap<>();
            map.put("url", ITestsConstants.URL_LOCAL_SOLR);
            List<Pair<String, String>> pairs = ReharvestUtils.findPidByType(map, client, item, 4000);
            List<String> pids = pairs.stream().map(Pair::getKey).collect(Collectors.toList());

            Assert.assertTrue(pids.size() == 3276);
            Assert.assertTrue(pids.contains("uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f"));
            Assert.assertTrue(pids.contains("uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReharvestUtilsITTest_ONLY_PID() throws ParseException {
        ReharvestItem item = ReharvestItem.fromJSON(new JSONObject(ITestsConstants.ONLY_PID));
        try(CloseableHttpClient client = ITestsConstants.buildApacheClient()) {
            Map<String, String> map = new HashMap<>();
            map.put("url", ITestsConstants.URL_LOCAL_SOLR);
            List<Pair<String, String>> pairs = ReharvestUtils.findPidByType(map, client, item, 4000);
            List<String> pids = pairs.stream().map(Pair::getKey).collect(Collectors.toList());

            Assert.assertTrue(pids.size() == 1);
            Assert.assertTrue(pids.contains("uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testReharvestUtilsITTest_DELETE_PID() throws ParseException {
        ReharvestItem item = ReharvestItem.fromJSON(new JSONObject(ITestsConstants.DELETE_PID));
        try(CloseableHttpClient client = ITestsConstants.buildApacheClient()) {
            Map<String, String> map = new HashMap<>();
            map.put("url", ITestsConstants.URL_LOCAL_SOLR);
            List<Pair<String, String>> pairs = ReharvestUtils.findPidByType(map, client, item, 4000);
            List<String> pids = pairs.stream().map(Pair::getKey).collect(Collectors.toList());

            Assert.assertTrue(pids.size() == 1);
            Assert.assertTrue(pids.contains("uuid:d4d42790-84ea-11ec-b436-5ef3fc9bb22f"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
