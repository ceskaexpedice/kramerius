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
package cz.incad.kramerius.imaging.paths;

import java.io.IOException;
import java.net.URL;


/**
 * Represents file or folder
 * @author pavels
 */
public interface Path {

    /**
     * Returns name of file or folder
     * @return
     */
    public String getName();

    /**
     * Returns true if underlaying file exists
     * @return true if underlaying file exists
     */
    public boolean exists();

    /**
     * Returns parent
     * @return parent path
     */
    public Path getParent();

    /**
     * Creates new folder 
     * @return Created folder
     * @throws IOException Cannot create folder
     */
    public DirPath makeDir() throws IOException;
    
    /**
     * Creates new file
     * @return Created file 
     * @throws IOException Cannot create file
     */
    public FilePath makeFile() throws IOException;

    /**
     * Constructs URL 
     * @return Constructed URL
     */
    public URL toURL();

}
