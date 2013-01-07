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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;


/**
 * Represents direcotory structure
 * @author pavels
 */
public interface DirPath extends Path {

    /**
     * Returns all paths from the directory
     * @return Paths
     */
    public Path[] list();

    /**
     * Returuns all paths from the directory accepted by given filter
     * @param filter Path filter
     * @return  Paths
     */
    public Path[] list(PathFilter filter);

    /**
     * Finds file by given name
     * @param name Name of file
     * @return Found path
     */
    public Path child(String name);
    
    /**
     * Creates new file 
     * @param name Name of creating file
     * @return Created file
     * @throws IOException IO error has been occured
     */
    public FilePath createChildFile(String name) throws IOException;
    
    /**
     * Creates new folder
     * @param name Creating folder's name
     * @return Crated folder
     * @throws IOException IO error has been occured
     */
    public DirPath createChildDir(String name)  throws IOException;
    
    /**
     * Delete file
     * @param name Name of the file
     * @throws IOException IO error has been occuredd
     */
    public void deleteChild(String name)  throws IOException;
}
