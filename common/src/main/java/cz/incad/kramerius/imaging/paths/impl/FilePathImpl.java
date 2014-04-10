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
package cz.incad.kramerius.imaging.paths.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.imageio.stream.ImageOutputStreamImpl;


import cz.incad.kramerius.imaging.paths.FilePath;
import cz.incad.kramerius.imaging.paths.Path;

/**
 * @author pavels
 *
 */
public class FilePathImpl extends AbstractPath implements FilePath{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FilePathImpl.class.getName());
    
    public FilePathImpl(File file, Path parent) {
        super(file, parent);
    }

    @Override
    public Writer openWriter() throws IOException {
        FileWriter fw = new FileWriter(this.file);
        return fw;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        FileOutputStream fos= new FileOutputStream(this.file);
        LOGGER.info("open stream from '"+this.file.getAbsolutePath()+"'");
        return fos;
    }

    @Override
    public Reader openReader() throws IOException {
        FileReader freader = new FileReader(this.file);
        LOGGER.info("open reader from '"+this.file.getAbsolutePath()+"'");
        return freader;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        FileInputStream fis = new FileInputStream(this.file);
        LOGGER.info("open stream from '"+this.file.getAbsolutePath()+"'");
        return fis;
    }
    
    @Override
    public ImageOutputStreamImpl openImageOutputStream() throws IOException {
        FileImageOutputStream fos = new FileImageOutputStream(this.file);
        return fos;
    }

    @Override
    public ImageInputStreamImpl openImageInputStream() throws IOException {
        FileImageInputStream fis = new FileImageInputStream(this.file);
        return fis;
    }

    @Override
    public boolean canRead() {
        return this.file.canRead();
    }

    @Override
    public boolean canWrite() {
        return this.file.canWrite();
    }
}
