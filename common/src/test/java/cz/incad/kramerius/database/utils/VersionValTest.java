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
package cz.incad.kramerius.database.utils;

import org.junit.Assert;
import org.junit.Test;

public class VersionValTest {
    
    @Test
    public void testVersionVals() {
        Assert.assertTrue(VersionVal.interpretVersionValue("4.3.2") == 432);
        Assert.assertTrue(VersionVal.interpretVersionValue("4.1.3") == 413);
        Assert.assertTrue(VersionVal.interpretVersionValue("5.1.0") == 510);
        
        // not valid versions
        try {
            VersionVal.interpretVersionValue("4.1.");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Ok
        }

        try {
            VersionVal.interpretVersionValue("4.1");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Ok
        }

        try {
            VersionVal.interpretVersionValue("4.");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Ok
        }

        try {
            VersionVal.interpretVersionValue("5.3.2.3");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // Ok
        }

    }
}
