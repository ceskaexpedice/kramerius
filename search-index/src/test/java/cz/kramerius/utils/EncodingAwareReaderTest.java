/*
 * Copyright (C) 2025  Inovatika
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
package cz.kramerius.utils;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;


public class EncodingAwareReaderTest {

    @Test
    public void testReader_16UTF() throws IOException {
        InputStream utf16stream = this.getClass().getResourceAsStream("4380100092.txt");
        Assert.assertNotNull(utf16stream);
        String content = EncodingAwareReader.readWithDetectedEncoding(utf16stream);
        Assert.assertTrue(content.contains("Wilfcrt Karel. 694."));
    }

    @Test
    public void testReader_8UTF() throws IOException {
        InputStream utf16stream = this.getClass().getResourceAsStream("txt_cdfb4370-5962-11e3-9ea2-5ef3fc9ae867_0001.txt");
        Assert.assertNotNull(utf16stream);
        String content = EncodingAwareReader.readWithDetectedEncoding(utf16stream);
        Assert.assertTrue(content.contains("ROSTE A SÍLÍ NÄŠ ZNÁRODNĚNÝ"));
    }


}
