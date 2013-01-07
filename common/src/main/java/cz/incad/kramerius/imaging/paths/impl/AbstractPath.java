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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import cz.incad.kramerius.imaging.paths.DirPath;
import cz.incad.kramerius.imaging.paths.FilePath;
import cz.incad.kramerius.imaging.paths.Path;

/**
 * Abstract Path
 * @author pavels
 */
public abstract class AbstractPath implements Path{
    
    protected Path parent;
    protected File file;

    public AbstractPath(File file, Path parent) {
        super();
        this.file = file;
        this.parent = parent;
    }

    @Override
    public String getName() {
        return this.file.getName();
    }

    @Override
    public boolean exists() {
        return this.file.exists();
    }

    @Override
    public Path getParent() {
        return this.parent;
    }

    @Override
    public DirPath makeDir() throws IOException {
        if (!this.file.exists()) {
            boolean mkdirs = this.file.mkdirs();
            if (mkdirs) return new DirPathImpl(this.file, this.getParent());
            else throw new IOException("cannot create dir '"+this.file.getName()+"'");
        }
        return new DirPathImpl(this.file, this.getParent());
    }

    @Override
    public FilePath makeFile() throws IOException {
        if (!this.file.exists()) {
            boolean mkfile = this.file.createNewFile();
            if (mkfile) return new FilePathImpl(this.file, this.getParent());
            else throw new IOException("cannot create file '"+this.file.getName()+"'");
        }
        return new FilePathImpl(this.file, this.getParent());
    }

    @Override
    public URL toURL() {
        try {
            return this.file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractPath other = (AbstractPath) obj;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        return true;
    }
}
