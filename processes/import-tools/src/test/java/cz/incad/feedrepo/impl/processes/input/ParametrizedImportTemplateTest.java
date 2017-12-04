package cz.incad.feedrepo.impl.processes.input;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Provider;

import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ParametrizedImportTemplateTest  {

    public static final String BUNDLES ="" +
            "convert.directory=convert.directory\n" +
            "target.directory=target.directory\n" +
            "convert.selection.dialog=convert.selection.dialog";
    
    public interface _TestLocaleProvider extends Provider<Locale> {}
    
    @Test
    public void testInputTemplateTest() throws IOException {
        Provider<Locale> localeProvider = EasyMock.createMock(_TestLocaleProvider.class);
        EasyMock.expect(localeProvider.get()).andReturn(Locale.getDefault()).anyTimes();
        
        ResourceBundleService resb = EasyMock.createMock(ResourceBundleService.class);
        PropertyResourceBundle resourceBundle = new PropertyResourceBundle(new StringReader(BUNDLES));
        EasyMock.expect(resb.getResourceBundle("labels", Locale.getDefault())).andReturn(resourceBundle).anyTimes();
        
        KConfiguration  conf = EasyMock.createMock(KConfiguration.class);
        EasyMock.expect(conf.getProperty("import.directory")).andReturn(System.getProperty("user.dir")).anyTimes();

        EasyMock.expect(conf.getProperty("convert.target.directory")).andReturn(System.getProperty("user.dir")).anyTimes();
        EasyMock.expect(conf.getProperty("convert.directory")).andReturn(System.getProperty("user.dir")).anyTimes();

        Configuration  subConfObject = EasyMock.createMock(Configuration.class);
        EasyMock.expect(conf.getConfiguration()).andReturn(subConfObject).anyTimes();

        EasyMock.expect(subConfObject.getBoolean("ingest.skip")).andReturn(true).anyTimes();
        EasyMock.expect(subConfObject.getBoolean("ingest.startIndexer")).andReturn(true).anyTimes();
        EasyMock.expect(subConfObject.getBoolean("convert.defaultRights")).andReturn(true).anyTimes();
        EasyMock.expect(subConfObject.getBoolean("ingest.updateExisting")).andReturn(false).anyTimes();

        EasyMock.replay(localeProvider,resb, conf, subConfObject);
        
        ParametrizedImportInputTemplate temp = new ParametrizedImportInputTemplate();
        temp.configuration=conf;
        temp.localesProvider = localeProvider;
        temp.resourceBundleService = resb;
        
        StringWriter nstr = new StringWriter();
        temp.renderInput(null,nstr, new Properties());
        String string = nstr.toString();
        //System.out.println(string);
        Assert.assertNotNull(string);
    }

}
