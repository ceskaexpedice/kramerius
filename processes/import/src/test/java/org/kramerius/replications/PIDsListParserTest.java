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
package org.kramerius.replications;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.kramerius.replications.pidlist.PIDsListLexer;
import org.kramerius.replications.pidlist.PIDsListParser;
import org.kramerius.replications.pidlist.PidsListCollect;

import antlr.RecognitionException;
import antlr.TokenStreamException;


/**
 * @author pavels
 *
 */
public class PIDsListParserTest {

    @Test
    public void testPIDParser() throws RecognitionException, TokenStreamException {
        final List<String> exps = new ArrayList<String>();
        {
            exps.add("'uuid:0eaa6730-9068-11dd-97de-000d606f5dc6'");
            exps.add("'uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6'");
        }
        String  input = "{'pids':['uuid:0eaa6730-9068-11dd-97de-000d606f5dc6','uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6']}";
        PIDsListParser parser = new PIDsListParser(new PIDsListLexer(new StringReader(input)));
        PidsListCollect collect = new PidsListCollect() {
            
            @Override
            public void pidEmitted(String pid) {
                Assert.assertTrue(exps.contains(pid));
                exps.remove(pid);
            }
            
            @Override
            public void pathEmitted(String path) {
                // TODO Auto-generated method stub
            }
        };
        parser.setPidsListCollect(collect);
        parser.pids();
        
        Assert.assertTrue(exps.isEmpty());
    }

    @Test
    public void testPIDParser2() throws RecognitionException, TokenStreamException {
        final List<String> exps = new ArrayList<String>();
        {
            exps.add("\"uuid:0eaa6730-9068-11dd-97de-000d606f5dc6\"");
            exps.add("\"uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6\"");
        }
        String  input = "{\"pids\":[\"uuid:0eaa6730-9068-11dd-97de-000d606f5dc6\",\"uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6\"]}";
        PIDsListParser parser = new PIDsListParser(new PIDsListLexer(new StringReader(input)));
        PidsListCollect collect = new PidsListCollect() {
            
            @Override
            public void pidEmitted(String pid) {
                Assert.assertTrue(exps.contains(pid));
                exps.remove(pid);
            }
            
            @Override
            public void pathEmitted(String path) {
                // TODO Auto-generated method stub
            }
        };
        parser.setPidsListCollect(collect);
        parser.pids();
        
        Assert.assertTrue(exps.isEmpty());
    }

    @Test
    public void testPIDParser3() throws RecognitionException, TokenStreamException {
        final List<String> expPids = new ArrayList<String>();
        {
            expPids.add("'uuid:0eaa6730-9068-11dd-97de-000d606f5dc6'");
            expPids.add("'uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6'");
        }
        final List<String> expPath = new ArrayList<String>();
        {
            expPath.add("'uuid:0eaa6730-9068-11dd-97de-000d606f5dc6/uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6'");
        }
        String  input = "{'pids':['uuid:0eaa6730-9068-11dd-97de-000d606f5dc6','uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6'],'paths':['uuid:0eaa6730-9068-11dd-97de-000d606f5dc6/uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6']}";
        PIDsListParser parser = new PIDsListParser(new PIDsListLexer(new StringReader(input)));
        PidsListCollect collect = new PidsListCollect() {
            
            @Override
            public void pidEmitted(String pid) {
                Assert.assertTrue(expPids.contains(pid));
                expPids.remove(pid);
            }
            
            @Override
            public void pathEmitted(String path) {
                Assert.assertTrue(expPath.contains(path));
                expPath.remove(path);
            }
        };
        parser.setPidsListCollect(collect);
        parser.pids();
        Assert.assertTrue(expPids.isEmpty());
        Assert.assertTrue(expPath.isEmpty());
    }
    
    @Test
    public void testPIDParser4() throws RecognitionException, TokenStreamException {
        String  input = "{'paths':['uuid:0eaa6730-9068-11dd-97de-000d606f5dc6/uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6'],'pids':['uuid:0eaa6730-9068-11dd-97de-000d606f5dc6','uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6']}";
        final List<String> expPids = new ArrayList<String>();
        {
            expPids.add("'uuid:0eaa6730-9068-11dd-97de-000d606f5dc6'");
            expPids.add("'uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6'");
        }
        final List<String> expPath = new ArrayList<String>();
        {
            expPath.add("'uuid:0eaa6730-9068-11dd-97de-000d606f5dc6/uuid:4a79bd50-af36-11dd-a60c-000d606f5dc6'");
        }
        PIDsListParser parser = new PIDsListParser(new PIDsListLexer(new StringReader(input)));
        PidsListCollect collect = new PidsListCollect() {
            
            @Override
            public void pidEmitted(String pid) {
                Assert.assertTrue(expPids.contains(pid));
                expPids.remove(pid);
            }
            
            @Override
            public void pathEmitted(String path) {
                Assert.assertTrue(expPath.contains(path));
                expPath.remove(path);
            }
        };
        parser.setPidsListCollect(collect);
        parser.pids();
        Assert.assertTrue(expPids.isEmpty());
        Assert.assertTrue(expPath.isEmpty());
        
    }

}
