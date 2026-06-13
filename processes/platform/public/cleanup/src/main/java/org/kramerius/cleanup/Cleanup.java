package org.kramerius.cleanup;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.dochub.CleanableSpace;
import cz.inovatika.dochub.CleanupStrategy;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Cleanup
 * @author ppodsednik
 */
public class Cleanup {
    public static Logger LOGGER = Logger.getLogger(Cleanup.class.getName());

    private final Map<SpaceType, CleanableSpace> spaces;

    @Inject
    public Cleanup(Map<SpaceType, CleanableSpace> spaces) {
        this.spaces = spaces;
    }

    @ProcessMethod
    public static void cleanup(
            @ParameterName("spaceType") @IsRequired String spaceTypeS,
            @ParameterName("strategy") @IsRequired String strategyS
    ) {
        Injector injector = Guice.createInjector(new CleanupModule());
        Cleanup cleanup = injector.getInstance(Cleanup.class);
        try {
            SpaceType spaceType = SpaceType.valueOf(spaceTypeS.toUpperCase());
            StrategyType strategy = StrategyType.valueOf(strategyS.toUpperCase());
            cleanup.execute(spaceType, strategy);
        } catch (IllegalArgumentException e) {
            LOGGER.severe("Error: Invalid Space or Strategy type provided.");
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOGGER.severe("Fatal error during cleanup execution:");
            throw new RuntimeException(e);
        }
    }

    private void execute(SpaceType spaceType, StrategyType strategyType) throws IOException {
        CleanableSpace space = spaces.get(spaceType);
        if (space == null) {
            throw new IllegalStateException("Space not found: " + spaceType);
        }
        CleanupStrategy strategy = createStrategy(strategyType, space);
        LOGGER.info("Starting " + strategyType + " cleanup on " + spaceType + " space...");
        space.cleanup(strategy);
        LOGGER.info("Cleanup successfully completed for " + spaceType);
    }

    private CleanupStrategy createStrategy(StrategyType strategyType, CleanableSpace space) throws IOException {
        return switch (strategyType) {
            case SIZE_LIMIT -> {
                Path rootPath = space.getRootPath();
                int maxGb = space.getConfiguredMaxLimit();
                long limitInBytes = (long) maxGb * 1024 * 1024 * 1024;
                yield new SizeLimitCleanupStrategy(limitInBytes, rootPath);
            }
            case EXPIRATION -> {
                // TODO we use now only one config property for both spaces
                int expirationHours = space.getConfiguredMaxAge();
                yield new ExpirationCleanupStrategy(expirationHours);
            }
            case FORCE_EMPTY -> {
                LOGGER.warning("Warning: FORCE_EMPTY strategy selected. All files will be deleted.");
                yield (file, attrs) -> true;
            }
            default -> throw new UnsupportedOperationException("Strategy " + strategyType + " is not implemented yet.");
        };
    }

}
