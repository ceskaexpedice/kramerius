package cz.incad.kramerius.security.licenses.limits;

public record LimitConfiguration(
        int intervalValue,
        LimitInterval limitInterval,
        int maxAllowedUsage
) {
}
