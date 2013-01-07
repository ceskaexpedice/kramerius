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
package cz.incad.kramerius.imaging.paths;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.imageio.stream.ImageInputStreamImpl;
import javax.imageio.stream.ImageOutputStreamImpl;

/**
 * Represents file structure
 * @author pavels
 */
public interface FilePath extends Path{

    /**
     * Creates new writer
     * @return Created writer
     * @throws IOException IO error has been occurred 
     */
    public Writer openWriter() throws IOException;

    /**
     * Creates new outputstream
     * @return Created outputstream 
     * @throws IOException IO error has been occurred
     */
    public OutputStream openOutputStream() throws IOException;
    
    /**
     * Creates new reader
     * @return Created reader
     * @throws IOException IO error has been occurred
     */
    public Reader openReader() throws IOException;
    
    /**
     * Creates new input stream 
     * @return Created inputstream 
     * @throws IOException IO error has been occurred
     */
    public InputStream openInputStream() throws IOException;

    /**
     * Creates new ImageOutputStream
     * @return Created imageinputstream
     * @throws IOException IO error has been occurred
     */
    public ImageOutputStreamImpl openImageOutputStream() throws IOException;
    
    /**
     * Creates new ImageInputStream
     * @return Crated imageotputstream
     * @throws IOException IO error has been occurred
     */
    public ImageInputStreamImpl openImageInputStream() throws IOException;
    
    /**
     * Returns true if this file is readable
     * @return True if this file is readable
     */
    public boolean canRead();
    
    /**
     * Returns true if this file is writable
     * @return True if this file is writeable
     */
    public boolean canWrite();
}
