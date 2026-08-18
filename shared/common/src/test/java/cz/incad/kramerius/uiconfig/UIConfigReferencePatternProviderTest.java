package cz.incad.kramerius.uiconfig;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UIConfigReferencePatternProviderTest {

    @Test
    public void returnsDefaultPatternsWhenConfigurationIsMissing() {
        UIConfigReferencePatternProvider provider = new UIConfigReferencePatternProvider(new BaseConfiguration());

        Assert.assertEquals(
                List.of("/pages/content/*"),
                provider.getResourceReferencePatterns(UIConfigType.GENERAL));
    }

    @Test
    public void usesConfiguredPatternsWhenConfigurationKeyExists() {
        Configuration configuration = new BaseConfiguration();
        configuration.setProperty(
                "ui_config.resource_reference_patterns.general",
                "/custom/logo,/custom/pages/*");

        UIConfigReferencePatternProvider provider = new UIConfigReferencePatternProvider(configuration);

        Assert.assertEquals(
                List.of("/custom/logo", "/custom/pages/*"),
                provider.getResourceReferencePatterns(UIConfigType.GENERAL));
    }

    @Test
    public void trimsConfiguredPatternsAndSkipsEmptyValues() {
        Configuration configuration = new BaseConfiguration();
        configuration.setProperty(
                "ui_config.resource_reference_patterns.licenses",
                " /custom/logo, ,/custom/pages/* ");

        UIConfigReferencePatternProvider provider = new UIConfigReferencePatternProvider(configuration);

        Assert.assertEquals(
                List.of("/custom/logo", "/custom/pages/*"),
                provider.getResourceReferencePatterns(UIConfigType.LICENSES));
    }

    @Test
    public void allowsEmptyOverride() {
        Configuration configuration = new BaseConfiguration();
        configuration.setProperty("ui_config.resource_reference_patterns.general", "");

        UIConfigReferencePatternProvider provider = new UIConfigReferencePatternProvider(configuration);

        Assert.assertTrue(provider.getResourceReferencePatterns(UIConfigType.GENERAL).isEmpty());
    }
}
