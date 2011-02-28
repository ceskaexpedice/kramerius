package cz.incad.kramerius.imaging.lp;

import java.io.IOException;
import java.util.Arrays;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.lp.guice.Fedora3Module;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.imaging.lp.guice.PlainModule;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class GenerateDeepZoomCache {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GenerateDeepZoomCache.class.getName());

    public static void main(String[] args) throws IOException {
        System.out.println("Generate deep zoom cache :" + Arrays.asList(args));
        if (args.length >= 1) {
            Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule(), new Fedora3Module());
            int numberStepsOverTile = KConfiguration.getInstance().getConfiguration().getInt("deepZoom.numberStepsOverTile",1);
            if (args.length == 2) {
                numberStepsOverTile = Integer.parseInt(args[1]);
            }
            DeepZoomCacheService service = injector.getInstance(Key.get(DeepZoomCacheService.class, Names.named("memoryCacheForward")));
            service.prepareCacheForUUID(args[0],numberStepsOverTile+1);
            
            
            boolean spawnFlag = Boolean.getBoolean(GenerateDeepZoomFlag.class.getName());
            if (spawnFlag) {
                String[] processArgs = {GenerateDeepZoomFlag.Action.SET.name(),args[0],"kramerius4://deepZoomCache"};
                ProcessUtils.startProcess("generateDeepZoomFlag", processArgs);
            } else {
                LOGGER.warning("not set flag for ");
            }
            
            LOGGER.info("Process finished");
        } else {
            LOGGER.severe("generate cache class <uuid>");
        }
    }
}
