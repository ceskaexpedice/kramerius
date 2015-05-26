package cz.cas.lib.knav;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import cz.incad.kramerius.FedoraAccess;
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
        EasyMock.expect(sa.getPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectPidsPath[] {pidPath});
        EasyMock.expect(sa.getPathOfModels("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectModelsPath[] {modelPAth});
        
        Configuration configuration = EasyMock.createMock(Configuration.class);
        EasyMock.expect(configuration.getInt("mwprocess.wall",70)).andReturn(70);
        EasyMock.expect(configuration.containsKey("mwprocess.model.periodical.wall")).andReturn(false);
        EasyMock.replay(sa,configuration);
        
        int configuredWall = ApplyMovingWall.configuredWall(sa, "uuid:045b1250-7e47-11e0-add1-000d606f5dc6", configuration);
        Assert.assertTrue(configuredWall == 70);
    }


    @Test
    public void testConfiguration2() throws IOException {
        SolrAccess sa = EasyMock.createMock(SolrAccess.class);
        ObjectPidsPath pidPath = new ObjectPidsPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6");
        ObjectModelsPath modelPAth = new ObjectModelsPath("periodical");
        EasyMock.expect(sa.getPath("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectPidsPath[] {pidPath});
        EasyMock.expect(sa.getPathOfModels("uuid:045b1250-7e47-11e0-add1-000d606f5dc6")).andReturn(new ObjectModelsPath[] {modelPAth});
        
        Configuration configuration = EasyMock.createMock(Configuration.class);
        EasyMock.expect(configuration.getInt("mwprocess.wall",70)).andReturn(70);
        EasyMock.expect(configuration.containsKey("mwprocess.model.periodical.wall")).andReturn(true);
        EasyMock.expect(configuration.getInt("mwprocess.model.periodical.wall")).andReturn(20);
        EasyMock.replay(sa,configuration);
        
        int configuredWall = ApplyMovingWall.configuredWall(sa, "uuid:045b1250-7e47-11e0-add1-000d606f5dc6", configuration);
        Assert.assertTrue(configuredWall == 20);
    }
    
    @Test
    public void testPath() {
        String raw = "periodical/periodicalvolume/periodicalitem/page";
        String[] split = raw.split("/");
        for (String f : split) {
            System.out.println(f);
        }
    }
    
}
