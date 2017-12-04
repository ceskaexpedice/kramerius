/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.utils.params;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import junit.framework.Assert;

import org.junit.Test;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import cz.incad.kramerius.utils.params.ParamsLexer;
import cz.incad.kramerius.utils.params.ParamsParser;

public class ParamsParserTest {
    
    @Test
    public void testParser() throws RecognitionException, TokenStreamException {
        ParamsParser paramsParser = new ParamsParser(new ParamsLexer(new StringReader("{1;2;3}")));
        List params = paramsParser.params();
        Assert.assertTrue(params.size() == 3);
        Assert.assertTrue(params.get(0).equals("1"));
        Assert.assertTrue(params.get(1).equals("2"));
        Assert.assertTrue(params.get(2).equals("3"));
    }
    
    @Test
    public void testParser2() throws RecognitionException, TokenStreamException {
        ParamsParser paramsParser = new ParamsParser(new ParamsLexer(new StringReader("{{a1;a2;a3};{b1;b2;b3};{c1;c2;c3}}")));
        List params = paramsParser.params();
        Assert.assertTrue(params.size() == 3);
        Assert.assertTrue(params.get(0) instanceof List);
        assertCollection((List)params.get(0), "a1","a2","a3");
        
        Assert.assertTrue(params.get(1) instanceof List);
        assertCollection((List)params.get(1), "b1","b2","b3");

        Assert.assertTrue(params.get(2) instanceof List);
        assertCollection((List)params.get(2), "c1","c2","c3");
    }

    @Test
    public void testParser3() throws RecognitionException, TokenStreamException {
        String string = "{importDirectory=/home/pavels/.kramerius4/import;ingestUrl=http\\://localhost\\:8080/fedora}";
        ParamsParser paramsParser = new ParamsParser(new ParamsLexer(new StringReader(string)));
        List params = paramsParser.params();
        Assert.assertTrue(params.size() == 2);
        
        Assert.assertTrue(params.get(0).equals("importDirectory=/home/pavels/.kramerius4/import"));
        Assert.assertTrue(params.get(1).equals("ingestUrl=http://localhost:8080/fedora"));
        
    }
 
    @Test
    public void testParser4() throws RecognitionException, TokenStreamException {
        String string = "{}";
        ParamsParser paramsParser = new ParamsParser(new ParamsLexer(new StringReader(string)));
        List params = paramsParser.params();
        Assert.assertTrue(params.size() == 0);
        
    }
    
    @Test
    public void testParser5() throws RecognitionException, TokenStreamException {
        String string ="{replicatetype=monograph;idlist=;migrationDirectory=C\\:\\\\Users\\\\ps/.kramerius4/import;targetDirectory=C\\:\\\\Users\\\\ps/.kramerius4/import;defaultRights=true;startIndexer=false;ingestSkip=false}";
        ParamsParser paramsParser = new ParamsParser(new ParamsLexer(new StringReader(string)));
        List params = paramsParser.params();
        Assert.assertTrue(params.size() == 7);
        List<String> expectingPrefixes = new ArrayList<String>(); {
            expectingPrefixes.add("replicatetype");
            expectingPrefixes.add("idlist");
            expectingPrefixes.add("migrationDirectory");
            expectingPrefixes.add("targetDirectory");
            expectingPrefixes.add("defaultRights");
            expectingPrefixes.add("startIndexer");
            expectingPrefixes.add("ingestSkip");
        }
        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i).toString();
            param = param.substring(0, param.indexOf('='));
            boolean removed = expectingPrefixes.remove(param);
            Assert.assertTrue(removed);
        }
        Assert.assertTrue(expectingPrefixes.isEmpty());
    }
    
    private static void assertCollection(Collection<?> testedCol, Object ...objects ) {
        Assert.assertTrue(testedCol.size() == objects.length);
        List<Object> processList = new ArrayList<Object>(Arrays.asList(objects));
        for (Object testedObj : testedCol) {
            Assert.assertEquals(processList.remove(0), testedObj);
        }
    }
    
    
    
}
