package cz.incad.kramerius.imaging.lp;

import java.io.IOException;
import java.util.Arrays;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.imaging.CacheService;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;

public class GenerateDeepZoomCache {

	static java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GenerateDeepZoomCache.class.getName());
	
	public static void main(String[] args) throws IOException {
		System.out.println("Generate deep zoom cache :"+Arrays.asList(args));
		if (args.length == 1) {
			Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule());
			CacheService service = injector.getInstance(CacheService.class);
			service.prepareCacheForUUID(args[0]);
			LOGGER.info("Process finished");
		} else {
			LOGGER.severe("generate cache class <uuid>");
		}
	}
}
