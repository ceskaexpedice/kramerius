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
package cz.incad.kramerius.imaging.impl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.impl.FileSystemCacheServiceImplTest._Module;
import cz.incad.kramerius.imaging.paths.DirPath;

/**
 * @author pavels
 *
 */
public class TileSupportImplTest extends AbstractGuiceTestCase {

    @Override
    protected Injector injector() {
        Injector injector = Guice.createInjector(new _Module());
        return injector;
    }

    
    @Test
    public void testTileSupport() {
        Injector inj = injector();
        DeepZoomTileSupport tileSupport = inj.getInstance(DeepZoomTileSupport.class);
        Dimension dim = new Dimension(1345, 2005);
        
        int tileLevels1 = tileSupport.getLevels(dim, 512);
        int tileLevels2 = tileSupport.getLevels(dim, 1);
        Assert.assertTrue(tileLevels1 == 3);
        Assert.assertTrue(tileLevels2 == 12);

        int offset = tileLevels2 - tileLevels1;
        Assert.assertTrue(offset == 9);

//        double scale1 = tileSupport.getScale(0, tileLevels1);
//        Dimension scaledDimension = tileSupport.getScaledDimension(dim, scale1);
//        System.out.println(scaledDimension);
        
        for (int i = 8; i < 12; i++) {
            double scale2 = tileSupport.getScale(i, tileLevels2);
            Dimension scaledDimension = tileSupport.getScaledDimension(dim, scale2);
            System.out.println(scaledDimension);
            
        }
//        double scale2 = tileSupport.getScale(9, tileLevels2);
//        Assert.assertTrue(scale1 == scale2);
        
    }
   
    
    
    class _Module extends AbstractModule {
        
        @Override
        protected void configure() {
            TileSupportImpl tis = EasyMock.createMockBuilder(TileSupportImpl.class).withConstructor()
                .addMockedMethod("getTileSize").createMock();
            
            FedoraAccess fa = EasyMock.createMock(FedoraAccess.class);

            EasyMock.replay(tis,fa);

            bind(DeepZoomTileSupport.class).toInstance(tis);
            bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(fa);
        }
    }

}
