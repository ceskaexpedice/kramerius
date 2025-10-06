package cz.incad.kramerius.imaging.lp;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class GenerateDeepZoomCache {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GenerateDeepZoomCache.class.getName());

    public static void main(String[] args) throws IOException  {
        LOGGER.log(Level.INFO,"Generate deep zoom cache :" + Arrays.asList(args));
        if (args.length >= 1) {
            Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule(), new SolrModule(), new RepoModule(), new NullStatisticsModule());
            int numberStepsOverTile = KConfiguration.getInstance().getConfiguration().getInt("deepZoom.numberStepsOverTile",1);
            if (args.length == 2) {
                numberStepsOverTile = Integer.parseInt(args[1]);
            }
            DeepZoomCacheService service = injector.getInstance(Key.get(DeepZoomCacheService.class, Names.named("memoryCacheForward")));
            service.prepareCacheForPID(args[0],numberStepsOverTile+1);
            
            
            boolean spawnRELSEXTFlag = Boolean.getBoolean(GenerateDeepZoomFlag.class.getName());
            if (spawnRELSEXTFlag) {
                String[] processArgs = {GenerateDeepZoomFlag.Action.SET.name(),args[0],"kramerius4://deepZoomCache"};
                // TODO pepo ProcessUtils.startProcess("generateDeepZoomFlag", processArgs);
            } else {
                LOGGER.warning("no subprocess for generating flag in rels-ext");
            }
            
            
            boolean spawnGeneratethumb = Boolean.getBoolean(GenerateThumbnail.class.getName());
            if (spawnGeneratethumb) {
                String[] processArgs = {args[0]};
                // TODO pepo ProcessUtils.startProcess("generateFullThumb", processArgs);
            } else {
                LOGGER.warning("no subprocess for generating thumbs");
            }
            
            
            LOGGER.info("Process finished");
        } else {
            LOGGER.severe("generate cache class <uuid>");
        }
    }
}
