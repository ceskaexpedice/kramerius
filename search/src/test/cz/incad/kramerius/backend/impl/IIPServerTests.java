package cz.incad.kramerius.backend.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.easymock.EasyMock;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.GeneratePDFServiceImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.TestCase;

public class IIPServerTests extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        
    }

    public void testUpdate() throws UnsupportedEncodingException, IOException {
        KConfiguration kconf = KConfiguration.getInstance();
        kconf.getConfiguration().setProperty("UrlOfIIPserver", "http://192.168.1.3/fcgi-bin/iipsrv.fcgi");
        kconf.getConfiguration().setProperty("fedoraDataFolderOnIIPServer", "/mnt/fc/data");
            

        Injector injector = Guice.createInjector(new ModuleForSearchTests());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("securedFedoraAccess")));
        
        EasyMock.expect(fa.getMimeTypeForStream("uuid:abc-eef-gef", "IMG_FULL")).andReturn("image/jp2");
        //EasyMock.replay(fa);

        StringTemplateGroup stGroup = AbstractImageServlet.stGroup();
        StringTemplate template = stGroup.getInstanceOf("dziurl");
        AbstractImageServlet.setStringTemplateModel("abc-eef-gef", "/mnt/fc/data", template, fa);
        
        System.out.println(template.toString());
    }
}
