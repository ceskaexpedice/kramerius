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

import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jan Pokorsky
 */
public class EditorConfigurationTest {

    public EditorConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testOpenIDsParameterIsEmpty() {
        String openIDs = "";
        String[] exp = {};
        doTestParseOpenIDsParameter(openIDs, exp);
    }

    @Test
    public void testOpenIDsParameterWithUUID() {
        String openIDs = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6";
        String[] exp = {"uuid:0eaa6730-9068-11dd-97de-000d606f5dc6"};
        doTestParseOpenIDsParameter(openIDs, exp);
    }

    @Test
    public void testOpenIDsParameterWith2UUIDs() {
        String openIDs = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6,uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6";
        String[] exp = {
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6",
                "uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6"};
        doTestParseOpenIDsParameter(openIDs, exp);
    }

    @Test
    public void testOpenIDsParameterWith2UUIDsAndWhitespaces() {
        String openIDs = " uuid:0eaa6730-9068-11dd-97de-000d606f5dc6   , uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6";
        String[] exp = {
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6",
                "uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6"};
        doTestParseOpenIDsParameter(openIDs, exp);
    }

    /*temporarily removed
    @Test
    public void testOpenIDsParameterWithCorruptedID() {
        String openIDs = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6,uuid::cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6";
        String[] exp = {"uuid:0eaa6730-9068-11dd-97de-000d606f5dc6"};
        doTestParseOpenIDsParameter(openIDs, exp);
    }

    @Test
    public void testOpenIDsParameterWithUnsupportedID() {
        String openIDs = "0eaa6730-9068-11dd-97de-000d606f5dc6";
        String[] exp = {};
        doTestParseOpenIDsParameter(openIDs, exp);

        openIDs = "demo:0eaa6730-9068-11dd-97de-000d606f5dc6";
        doTestParseOpenIDsParameter(openIDs, exp);
    }
*/
    @Test
    public void testOpenIDsParameterWithDuplicates() {
        String openIDs = "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6,uuid:0eaa6730-9068-11dd-97de-000d606f5dc6";
        String[] exp = {"uuid:0eaa6730-9068-11dd-97de-000d606f5dc6"};
        doTestParseOpenIDsParameter(openIDs, exp);
    }

    private void doTestParseOpenIDsParameter(String openIDs, String[] exp) {
        EditorConfiguration instance = EditorConfiguration.getInstance();
        Collection<String> res = instance.parseOpenIDsParameter(openIDs);
        assertArrayEquals(exp, res.toArray(new String[res.size()]));
    }

}