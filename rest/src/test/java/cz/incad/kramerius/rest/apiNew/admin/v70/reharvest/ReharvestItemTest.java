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
package cz.incad.kramerius.rest.apiNew.admin.v70.reharvest;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.UUID;

public class ReharvestItemTest {

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



    @Test
    public void testReharvest() throws ParseException {
        ReharvestItem item = ReharvestItem.fromJSON(new JSONObject(MONOGRAPH_DELETE_TREE));
        Assert.assertTrue(item.getRootPid().equals("uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f"));
        Assert.assertTrue(item.getLibraries().equals(Arrays.asList("cbvk")));
        Assert.assertTrue(item.getPid().equals("uuid:c5e400b0-b1ab-11eb-a22e-5ef3fc9bb22f"));
        Assert.assertTrue(item.getTypeOfReharvest().equals(ReharvestItem.TypeOfReharvset.delete_tree));
        Assert.assertTrue(item.getState().equals("waiting"));

        ReharvestItem deleteRoot = new ReharvestItem(UUID.randomUUID().toString());
        deleteRoot.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_root);
        deleteRoot.setRootPid("compositeId");
        deleteRoot.setPid("compositeId");
        deleteRoot.setOwnPidPath("compositeId");
        Assert.assertTrue(deleteRoot.toJSON() != null);
        Assert.assertTrue(deleteRoot.toJSON().getString("root.pid").equals("compositeId"));
        Assert.assertTrue(deleteRoot.toJSON().getString("pid").equals("compositeId"));
        Assert.assertTrue(deleteRoot.toJSON().getString("type").equals("delete_root"));

    }
}
