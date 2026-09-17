package cz.incad.kramerius.iiif;

/**
 * Classifies IIIF image requests that can be served as small tiles without an
 * extra object-level authorization probe.
 */
public final class IIIFRequestGuard {

    public static final int DEFAULT_MAX_TILE_SIZE = 512;

    private IIIFRequestGuard() {
    }

    public static boolean isSmallTileRequest(String region, String size, int maxTileSize) {
        return !requiresFullReadCheck(region, size, maxTileSize);
    }

    public static boolean requiresInfoJsonProbe(String region, String size, int maxTileSize) {
        return requiresFullReadCheck(region, size, maxTileSize);
    }

    public static boolean requiresFullReadCheck(String region, String size, int maxTileSize) {
        int effectiveMaxTileSize = maxTileSize > 0 ? maxTileSize : DEFAULT_MAX_TILE_SIZE;
        return requiresProbeByRegion(region, effectiveMaxTileSize)
                || requiresProbeBySize(size, effectiveMaxTileSize);
    }

    private static boolean requiresProbeByRegion(String region, int maxTileSize) {
        if (isBlank(region)) {
            return true;
        }

        String normalized = region.trim().toLowerCase();
        if ("full".equals(normalized) || "square".equals(normalized) || normalized.contains("pct:")) {
            return true;
        }

        String[] values = normalized.split(",");
        if (values.length != 4) {
            return true;
        }

        Integer width = parsePositiveInt(values[2]);
        Integer height = parsePositiveInt(values[3]);
        if (width == null || height == null) {
            return true;
        }
        return width > maxTileSize || height > maxTileSize;
    }

    private static boolean requiresProbeBySize(String size, int maxTileSize) {
        if (isBlank(size)) {
            return true;
        }

        String normalized = size.trim().toLowerCase();
        if ("max".equals(normalized) || normalized.contains("pct:")) {
            return true;
        }

        String[] values = normalized.split(",", -1);
        if (values.length < 1 || values.length > 2) {
            return true;
        }

        Integer width = parseIiifSizeValue(values[0]);
        Integer height = values.length == 2 ? parseIiifSizeValue(values[1]) : null;
        if (width == null && height == null) {
            return true;
        }
        return isOverLimit(width, maxTileSize) || isOverLimit(height, maxTileSize);
    }

    private static Integer parseIiifSizeValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replace("^", "").replace("!", "");
        if (normalized.isEmpty()) {
            return null;
        }
        return parsePositiveInt(normalized);
    }

    private static Integer parsePositiveInt(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isOverLimit(Integer value, int maxTileSize) {
        return value != null && value > maxTileSize;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
