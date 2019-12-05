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
package cz.incad.kramerius.imaging.impl;

import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author pavels
 *
 */
public class Fedora3StreamsDiscStructureTest {

    @Test
    public void testParseDate() throws DatatypeConfigurationException {
        Date date = Fedora3StreamsDiscStructure.disectCreateDate("2012-11-29T13:06:53.331Z");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Assert.assertTrue(cal.get(Calendar.YEAR) == 2012);
        Assert.assertTrue(cal.get(Calendar.MONTH) == 10);
        Assert.assertTrue(cal.get(Calendar.DAY_OF_MONTH) == 29);
        Assert.assertTrue(cal.get(Calendar.HOUR_OF_DAY) == 13);
        Assert.assertTrue(cal.get(Calendar.MINUTE) == 6);
    }
}
