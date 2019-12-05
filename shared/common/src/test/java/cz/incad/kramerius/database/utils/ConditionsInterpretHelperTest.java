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

import junit.framework.Assert;

import org.junit.Test;

import cz.incad.kramerius.database.cond.ConditionsInterpretHelper;

/**
 * @author pavels
 *
 */
public class ConditionsInterpretHelperTest {
    
    @Test
    public void testConditions1() {
        String left = "3.4.2";
        String right ="4.2.2";
        Assert.assertTrue(ConditionsInterpretHelper.versionCondition(left, "<", right));
        Assert.assertTrue(ConditionsInterpretHelper.versionCondition(left, "<=", right));
        Assert.assertFalse(ConditionsInterpretHelper.versionCondition(left, ">", right));
        Assert.assertFalse(ConditionsInterpretHelper.versionCondition(left, ">=", right));
        Assert.assertFalse(ConditionsInterpretHelper.versionCondition(left, "=", right));
    }

    @Test
    public void testConditions2() {
        String left = "5.3.1";
        String right ="4.2.2";
        Assert.assertFalse(ConditionsInterpretHelper.versionCondition(left, "<", right));
        Assert.assertFalse(ConditionsInterpretHelper.versionCondition(left, "<=", right));
        Assert.assertTrue(ConditionsInterpretHelper.versionCondition(left, ">", right));
        Assert.assertTrue(ConditionsInterpretHelper.versionCondition(left, ">=", right));
        Assert.assertFalse(ConditionsInterpretHelper.versionCondition(left, "=", right));
    }

    @Test
    public void testConditions3() {
        String left = "5.3.0";
        String right ="5.3.0";
        Assert.assertFalse(ConditionsInterpretHelper.versionCondition(left, "<", right));
        Assert.assertTrue(ConditionsInterpretHelper.versionCondition(left, "<=", right));
        Assert.assertFalse(ConditionsInterpretHelper.versionCondition(left, ">", right));
        Assert.assertTrue(ConditionsInterpretHelper.versionCondition(left, ">=", right));
        Assert.assertTrue(ConditionsInterpretHelper.versionCondition(left, "=", right));
    }
}
