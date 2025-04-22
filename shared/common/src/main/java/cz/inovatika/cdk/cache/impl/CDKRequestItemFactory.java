package cz.inovatika.cdk.cache.impl;

import cz.inovatika.cdk.cache.CDKRequestItem;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Factory class for creating instances of {@link CDKRequestItem}.
 * This class provides methods for creating cache items from raw data
 * as well as from database query results.
 */
public class CDKRequestItemFactory {

    /**
     * Creates a cache item based on the provided data type.
     * Supports String and ByteBuffer-based data.
     *
     * @param data               The data to be cached, either a {@link String} or a {@link ByteBuffer}.
     * @param mimeType           The MIME type of the data.
     * @param url                The URL associated with the cached request.
     * @param pid                The persistent identifier of the requested resource.
     * @param dlAcronym          The acronym of the digital library from which the request originated.
     * @param localDateTime      The timestamp indicating when the request was made.
     * @param userIdentification The identification of the user making the request.
     * @return A {@link CDKRequestItem} instance containing the provided data.
     * @throws IllegalArgumentException If the provided data type is unsupported.
     */
    public static CDKRequestItem<?> createCacheItem(

        Object data, String mimeType, String url, String pid, String dlAcronym, LocalDateTime localDateTime, String userIdentification) {

        if (data instanceof String) {
            return new StringCDKRequestItem.Builder()
                    .data((String) data)
                    .mimeType(mimeType)
                    .url(url)
                    .pid(pid)
                    .dlAcronym(dlAcronym)
                    .timestamp(localDateTime)
                    .userIdentification(userIdentification)
                    .build();
        } else if (data instanceof byte[]) {
            return new ByteBufferCDKRequestItem.Builder()
                    .data(ByteBuffer.wrap((byte[]) data))
                    .mimeType(mimeType)
                    .url(url)
                    .pid(pid)
                    .dlAcronym(dlAcronym)
                    .timestamp(localDateTime)
                    .userIdentification(userIdentification)
                    .build();
        } else if (data instanceof ByteBuffer) {
            return new ByteBufferCDKRequestItem.Builder()
                    .data((ByteBuffer) data)
                    .mimeType(mimeType)
                    .url(url)
                    .pid(pid)
                    .dlAcronym(dlAcronym)
                    .timestamp(localDateTime)
                    .userIdentification(userIdentification)
                    .build();
        } else {
            throw new IllegalArgumentException("Unsupported data type: " + data.getClass());
        }
    }

    /**
     * Creates a cache item from a database result set.
     * Extracts the relevant columns from the provided {@link ResultSet}
     * and constructs an appropriate cache item.
     *
     * @param rs The {@link ResultSet} containing the cached data from the database.
     * @return A {@link CDKRequestItem} instance containing the extracted data.
     * @throws RuntimeException If both string-based and byte-based data are null or if an SQL error occurs.
     */
    public static CDKRequestItem<?> createCacheItemFromResultSet(ResultSet rs) {
        try {

            String mimeType = rs.getString(CDKRequestItem.MIME_TYPE_COLUMN);
            String url = rs.getString(CDKRequestItem.URL_COLUMN);
            String pid = rs.getString(CDKRequestItem.PID_COLUMN);
            String dlAcronym = rs.getString(CDKRequestItem.SOURCE_LIBRARY_COLUMN);
            LocalDateTime timestamp = rs.getTimestamp(CDKRequestItem.CREATED_AT_COLUMN).toLocalDateTime();

            String stringData = rs.getString(CDKRequestItem.TEXT_DATA_COLUMN);
            byte[] byteData = rs.getBytes(CDKRequestItem.BYTE_DATA_COLUMN);

            if (stringData != null) {
                return new StringCDKRequestItem.Builder()
                        .data(stringData)
                        .mimeType(mimeType)
                        .url(url)
                        .pid(pid)
                        .dlAcronym(dlAcronym)
                        .timestamp(timestamp)
                        .build();
            } else if (byteData != null) {
                return new ByteBufferCDKRequestItem.Builder()
                        .data(ByteBuffer.wrap(byteData))
                        .mimeType(mimeType)
                        .url(url)
                        .pid(pid)
                        .dlAcronym(dlAcronym)
                        .timestamp(timestamp)
                        .build();
            } else {
                throw new RuntimeException("Both string_data and byte_data are null in database record");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading item from ResultSet", e);
        }
    }

}