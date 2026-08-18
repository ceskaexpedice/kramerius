package cz.incad.kramerius.uiconfig;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Provides UI config resource reference patterns.
 */
public class UIConfigReferencePatternProvider {

    private static final String CONFIG_KEY_PREFIX = "ui_config.resource_reference_patterns.";

    private final Configuration configuration;

    public UIConfigReferencePatternProvider() {
        this(KConfiguration.getInstance().getConfiguration());
    }

    public UIConfigReferencePatternProvider(Configuration configuration) {
        this.configuration = configuration;
    }

    public List<String> getResourceReferencePatterns(UIConfigType type) {
        UIConfigDefinition definition = getDefinition(type);
        String configurationKey = CONFIG_KEY_PREFIX + definition.getEndpoint();

        return configuration.containsKey(configurationKey)
                ? getConfiguredPatterns(configurationKey)
                : getDefaultResourceReferencePatterns(type);
    }

    public List<UIConfigDefinition> getDefinitions() {
        return List.of(
                getDefinition(UIConfigType.GENERAL),
                getDefinition(UIConfigType.LICENSES),
                getDefinition(UIConfigType.CURATOR_LISTS)
        );
    }

    public UIConfigDefinition getDefinition(UIConfigType type) {
        switch (type) {
            case GENERAL:
                return new UIConfigDefinition(
                        UIConfigType.GENERAL,
                        "ui-config.general",
                        "general",
                        "General JSON",
                        "object"
                );
            case LICENSES:
                return new UIConfigDefinition(
                        UIConfigType.LICENSES,
                        "ui-config.licenses",
                        "licenses",
                        "Licenses JSON",
                        "object"
                );
            case CURATOR_LISTS:
            default:
                return new UIConfigDefinition(
                        UIConfigType.CURATOR_LISTS,
                        "ui-config.curator-lists",
                        "curator-lists",
                        "Curator lists JSON",
                        "array"
                );
        }
    }

    private List<String> getDefaultResourceReferencePatterns(UIConfigType type) {
        switch (type) {
            case GENERAL:
                return List.of(
                        "/pages/content/*"
                );
            case LICENSES:
                return List.of(
                        "/bar/logo",
                        "/messagePages/page/*",
                        "/instructionPage/*",
                        "/providedBy/imageUrl",
                        "/licenses/bar/logo",
                        "/licenses/messagePages/page/*",
                        "/licenses/instructionPage/*",
                        "/licenses/providedBy/imageUrl"
                );
            case CURATOR_LISTS:
            default:
                return List.of();
        }
    }

    private List<String> getConfiguredPatterns(String key) {
        if (!configuration.containsKey(key)) {
            return List.of();
        }

        return Arrays.stream(configuration.getStringArray(key))
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty())
                .toList();
    }

}
