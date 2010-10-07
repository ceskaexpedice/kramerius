package cz.incad.kramerius.imaging;

import java.io.IOException;

import org.junit.Test;

import junit.framework.Assert;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.CommonModule;

public class ImagingTests extends AbstractGuiceTestCase {

	@Test
	public void testCacheDrobnustky() throws IOException {
		CacheService cacheService = injector().getInstance(CacheService.class);
		Assert.assertNotNull(cacheService);
		long st = System.currentTimeMillis();
//		cacheService.prepareCacheForUUID("0eaa6730-9068-11dd-97de-000d606f5dc6");

		cacheService.prepareCacheForUUID("8f526130-8b0d-11de-8994-000d606f5dc6");
		System.out.println(System.currentTimeMillis() - st);
	}
	
	@Override
	protected Injector injector() {
		Injector injector = Guice.createInjector(new ImagingModule());
		return injector;
	}

	
}
