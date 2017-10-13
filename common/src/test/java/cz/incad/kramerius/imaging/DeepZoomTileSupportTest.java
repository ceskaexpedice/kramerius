package cz.incad.kramerius.imaging;

import java.awt.Dimension;
import java.io.IOException;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.impl.TileSupportImpl;

public class DeepZoomTileSupportTest extends AbstractGuiceTestCase {


    @Test
    public void testTileSupport() throws IOException {
        Injector inj = injector();
        DeepZoomTileSupport tileSupp = inj.getInstance(DeepZoomTileSupport.class);
        int tileSize = tileSupp.getTileSize();
        Assert.assertTrue(tileSize == 512);

        int cols = tileSupp.getCols(new Dimension(8949, 6684));
        Assert.assertTrue(cols == 18);
        
        int rows = tileSupp.getRows(new Dimension(8949, 6684));
        Assert.assertTrue(rows == 14);
        
        int levels = tileSupp.getLevels(new Dimension(8949, 6684), tileSize);
        Assert.assertTrue(levels == 6);
        
        
    }

    @Override
    protected Injector injector() {
        Injector injector = Guice.createInjector(new _Module());
        return injector;
    }

    
    class _Module extends AbstractModule {

        @Override
        protected void configure() {
            FedoraAccess fa = EasyMock.createMock(FedoraAccess.class);
            TileSupportImpl tis = EasyMock.createMockBuilder(TileSupportImpl.class).withConstructor()
            .addMockedMethod("getTileSize").createMock();
            
            EasyMock.expect(tis.getTileSize()).andReturn(512).anyTimes();
            
            EasyMock.replay(fa,tis);
            
            
            bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(fa);
            bind(DeepZoomTileSupport.class).toInstance(tis);
            
            
            // TODO Auto-generated method stub
            
        }
    }
}
