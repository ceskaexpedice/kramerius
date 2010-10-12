package cz.incad.kramerius;

import java.io.IOException;

import org.junit.Test;

import junit.framework.TestCase;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class FedoraAccessTestCase extends AbstractGuiceTestCase {

	@Test
	public void testFullImageDSPresent() throws IOException {
		// uuid dorbnustky = IMG_FULL not present
		KConfiguration.getInstance().getConfiguration().setProperty("_fedoraTomcatHost","http://vmkramerius:8080");
		Injector inj = injector();
		FedoraAccess fa = inj.getInstance(FedoraAccess.class);
		boolean ifPresent = fa.isImageFULLAvailable("0eaa6730-9068-11dd-97de-000d606f5dc6");
		TestCase.assertFalse(ifPresent);
		
		// uuid stranka z dorbnustek = IMG_FULL present
		ifPresent = fa.isImageFULLAvailable("4308eb80-b03b-11dd-a0f6-000d606f5dc6");
		TestCase.assertTrue(ifPresent);
	}
	
	@Override
	protected Injector injector() {
		Injector injector = Guice.createInjector(new CommonModule());
		return injector;
	}
	
}
