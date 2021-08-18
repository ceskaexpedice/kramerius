package cz.incad.kramerius.statistics.impl.nkp;

import com.google.inject.Provider;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.Assert;
import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;

public class ParametrizedNKPInputTemplateTest {

    public static final String BUNDLES ="" +
            "nkplog.datefrom.label = Date from\n" +
            "nkplog.dateto.label = Date to\n" +
            "nkplog.folder.label = Logs folder\n" +
            "nkplog.institution.label = Institution";




    public interface _TestLocaleProvider extends Provider<Locale> {}

    @Test
    public void testInputTemplateTest() throws IOException {
        Provider<Locale> localeProvider = EasyMock.createMock(_TestLocaleProvider.class);
        EasyMock.expect(localeProvider.get()).andReturn(Locale.getDefault()).anyTimes();

        ResourceBundleService resb = EasyMock.createMock(ResourceBundleService.class);
        PropertyResourceBundle resourceBundle = new PropertyResourceBundle(new StringReader(BUNDLES));
        EasyMock.expect(resb.getResourceBundle("labels", Locale.getDefault())).andReturn(resourceBundle).anyTimes();

        KConfiguration conf = EasyMock.createMock(KConfiguration.class);
        EasyMock.expect(conf.getProperty("import.directory")).andReturn(System.getProperty("user.dir")).anyTimes();

        EasyMock.expect(conf.getProperty("convert.target.directory")).andReturn(System.getProperty("user.dir")).anyTimes();
        EasyMock.expect(conf.getProperty("convert.directory")).andReturn(System.getProperty("user.dir")).anyTimes();

        Configuration subConfObject = EasyMock.createMock(Configuration.class);
        EasyMock.expect(conf.getConfiguration()).andReturn(subConfObject).anyTimes();

        EasyMock.expect(subConfObject.getBoolean("ingest.skip")).andReturn(true).anyTimes();
        EasyMock.expect(subConfObject.getBoolean("ingest.startIndexer")).andReturn(true).anyTimes();
        EasyMock.expect(subConfObject.getBoolean("convert.defaultRights")).andReturn(true).anyTimes();

        EasyMock.replay(localeProvider,resb, conf, subConfObject);

        ParametrizedNKPInputTemplate temp = new ParametrizedNKPInputTemplate();
        temp.localesProvider = localeProvider;
        temp.resourceBundleService = resb;
        temp.configuration = conf;

        StringWriter nstr = new StringWriter();
        temp.renderInput(null,nstr, new Properties());
        Assert.assertNotNull(nstr.toString());
    }

}
