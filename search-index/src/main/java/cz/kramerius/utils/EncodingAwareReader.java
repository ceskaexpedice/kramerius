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

import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.EncodingDetector;
import org.apache.tika.parser.txt.UniversalEncodingDetector;
import org.apache.tika.metadata.Metadata;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for reading text from an InputStream with automatic character encoding detection.
 * <p>
 * This class uses Apache Tika's {@link UniversalEncodingDetector} to detect the character encoding
 * of the provided input stream. If the encoding cannot be determined, it defaults to UTF-8.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * try (InputStream input = new FileInputStream("file.txt")) {
 *     String content = EncodingAwareReader.readWithDetectedEncoding(input);
 *     // use content
 * }
 * </pre>
 * </p>
 *
 * <p>
 * This class is not instantiable.
 * </p>
 */
public class EncodingAwareReader {

    private EncodingAwareReader() {}

    /**
     * Reads the content of the given {@link InputStream} using automatically detected character encoding.
     * <p>
     * The method marks the input stream, detects the encoding using {@link UniversalEncodingDetector},
     * and then resets the stream to read its contents using the detected (or default) charset.
     * </p>
     *
     * @param input the input stream to read from
     * @return the content of the stream as a {@link String}
     * @throws IOException if an I/O error occurs
     */
    public static String readWithDetectedEncoding(InputStream input) throws IOException {
        BufferedInputStream bufferedStream = new BufferedInputStream(input);
        bufferedStream.mark(16384);
        EncodingDetector detector = new UniversalEncodingDetector();
        Charset detectedCharset = detector.detect(bufferedStream, new Metadata());
        if (detectedCharset == null) {
            detectedCharset = StandardCharsets.UTF_8;
        }
        bufferedStream.reset();
        return IOUtils.toString(bufferedStream, detectedCharset);
    }
}
