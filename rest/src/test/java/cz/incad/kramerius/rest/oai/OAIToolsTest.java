/*
 * Copyright (C) Aug 23, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.oai;

import cz.incad.kramerius.rest.oai.utils.OAITools;
import org.junit.Assert;
import org.junit.Test;



public class OAIToolsTest {

    @Test
    public void testResumptionToken() {
        String resToken = OAITools.generateResumptionToken("marc21", 600, 600, "solrCursor", "monographs", null, null);
        Assert.assertTrue("solrCursor:monographs:marc21".equals(resToken));
        Assert.assertTrue(OAITools.metadataFromResumptionToken(resToken).equals("marc21"));
        Assert.assertTrue(OAITools.specFromResumptionToken(resToken).equals("monographs"));
        Assert.assertTrue(OAITools.solrCursorMarkFromResumptionToken(resToken).equals("solrCursor"));

        String resToken2 = OAITools.generateResumptionToken("marc21", 600, 600, "solrCursor", "monographs", "fstamp", "ustamp");
        Assert.assertTrue(OAITools.metadataFromResumptionToken(resToken2).equals("marc21"));
        Assert.assertTrue(OAITools.specFromResumptionToken(resToken2).equals("monographs"));
        Assert.assertTrue(OAITools.solrCursorMarkFromResumptionToken(resToken2).equals("solrCursor"));
        Assert.assertTrue(OAITools.fromFromResumptionToken(resToken2).equals("fstamp"));
        Assert.assertTrue(OAITools.untilFromResumptionToken(resToken2).equals("ustamp"));

        String resToken3 = OAITools.generateResumptionToken("marc21", 600, 600, "solrCursor", "monographs", null, "ustamp");
        Assert.assertNull(OAITools.fromFromResumptionToken(resToken3));
        Assert.assertNotNull(OAITools.untilFromResumptionToken(resToken3));
        Assert.assertEquals(OAITools.untilFromResumptionToken(resToken3),"ustamp");

        String resToken4 = OAITools.generateResumptionToken("marc21", 600, 600, "solrCursor", "monographs", "fstamp", null);
        Assert.assertNotNull(OAITools.fromFromResumptionToken(resToken4));
        Assert.assertNull(OAITools.untilFromResumptionToken(resToken4));
        Assert.assertEquals(OAITools.fromFromResumptionToken(resToken4),"fstamp");
    }

}
