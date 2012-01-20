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

package cz.incad.kramerius.editor.share;

import cz.incad.kramerius.editor.share.InputValidator.Validator;
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
public class InputValidatorTest {

    public InputValidatorTest() {
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

    /* temporarily removed
    @Test
    public void testValidatePID() {
        doTestValidPID(
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6",
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6");
        doTestValidPID(
                "   uuid:0eaa6730-9068-11dd-97de-000d606f5dc6   ",
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6");
        doTestValidPID(
                "\t   uuid:0eaa6730-9068-11dd-97de-000d606f5dc6\n   ",
                "uuid:0eaa6730-9068-11dd-97de-000d606f5dc6");
    }

    private void doTestValidPID(String input, String expNormalized) {
        Validator<String> result = InputValidator.validatePID(input);
        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(expNormalized, result.getNormalized());
    }

    @Test
    public void testValidatePIDOnInvalidInput() {
        doTestInvalidPID("invalid");
        doTestInvalidPID("");
        doTestInvalidPID(null);
        doTestInvalidPID("demo:0eaa6730-9068-11dd-97de-000d606f5dc6");
        doTestInvalidPID("uuid::0eaa6730-9068-11dd-97de-000d606f5dc6");
        doTestInvalidPID("uuid0eaa6730-9068-11dd-97de-000d606f5dc6");
        doTestInvalidPID("0eaa6730-9068-11dd-97de-000d606f5dc6");
        doTestInvalidPID("uuid:1");
    }

    private void doTestInvalidPID(String input) {
        String expNormalized = null;
        Validator<String> result = InputValidator.validatePID(input);
        assertNotNull(input, result);
        assertFalse(input, result.isValid());
        assertEquals(input, expNormalized, result.getNormalized());
    }
*/
}