/*
 * Copyright (C) 2012 Pavel Stastny
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
/**
 * 
 */
package cz.incad.kramerius.statistics.impl;

import junit.framework.Assert;

import org.antlr.stringtemplate.StringTemplate;
import org.junit.Test;

/**
 * @author pavels
 *
 */
public class AuthorReportTest {

    @Test
    public void testTemplate() {
        StringTemplate statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectAuthorReport");
        statRecord.setAttribute("action", "PDF");
        statRecord.setAttribute("paging", true);
        String str = statRecord.toString();
        Assert.assertTrue(str.contains(" offset "));
        Assert.assertTrue(str.contains(" limit "));
        Assert.assertNotNull(statRecord.toString());
        Assert.assertFalse(str.contains("SIMILAR TO"));


        statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectAuthorReport");
        statRecord.setAttribute("action", "PDF");
        statRecord.setAttribute("paging", false);
        str = statRecord.toString();
        Assert.assertFalse(str.contains(" offset "));
        Assert.assertFalse(str.contains(" limit "));
        Assert.assertNotNull(statRecord.toString());
        Assert.assertFalse(str.contains("SIMILAR TO"));


        statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectAuthorReport");
        statRecord.setAttribute("action", null);
        statRecord.setAttribute("paging", true);
        str = statRecord.toString();
        Assert.assertTrue(str.contains(" offset "));
        Assert.assertTrue(str.contains(" limit "));
        Assert.assertNotNull(str);
        Assert.assertFalse(str.contains("SIMILAR TO"));


        statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectAuthorReport");
        statRecord.setAttribute("action", null);
        statRecord.setAttribute("paging", false);
        str = statRecord.toString();
        Assert.assertFalse(str.contains(" offset "));
        Assert.assertFalse(str.contains(" limit "));
        Assert.assertNotNull(str);
        Assert.assertFalse(str.contains("SIMILAR TO"));

        
        statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectAuthorReport");
        statRecord.setAttribute("action", "PDF");
        statRecord.setAttribute("paging", true);
        statRecord.setAttribute("fromDefined", true);
        statRecord.setAttribute("toDefined", false);
        str = statRecord.toString();
        Assert.assertFalse(str.contains("SIMILAR TO"));

        //System.out.println(str);
        Assert.assertTrue(str.contains(" offset "));
        Assert.assertTrue(str.contains(" limit "));
        Assert.assertNotNull(str);

        statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectAuthorReport");
        statRecord.setAttribute("action", "PDF");
        statRecord.setAttribute("paging", true);
        statRecord.setAttribute("fromDefined", true);
        statRecord.setAttribute("toDefined", true);

        str = statRecord.toString();
        Assert.assertFalse(str.contains("SIMILAR TO"));
        
        statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectAuthorReport");
        statRecord.setAttribute("action", "PDF");
        statRecord.setAttribute("paging", false);
        statRecord.setAttribute("fromDefined", true);
        statRecord.setAttribute("toDefined", true);

        str = statRecord.toString();
        Assert.assertFalse(str.contains("SIMILAR TO"));
        
        statRecord = DatabaseStatisticsAccessLogImpl.stGroup.getInstanceOf("selectAuthorReport");
        statRecord.setAttribute("action", "PDF");
        statRecord.setAttribute("paging", false);
        statRecord.setAttribute("fromDefined", true);
        statRecord.setAttribute("toDefined", true);

        statRecord.setAttribute("ipaddr", "192.*");

        str = statRecord.toString();
        Assert.assertTrue(str.contains("SIMILAR TO"));

    }
}
