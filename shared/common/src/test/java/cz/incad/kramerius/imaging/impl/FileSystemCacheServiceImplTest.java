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
package cz.incad.kramerius.imaging.impl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.imageio.stream.ImageOutputStreamImpl;
import javax.xml.crypto.Data;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.paths.DirPath;
import cz.incad.kramerius.imaging.paths.FilePath;
import cz.incad.kramerius.imaging.paths.Path;
import cz.incad.kramerius.imaging.paths.PathFilter;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * @author pavels
 *
 */
public class FileSystemCacheServiceImplTest extends AbstractGuiceTestCase {
    
    public static Map<List<String>, Path> PATHS = new HashMap<List<String>, Path>(); 
    
    public static BufferedImage bufImage() throws IOException {
        InputStream is = FileSystemCacheServiceImplTest.class.getResourceAsStream(("res/img.jpg"));
        return ImageIO.read(is);
    }

    public void cacheImage(Injector inj) throws IOException, ProcessSubtreeException {
        DeepZoomCacheService dcache = inj.getInstance(DeepZoomCacheService.class);
        dcache.prepareCacheForPID(DataPrepare.DROBNUSTKY_PIDS[0]);
        
    }
    
    @Before
    public void before() {
        PATHS.clear();
        _DirPath dirPath = new _DirPath(DataPrepare.DROBNUSTKY_PIDS[0], null);
        PATHS.put(dirPath.toUp(), dirPath);
    }

    @Test
    public void testPrepageAndGet() throws IOException, ProcessSubtreeException {
        Injector inj = injector();
        cacheImage(inj);
        DeepZoomCacheService dz = inj.getInstance(DeepZoomCacheService.class);
        InputStream is = dz.getDeepZoomDescriptorStream(DataPrepare.DROBNUSTKY_PIDS[0]);
        Assert.assertNotNull(is);
        

        Dimension originalResolution = dz.getResolutionFromFile(DataPrepare.DROBNUSTKY_PIDS[0]);
        
        
        DeepZoomTileSupport tileSupport = inj.getInstance(DeepZoomTileSupport.class);
        int closestLevel = tileSupport.getClosestLevel(originalResolution, 512, 1);
        Assert.assertTrue(closestLevel == 9);
    
        int levels = tileSupport.getLevels(originalResolution, 512);
        Assert.assertTrue(levels == 3);

        levels = tileSupport.getLevels(originalResolution, 1);
        Assert.assertTrue(levels == 12);
    
        
    }
    
    
    @Test
    public void testPrepraCacheImage() throws IOException, ProcessSubtreeException, LexerException {
        Injector inj = injector();
        cacheImage(inj);
        
        DirPath dpath = (DirPath) PATHS.get(Arrays.asList(DataPrepare.DROBNUSTKY_PIDS[0]));
        Path[] files = dpath.list(new PathFilter() {
            
            @Override
            public boolean accept(Path path) {
                return (path instanceof FilePath);
            }
        });
        
        Assert.assertTrue(files.length == 2);
        Assert.assertTrue(files[0].getName().equals("deep_zoom") ||  files[1].getName().equals("deep_zoom"));
        Assert.assertTrue(files[0].getName().equals("1386_x_1981.resolution") ||  files[1].getName().equals("1386_x_1981.resolution"));

        Path[] dirs = dpath.list(new PathFilter() {
            
            @Override
            public boolean accept(Path path) {
                return (path instanceof DirPath);
            }
        });
        Assert.assertTrue(dirs.length == 3);
        
        Assert.assertTrue(dirs[0].getName().equals("9") || dirs[1].getName().equals("9") || dirs[2].getName().equals("9"));
        Assert.assertTrue(dirs[0].getName().equals("10") || dirs[1].getName().equals("10") || dirs[2].getName().equals("10"));
        Assert.assertTrue(dirs[0].getName().equals("11") || dirs[1].getName().equals("11") || dirs[2].getName().equals("11"));
    }

    
    @Override
    protected Injector injector() {
        Injector injector = Guice.createInjector(new _Module());
        return injector;
    }


