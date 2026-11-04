package cz.incad.kramerius.statistics;

/**
 * How an image stream was accessed; stored in the access log as the field image_kind.
 * Tiles of the zoomable viewers are listed for completeness, they are not logged.
 */
public enum ImageAccessKind {
    /** whole stream from IMG_FULL (image, pdf, epub, ...) */
    FULL,
    PREVIEW,
    THUMB,
    /** IIIF image info.json, the "page displayed" event of the viewers */
    IIIF_INFO,
    /** IIIF image region/size request that is not a viewer tile (crop, whole image, external client) */
    IIIF_REGION,
    /** IIIF presentation manifest */
    IIIF_MANIFEST,
    ZOOMIFY_DESCRIPTOR,
    ZOOMIFY_TILE,
    DEEPZOOM_DESCRIPTOR,
    DEEPZOOM_TILE
}
