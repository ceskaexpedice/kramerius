package cz.incad.kramerius.security.licenses.limits;

/*
public record PageDimension(float width, float height) {
}*/

/*
    // generate content - pdf, text, ebook
    public boolean isOfflineGenerateContentAllowed();
    public void setOfflineGenerateContentAllowed(boolean flag);
    public boolean isOfflineGenerateContentLimited();
    public void setOfflineGenerateContentLimited(boolean flag);

    public LicenseOfflineGenerationConf getLimitConfiguration();

    public void setLimitConfiguration(LicenseOfflineGenerationConf limitConfiguration);

    public boolean checkUsageLimit(User user, String pid, UserContentSpace userContentSpace);// license settings int intervalValue, LimitInterval  limitInterval, int maxAllowedUsage);

*/

// license settings int intervalValue, LimitInterval  limitInterval, int maxAllowedUsage)
public record OfflineGenerationConf(
        boolean offlineGenrateAllowed,
        LimitConfiguration limitConfiguration
) {

}