    class _Module extends AbstractModule {

        
        @Override
        protected void configure() {
            try {
                FedoraAccess fa = EasyMock.createMock(FedoraAccess.class);
                TileSupportImpl tis = EasyMock.createMockBuilder(TileSupportImpl.class).withConstructor()
                .addMockedMethod("getTileSize").createMock();
                
                FileSystemCacheServiceImpl fcache = EasyMock.createMockBuilder(FileSystemCacheServiceImpl.class).withConstructor()
                    .addMockedMethod("createDeepZoomOriginalImageFromFedoraRAW")
                    .addMockedMethod("isResolutionFilePresent")
                    .addMockedMethod("uuidFolder")
                    .createMock();
                
                EasyMock.expect(fa.isImageFULLAvailable(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn(true).anyTimes();
                EasyMock.expect(fcache.createDeepZoomOriginalImageFromFedoraRAW(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn(bufImage());
                EasyMock.expect(tis.getTileSize()).andReturn(512).anyTimes();
                EasyMock.expect(fcache.isResolutionFilePresent(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn(false).anyTimes();
                
                
                EasyMock.expect(fcache.uuidFolder(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn((DirPath) PATHS.get(Arrays.asList(DataPrepare.DROBNUSTKY_PIDS[0]))).anyTimes();

                
                DiscStrucutreForStore discStruct = EasyMock.createMock(DiscStrucutreForStore.class);
                
                EasyMock.replay(fa,tis,fcache, discStruct);
                
                bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(fa);
                bind(DeepZoomTileSupport.class).toInstance(tis);
                bind(DeepZoomCacheService.class).toInstance(fcache);
                bind(DiscStrucutreForStore.class).toInstance(discStruct);
                
                
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    
    
    public static class _AbstractPath implements Path {

        
        private String name;
        private Path parent;
        
        
        
        public _AbstractPath(String name, Path parent) {
            super();
            this.name = name;
            this.parent = parent;
        }

        @Override
        public String getName() {
            return name;
        }

        List<String> toUp() {
            List<String> names = new ArrayList<String>();
            Stack<Path> stack = new Stack<Path>();
            stack.push(this);
            while(!stack.isEmpty()) {
                Path poped = stack.pop();
                names.add(poped.getName());
                Path parent2 = poped.getParent();
                if (parent2 != null) stack.push(parent2);
            }
            return names;
        }
        
        
        
        @Override
        public boolean exists() {
            return PATHS.containsKey(toUp());
        }

        @Override
        public Path getParent() {
            return this.parent;
        }

        @Override
        public DirPath makeDir() {
            _DirPath dp = new _DirPath(this.name, this.getParent());
            PATHS.put(dp.toUp(), dp);
            return dp;
        }

        @Override
        public FilePath makeFile() {
            _FilePath fp = new _FilePath(this.name, this.getParent());
            PATHS.put(fp.toUp(), fp);
            return fp;
        }

        @Override
        public URL toURL() {
            throw new UnsupportedOperationException("still unsupported");
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
            _AbstractPath other = (_AbstractPath) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (parent == null) {
                if (other.parent != null)
                    return false;
            } else if (!parent.equals(other.parent))
                return false;
            return true;
        }
        
        
        
    }
    
    public static class _DirPath extends _AbstractPath implements DirPath {
        private List<Path> list = new ArrayList<Path>();


        public _DirPath(String name, Path parent) {
            super(name, parent);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Path[] list() {
            return (Path[]) this.list.toArray(new Path[this.list.size()]);
        }

        @Override
        public Path[] list(PathFilter filter) {
            List<Path> l = new ArrayList<Path>();
            for (Path path : this.list) {
                if (filter.accept(path)) {
                    l.add(path);
                }
            }
            return (Path[]) l.toArray(new Path[l.size()]);
        }

        @Override
        public Path child(String name) {
            for (Path path : this.list) {
                if (path.getName().equals(name)) return path;
            }
            return null;
        }

        @Override
        public FilePath createChildFile(String name) throws IOException {
            _FilePath fp = new _FilePath(name, this);
            PATHS.put(fp.toUp(),fp);
            if (!this.list.contains(fp)) {
                this.list.add(fp);
            }
            return fp;
        }

        @Override
        public DirPath createChildDir(String name) throws IOException {
            _DirPath dp = new _DirPath(name, this);
            PATHS.put(dp.toUp(), dp);
            if (!this.list.contains(dp)) {
                this.list.add(dp);
            }
            return dp;
        }

        @Override
        public void deleteChild(String name) throws IOException {
            List<String> up = this.toUp();
            List<String> cloned = new ArrayList<String>(up);
            cloned.add(name);
            
            Path removed = PATHS.remove(cloned);
            this.list.remove(removed);
        }
    }

    public static class _FilePath extends _AbstractPath implements FilePath {
        
        private ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        
        public _FilePath(String name, Path parent) {
            super(name, parent);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Writer openWriter() throws IOException {
            OutputStreamWriter owriter = new OutputStreamWriter(this.bos);
            return owriter;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return this.bos;
        }

        @Override
        public Reader openReader() throws IOException {
            byte[] bytes = bos.toByteArray();
            return new InputStreamReader(new ByteArrayInputStream(bytes));
        }

        @Override
        public InputStream openInputStream() throws IOException {
            byte[] bytes = bos.toByteArray();
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public ImageOutputStreamImpl openImageOutputStream() throws IOException {
            return new ImageOutputStreamImpl() {
                
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    throw new UnsupportedOperationException("unsup");
                }
                
                @Override
                public int read() throws IOException {
                    throw new UnsupportedOperationException("unsup");
                }
                
                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    bos.write(b,off,len);
                }
                
                @Override
                public void write(int b) throws IOException {
                    bos.write(b);
                }
            };
        }

        @Override
        public ImageInputStreamImpl openImageInputStream() throws IOException {
            return null;
        }

        @Override
        public boolean canRead() {
            return true;
        }

        @Override
        public boolean canWrite() {
            return true;
        }
        
        
    }    
}


