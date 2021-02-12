package cz.cas.lib.knav;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.impl.criteria.MovingWall;

public class ApplyMovingWallTest {
    
    @Test
    public void testConfiguration1() throws IOException {
        SolrAccess sa = EasyMock.createMock(SolrAccess.class);
        ObjectPidsPath pidPath = new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
        ObjectModelsPath modelPAth = new ObjectModelsPath("periodical");
        EasyMock.expect(sa.getPidPaths("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectPidsPath[] {pidPath});
        EasyMock.expect(sa.getModelPaths("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectModelsPath[] {modelPAth});
        
        Configuration configuration = EasyMock.createMock(Configuration.class);
        EasyMock.expect(configuration.getInt("mwprocess.wall",70)).andReturn(70);
        EasyMock.expect(configuration.containsKey("mwprocess.model.periodical.wall")).andReturn(false);
        EasyMock.replay(sa,configuration);
        
        int configuredWall = ApplyMWUtils.configuredWall(sa, "uuid:045b1250-7e47-11e0-add1-000d606f5dc6", configuration);
        Assert.assertTrue(configuredWall == 70);
    }


    @Test
    public void testConfiguration2() throws IOException {
        SolrAccess sa = EasyMock.createMock(SolrAccess.class);
        ObjectPidsPath pidPath = new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
        ObjectModelsPath modelPAth = new ObjectModelsPath("periodical");
        EasyMock.expect(sa.getPidPaths("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectPidsPath[] {pidPath});
        EasyMock.expect(sa.getModelPaths("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectModelsPath[] {modelPAth});
        
        Configuration configuration = EasyMock.createMock(Configuration.class);
        EasyMock.expect(configuration.getInt("mwprocess.wall",70)).andReturn(70);
        EasyMock.expect(configuration.containsKey("mwprocess.model.periodical.wall")).andReturn(true);
        EasyMock.expect(configuration.getInt("mwprocess.model.periodical.wall")).andReturn(20);
        EasyMock.replay(sa,configuration);
        
        int configuredWall = ApplyMWUtils.configuredWall(sa, "uuid:045b1250-7e47-11e0-add1-000d606f5dc6", configuration);
        Assert.assertTrue(configuredWall == 20);
    }
    
    @Test
    public void testPattern() throws IOException {
        Date date = MovingWall.customizedDates("2001.07.04 at 12:08:56 PDT", Arrays.asList("yyyy.MM.dd 'at' HH:mm:ss 'PDT'"));
        Assert.assertNotNull(date);

        date = MovingWall.customizedDates("2001.07.04 at 12:08:56 PDT", Arrays.asList("yyyy HH:mm:ss 'PDT'","yyyy.MM HH:mm:ss 'PDT'"));
        Assert.assertNull(date);
        
    }
    @Test
    public void testFlags() {
        Assert.assertTrue(ApplyMWUtils.detectChange(false, null));
        Assert.assertTrue(ApplyMWUtils.detectChange(true, null));
        Assert.assertTrue(ApplyMWUtils.detectChange(false, "policy:public"));
        Assert.assertTrue(ApplyMWUtils.detectChange(true, "policy:private"));

        Assert.assertFalse(ApplyMWUtils.detectChange(false, "policy:private"));
        Assert.assertFalse(ApplyMWUtils.detectChange(true, "policy:public"));

    }
    

}
