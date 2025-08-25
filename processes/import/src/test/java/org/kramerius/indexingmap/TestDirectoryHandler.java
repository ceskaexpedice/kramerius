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
package org.kramerius.indexingmap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TestDirectoryHandler implements AutoCloseable {

    private final Path tempDirectory;

    public TestDirectoryHandler(String zipResourcePath, Class<?> clazz) throws IOException, URISyntaxException {
        URL resourceUrl = clazz.getResource(zipResourcePath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + zipResourcePath);
        }
        Path zipFile = Paths.get(resourceUrl.toURI());
        this.tempDirectory = Files.createTempDirectory("test_data_");
        unzip(zipFile);
    }

    public TestDirectoryHandler(Path zipFile) throws IOException {
        this.tempDirectory = Files.createTempDirectory("test_data_");
        unzip(zipFile);
    }

    private void unzip(Path zipFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFilePath = this.tempDirectory.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(newFilePath);
                } else {
                    Files.createDirectories(newFilePath.getParent());
                    Files.copy(zis, newFilePath);
                }
            }
        }
    }

    public Path getTempDirectory() {
        return tempDirectory;
    }

    @Override
    public void close() throws IOException {
        System.out.println("Cleaning up temporary directory: " + tempDirectory);
        Files.walk(tempDirectory)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete path: " + path);
                        e.printStackTrace();
                    }
                });
    }
}