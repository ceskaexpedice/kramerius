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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import cz.incad.kramerius.utils.IOUtils;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * @author pavels
 */
public class ThirdPhaseTest {

    @Test
    public void testPath() throws RecognitionException, TokenStreamException, PhaseException, IOException {
        InputStream is = this.getClass().getResourceAsStream("iterate.txt");
        String string = IOUtils.readAsString(is, Charset.forName("UTF-8"),true);
        Assert.assertNotNull(is);
        List<String> paths = ThirdPhase.processIterateToFindRoot(new StringReader(string));
        Assert.assertTrue(paths.size() == 1);
    }
}
