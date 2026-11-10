package cz.incad.kramerius.statistics;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decides whether an IIIF Image API request ({region}/{size}/{rotation}/{quality}.{format})
 * is a tile of a zoomable viewer. Viewer tiles are not written to the access log: the viewers
 * (OpenSeadragon in the Kramerius clients) ask for info.json first, which is logged as the
 * "page displayed" event, and then load dozens of tiles per page. Everything else (crops,
 * whole images, best-fit thumbnails, external IIIF clients) is logged.
 * <p>
 * A viewer tile is a request where, for some tile size T (a power of two between
 * {@link #MIN_TILE_SIZE} and the configured maximum; the image servers advertise 256 px tiles
 * and the CDK client enlarges them to 512 px) and some scale factor s (a power of two):
 * <ul>
 * <li>size is {@code w,h} (or {@code w,}) without {@code !}, {@code ^}, {@code pct:}, {@code max}, {@code full},
 * and both dimensions are at most T,</li>
 * <li>region is {@code x,y,w,h} aligned to the tile grid: x and y are multiples of T*s, the region is at
 * most T*s and ceil(region/s) equals the size; or region is {@code full} (the lowest zoom levels, whole
 * image smaller than one tile),</li>
 * <li>rotation is 0 and the quality is default or native.</li>
 * </ul>
 * The scale factor is searched, not derived from the ratio: edge tiles are cut by the image border
 * (e.g. region 77 px wide delivered as 5 px at scale 16) and the ratio is not an integer there.
 * <p>
 * Limits: a viewer configured with a tile size above the maximum, or not a power of two, produces
 * requests that look like crops and are logged; a crop that happens to match the grid exactly is not.
 */
public final class ImageAccessLogPolicy {

    /** tile size the image servers behind Kramerius advertise (info.json "tiles") */
    public static final int MIN_TILE_SIZE = 256;
    /** largest tile size accepted by default; the CDK client requests 512 px tiles (configuration key iiif.tile.maxsize) */
    public static final int DEFAULT_MAX_TILE_SIZE = 512;
    private static final int MAX_SCALE_FACTOR = 1024;

    /**
     * Configuration key: log viewer tiles too (default false). With the default, a request recognized
     * as a tile by {@link #isViewerTile} or declared as one by the client (X-Kramerius-Intent contains
     * "tile") is not written to the access log; tiles come in dozens per page and would flood it.
     * Set to true on an installation to inspect what the viewers really request.
     */
    public static final String CONF_LOG_TILES = "statistics.image.log_tiles";
    /** value of the X-Kramerius-Intent header a viewer sends with its tiles */
    public static final String INTENT_TILE = "tile";

    private static final Pattern REGION_XYWH = Pattern.compile("(\\d{1,9}),(\\d{1,9}),(\\d{1,9}),(\\d{1,9})");
    private static final Pattern SIZE_WH = Pattern.compile("(\\d{1,9}),(\\d{0,9})");

    private ImageAccessLogPolicy() {
    }

    /**
     * @param region   IIIF region segment, already URL decoded
     * @param size     IIIF size segment, already URL decoded
     * @param rotation IIIF rotation segment
     * @param qualityFormat IIIF {quality}.{format} segment, may be null
     * @param maxTileSize largest tile size a viewer may request (configuration key iiif.tile.maxsize),
     *                    {@link #DEFAULT_MAX_TILE_SIZE} when not configured
     * @return true when the request is a tile of a zoomable viewer and should not be logged
     */
    public static boolean isViewerTile(String region, String size, String rotation, String qualityFormat, int maxTileSize) {
        if (region == null || size == null) {
            return false;
        }
        int maxT = maxTileSize >= MIN_TILE_SIZE ? maxTileSize : DEFAULT_MAX_TILE_SIZE;
        if (rotation != null && !"0".equals(rotation.trim())) {
            return false;
        }
        if (qualityFormat != null) {
            String quality = qualityFormat.toLowerCase(Locale.ROOT);
            int dot = quality.indexOf('.');
            if (dot >= 0) {
                quality = quality.substring(0, dot);
            }
            if (!"default".equals(quality) && !"native".equals(quality)) {
                return false;
            }
        }
        String normalizedSize = size.trim().toLowerCase(Locale.ROOT);
        Matcher sizeMatcher = SIZE_WH.matcher(normalizedSize);
        if (!sizeMatcher.matches()) {
            return false; // max, full, pct:, !w,h, ^w,h, ,h
        }
        long sizeW = Long.parseLong(sizeMatcher.group(1));
        long sizeH = sizeMatcher.group(2).isEmpty() ? -1 : Long.parseLong(sizeMatcher.group(2));
        if (sizeW <= 0 || sizeW > maxT || sizeH > maxT || sizeH == 0) {
            return false;
        }
        String normalizedRegion = region.trim().toLowerCase(Locale.ROOT);
        if ("full".equals(normalizedRegion)) {
            return true; // lowest zoom levels: whole image delivered smaller than one tile
        }
        Matcher regionMatcher = REGION_XYWH.matcher(normalizedRegion);
        if (!regionMatcher.matches()) {
            return false; // square, pct:
        }
        long x = Long.parseLong(regionMatcher.group(1));
        long y = Long.parseLong(regionMatcher.group(2));
        long w = Long.parseLong(regionMatcher.group(3));
        long h = Long.parseLong(regionMatcher.group(4));
        if (w <= 0 || h <= 0) {
            return false;
        }
        for (long t = MIN_TILE_SIZE; t <= maxT; t *= 2) {
            if (sizeW > t || sizeH > t) {
                continue;
            }
            for (long s = 1; s <= MAX_SCALE_FACTOR; s *= 2) {
                long cell = t * s;
                if (w > cell || h > cell) {
                    continue;
                }
                if (x % cell != 0 || y % cell != 0) {
                    continue;
                }
                long expectedW = ceilDiv(w, s);
                long expectedH = ceilDiv(h, s);
                if (expectedW == sizeW && (sizeH < 0 || expectedH == sizeH)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isViewerTile(String region, String size, String rotation, String qualityFormat) {
        return isViewerTile(region, size, rotation, qualityFormat, DEFAULT_MAX_TILE_SIZE);
    }

    /** true when the X-Kramerius-Intent header (comma separated, case insensitive) declares the request a viewer tile */
    public static boolean hasTileIntent(String intentHeader) {
        if (intentHeader == null) {
            return false;
        }
        for (String part : intentHeader.split(",")) {
            if (INTENT_TILE.equals(part.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether an IIIF image request should be written to the access log: not when tiles are off
     * (configuration {@link #CONF_LOG_TILES}, default) and the request is a tile by its shape or by the
     * client's declared intent.
     */
    public static boolean shouldLog(String region, String size, String rotation, String qualityFormat,
                                    int maxTileSize, String intentHeader, boolean logTiles) {
        if (logTiles) {
            return true;
        }
        if (hasTileIntent(intentHeader)) {
            return false;
        }
        return !isViewerTile(region, size, rotation, qualityFormat, maxTileSize);
    }

    private static long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }
}
