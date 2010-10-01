package cz.incad.kramerius.imaging.lp.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.CacheService;
import cz.incad.kramerius.imaging.TileSupport;
import cz.incad.kramerius.imaging.impl.CacheServiceImpl;
import cz.incad.kramerius.imaging.impl.TileSupportImpl;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.security.SecuredFedoraAccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class GenerateDeepZoomCacheModule extends AbstractModule {

	@Override
	protected void configure() {
		// mapped plain fedoraAccess as secured.  In this process it is not necessary to have checked access to fedora.
		bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
		bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
		bind(TileSupport.class).to(TileSupportImpl.class);
		bind(CacheService.class).to(CacheServiceImpl.class).in(Scopes.SINGLETON);
	}

	
}
