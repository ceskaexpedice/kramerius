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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cz.incad.kramerius.imaging.paths.DirPath;
import cz.incad.kramerius.imaging.paths.FilePath;
import cz.incad.kramerius.imaging.paths.Path;
import cz.incad.kramerius.imaging.paths.PathFilter;

public class DirPathImpl extends AbstractPath implements DirPath{

    
    public DirPathImpl(File file, Path parent) {
        super(file, parent);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Path[] list() {
        File[] lf = this.file.listFiles();
        return listPaths(lf);
    }

    public Path[] listPaths(File[] lf) {
        if (lf != null) {
            Path[] paths = new Path[lf.length];
            for (int i = 0; i < paths.length; i++) {
                if (lf[i].isDirectory()) {
                    paths[i] = new DirPathImpl(lf[i], this);
                } else {
                    paths[i] = new FilePathImpl(lf[i], this);
                }
            }
            return paths;
        } else {
            return new Path[0];
        }
    }

    @Override
    public Path[] list(PathFilter filter) {
        List<Path> paths = new ArrayList<Path>();
        File[] files = this.file.listFiles();
        if (files != null) {
            for (File f : files) {
                FilePathImpl fpath = new FilePathImpl(f, this);
                if (filter.accept(fpath)) {
                    paths.add(fpath);
                }
            }
        }
        return (Path[]) paths.toArray(new Path[paths.size()]);
    }

    @Override
    public Path child(String name) {
        File f = new File(this.file, name);
        if (f.exists()) {
            if (f.isDirectory()) {
                return new DirPathImpl(f, this);
            } else {
                return new FilePathImpl(f, this);
            }
        } else return null;
    }

    @Override
    public FilePath createChildFile(String name) throws IOException {
        File f = new File(this.file, name);
        f.createNewFile();
        return new FilePathImpl(f, this);
    }

    @Override
    public DirPath createChildDir(String name)  throws IOException {
        File f = new File(this.file, name);
        f.mkdirs();
        return new DirPathImpl(f, this);
    }

    @Override
    public void deleteChild(String name)  throws IOException {
        File f = new File(this.file, name);
        if (f.isDirectory()) {
            FileUtils.deleteDirectory(f);
        } else {
            f.delete();
        }
    }

    
}
