package cz.incad.kramerius.client.i18n;

import junit.framework.Assert;
import junit.framework.TestCase;

public class BundleContentTest extends TestCase {

    public void testBundleContent() {
        BundleContent cont = new BundleContent("mods");
        cont.findOrCreateChildren("mods.page.partType.NormalPage");
        Assert.assertNotNull(cont.get("mods"));
        Assert.assertNotNull(cont.get("mods.page"));
        Assert.assertNotNull(cont.get("mods.page.partType"));
        Assert.assertNotNull(cont.get("mods.page.partType.NormalPage"));
 
        cont = new BundleContent("mods");
        cont.findOrCreateChildren("mods.page.partType.normalPage");
        Assert.assertNotNull(cont.get("mods"));
        Assert.assertNotNull(cont.get("mods.page"));
        Assert.assertNotNull(cont.get("mods.page.partType"));
        Assert.assertNotNull(cont.get("mods.page.partType.NormalPage"));
        Assert.assertNotNull(cont.get("mods.page.partType.normalPage"));
        
        
    }
}
