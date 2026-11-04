package cz.incad.kramerius.statistics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Names and builders of the additional access log fields passed to
 * {@link StatisticsAccessLog#reportAccess(String, String, String, Map)}.
 * The names must match the fields of the Solr core "logs".
 */
public final class AccessLogFields {

    /** mime type of the accessed datastream as stored (image/jpeg, application/pdf, application/epub+zip, ...) */
    public static final String STREAM_MIME = "stream_mime";
    /** how an image stream was accessed, see {@link ImageAccessKind} */
    public static final String IMAGE_KIND = "image_kind";

    private AccessLogFields() {
    }

    public static Map<String, String> image(ImageAccessKind kind, String mimeType) {
        Map<String, String> fields = kind(kind);
        if (mimeType != null && !mimeType.isEmpty()) {
            fields.put(STREAM_MIME, mimeType);
        }
        return fields;
    }

    public static Map<String, String> kind(ImageAccessKind kind) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (kind != null) {
            fields.put(IMAGE_KIND, kind.name());
        }
        return fields;
    }

    public static Map<String, String> mime(String mimeType) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (mimeType != null && !mimeType.isEmpty()) {
            fields.put(STREAM_MIME, mimeType);
        }
        return fields;
    }
}
