/*
  * Copyright (C) 2025  Inovatika
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
package cz.inovatika.cdk.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Represents a cached request item for a CDK (Central Digital Library) system.
 * This item stores request-related information, including metadata and actual data,
 * which can be either text or binary. It is used to optimize repeated queries
 * by caching responses related to rights, descriptors, metadata, and previews (thumbnails).
 *
 * @param <T> The type of data stored in the cache (e.g., text or binary).
 */
public interface CDKRequestItem<T> {

    /** Column name for the unique identifier of the request item. */
    String ID_COLUMN = "id";
    /** Column name for the MIME type of the cached data. */
    String MIME_TYPE_COLUMN = "mime_type";
    /** Column name for the URL of the requested resource. */
    String URL_COLUMN = "url";
    /** Column name for the persistent identifier (PID) of the resource. */
    String PID_COLUMN = "pid";
    /** Column name for the source digital library acronym. */
    String SOURCE_LIBRARY_COLUMN = "source_library";
    /** Column name for the timestamp when the item was created. */
    String CREATED_AT_COLUMN = "created_at";
    /** Column name for the cached text data. */
    String TEXT_DATA_COLUMN = "text_data";
    /** Column name for the cached binary data. */
    String BYTE_DATA_COLUMN = "binary_data";
    /** Column name for the user identification associated with the request. */
    String USER_IDENTIFICATION_COLUMN = "user_identification";
    /** Default value for common (anonymous) users. */
    String COMMON_USER = "common_user";

    /**
     * Gets the unique identifier of the cache entry.
     *
     * @return The unique ID of the request item.
     */
    public String getId();

    /**
     * Gets the URL of the requested resource.
     *
     * @return The URL of the cached request.
     */
    public String getUrl();

    /**
     * Gets the acronym of the digital library from which the resource originates.
     *
     * @return The digital library acronym.
     */
    public String getDLAcronym();

    /**
     * Gets the persistent identifier (PID) of the requested resource.
     *
     * @return The PID of the cached resource.
     */
    public String getPid();

    /**
     * Gets the timestamp when the request item was created in the cache.
     *
     * @return The creation timestamp of the cache entry.
     */
    public LocalDateTime getTimestamp();

    /**
     * Gets the MIME type of the cached data.
     *
     * @return The MIME type (e.g., "image/jpeg", "application/xml").
     */
    public String getMimeType();

    /**
     * Retrieves the cached data, which can be either text or binary.
     *
     * @return The stored data.
     */
    public T getData();


    /**
     * Gets the identification of the user associated with the request.
     *
     * @return The user identification string.
     */
    public String getUserIdentification();


    /**
     * Saves the request item to the database.
     *
     * @param connection The database connection used for saving the item.
     * @throws SQLException If an SQL error occurs during the save operation.
     */
    void save(Connection connection) throws SQLException;

    /**
     * Determines whether the cache entry has expired based on the given expiration period.
     *
     * @param days The number of days after which the cache entry is considered expired.
     * @return {@code true} if the item has expired, {@code false} otherwise.
     */
    public boolean isExpired(int days);

}
