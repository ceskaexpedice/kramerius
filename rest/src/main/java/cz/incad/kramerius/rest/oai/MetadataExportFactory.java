package cz.incad.kramerius.rest.oai;

import cz.incad.kramerius.rest.oai.strategies.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating metadata export strategies based on the format.
 * This factory provides a way to retrieve the appropriate export strategy
 * based on the format string, allowing for flexible metadata export handling.
 */
public class MetadataExportFactory {


    private static final Map<String, MetadataExportStrategy> EXPORTS = new HashMap<>();
    static {
        EXPORTS.put("oai_dc", new OaiDcExportStrategy());
        EXPORTS.put("edm", new  EdmExportStrategy());
        EXPORTS.put("ese", new EseExportStrategy());
        EXPORTS.put("drkramerius4", new DrKramerius4ExportStrategy());
    }


    /**
     * Retrieves all available metadata export strategies.
     *
     * @return A collection of all MetadataExportStrategy instances.
     */
    public static Collection<MetadataExportStrategy> getAllExports() {
        return EXPORTS.values();
    }

    /**
     * Finds a metadata export strategy by its prefix.
     *
     * @param prefix The prefix of the metadata export strategy (e.g., "oai_dc").
     * @return An Optional containing the MetadataExportStrategy if found, or empty if not.
     */
    public static Optional<MetadataExportStrategy> findByPrefix(String prefix) {
        return Optional.ofNullable(EXPORTS.get(prefix));
    }
}