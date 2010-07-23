package cz.incad.kramerius.impl.fedora;

import java.awt.Image;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.processes.DefinitionModule;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import junit.framework.TestCase;

public class HandlerTest extends AbstractGuiceTestCase {

	@Test
	public void testDefinition() throws InterruptedException, XPathExpressionException, IOException {
		Injector inj = injector();
		KConfiguration.getInstance().getConfiguration().setProperty("_fedoraTomcatHost", "http://vmkramerius:8080");
		FedoraAccess fa = inj.getInstance(FedoraAccess.class);
		Image image = KrameriusImageSupport.readImage("4308eb80-b03b-11dd-a0f6-000d606f5dc6", FedoraUtils.IMG_FULL_STREAM, fa);
		TestCase.assertNotNull(image);
		
		image = KrameriusImageSupport.readImage("4308eb80-b03b-11dd-a0f6-000d606f5dc6", FedoraUtils.IMG_THUMB_STREAM, fa);
		TestCase.assertNotNull(image);

	}
	
	
	protected Injector injector() {
		Injector injector = Guice.createInjector(new DefinitionModule());
		return injector;
	}

}
