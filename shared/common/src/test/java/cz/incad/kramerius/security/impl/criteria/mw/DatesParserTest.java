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
package cz.incad.kramerius.security.impl.criteria.mw;

import java.io.StringReader;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

import com.ibm.icu.util.Calendar;

import antlr.RecognitionException;
import antlr.TokenStreamException;

public class DatesParserTest {

    @Test
    public void shouldPass() throws RecognitionException, TokenStreamException {
        String date =" 1991 ";
        DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
        Date parsed = p.dates();
        Assert.assertTrue("expecting 1991 ",field(parsed,Calendar.YEAR) == 1991);
    }

    @Test
    public void shouldPass2() throws RecognitionException, TokenStreamException {
        String date =" 2. - 12 .1991 ";
        DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
        Date parsed = p.dates();
        Assert.assertTrue("expecting year 1991 ",field(parsed,Calendar.YEAR) == 1991);
        Assert.assertTrue("expecting month december (Calendar.DECEMBER = 11) ",field(parsed,Calendar.MONTH) == 11);
    }
    

    @Test
    public void shouldPass3() throws RecognitionException, TokenStreamException {
        String date ="2.-12. 1. 1991 ";
        DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
        Date parsed = p.dates();
        Assert.assertTrue("expecting year 1991 ",field(parsed,Calendar.YEAR) == 1991);
        Assert.assertTrue("expecting month january (Calendar.JANUARY = 0) ",field(parsed,Calendar.MONTH) == 0);
        Assert.assertTrue("expecting day 12 ",field(parsed,Calendar.DAY_OF_MONTH) == 12);
    }


    @Test
    public void shouldPass4() throws RecognitionException, TokenStreamException {
        String date ="2.-12. 1. 1991 ";
        DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
        Date parsed = p.dates();
        Assert.assertTrue("expecting year 1991 ",field(parsed,Calendar.YEAR) == 1991);
        Assert.assertTrue("expecting month january (Calendar.JANUARY = 0) ",field(parsed,Calendar.MONTH) == 0);
        Assert.assertTrue("expecting day 12 ",field(parsed,Calendar.DAY_OF_MONTH) == 12);
    }

    @Test
    public void shouldPass5() throws RecognitionException, TokenStreamException {
        String date =" " +
        		"12. " +
        		"  9. 1991 " +
        		"  ";
        DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
        Date parsed = p.dates();
        Assert.assertTrue("expecting year 1991 ",field(parsed,Calendar.YEAR) == 1991);
        Assert.assertTrue("expecting month september (Calendar.SEPTEMBER = 8) ",field(parsed,Calendar.MONTH) == 8);
        Assert.assertTrue("expecting day 12 ",field(parsed,Calendar.DAY_OF_MONTH) == 12);
    }

    @Test
    public void shouldPass6() throws RecognitionException, TokenStreamException {
        String date =" 1991 - 1992 ";
        DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
        Date parsed = p.dates();
        Assert.assertTrue("expecting year 1992 ",field(parsed,Calendar.YEAR) == 1992);
    }

    @Test
    public void shouldFail() throws TokenStreamException  {
        boolean failed = false;
        try {
            String date ="991"; //short year
            DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
            Date parsed = p.dates();
        } catch (RecognitionException e) {
            //  ok
            failed = true;
        }
        if (!failed) Assert.fail();
    }

    @Test
    public void shouldFail2() throws TokenStreamException  {
        boolean failed = false;
        try {
            String date ="13.1991"; //bad month
            DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
            Date parsed = p.dates();
        } catch (RecognitionException e) {
            //  ok
            failed = true;
        }
        if (!failed) Assert.fail();
    }

    @Test
    public void shouldFail3() throws TokenStreamException, RecognitionException  {
        boolean failed = false;
        try {
            String date ="xa13.1991"; //not in format
            DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
            Date parsed = p.dates();
        } catch (TokenStreamException e) {
            failed = true;
        }
        if (!failed) Assert.fail();
    }

    @Test
    public void shouldFail4() throws TokenStreamException, RecognitionException  {
        boolean failed = false;
        try {
            String date ="19912"; //not in format
            DatesParser p = new DatesParser(new DateLexer(new StringReader(date)));
            Date parsed = p.dates();
        } catch (RecognitionException e) {
            failed = true;
        }
        if (!failed) Assert.fail();
    }

    
    private int field(Date d, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return cal.get(field);
    }
}
