package cz.incad.kramerius.imaging;

import java.awt.Image;
import java.awt.image.BufferedImage;
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
		Injector injector = injector();
		CacheService cacheService = injector.getInstance(CacheService.class);
		TileSupport tileSupport = injector.getInstance(TileSupport.class);
		Assert.assertNotNull(cacheService);
		BufferedImage rawImage = tileSupport.getRawImage("4308eb80-b03b-11dd-a0f6-000d606f5dc6");
		long st = System.currentTimeMillis();
//		cacheService.prepareCacheForUUID("0eaa6730-9068-11dd-97de-000d606f5dc6");
		cacheService.prepareCacheImage("4308eb80-b03b-11dd-a0f6-000d606f5dc6", 1, rawImage);
		
		
//		cacheService.prepareCacheForUUID("8f526130-8b0d-11de-8994-000d606f5dc6");
		System.out.println(System.currentTimeMillis() - st);
	}
	
	@Override
	protected Injector injector() {
		Injector injector = Guice.createInjector(new ImagingModule());
		return injector;
	}

	
}
