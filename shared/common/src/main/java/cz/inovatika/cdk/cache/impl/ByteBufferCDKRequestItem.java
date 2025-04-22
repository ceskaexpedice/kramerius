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
package cz.inovatika.cdk.cache.impl;

import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import cz.incad.kramerius.utils.database.JDBCUpdateTemplate;
import cz.inovatika.cdk.cache.CDKRequestItem;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a cached request item that stores binary data using {@link ByteBuffer}.
 * This class is responsible for persisting and updating binary-based CDK request items in the database.
 */
public class ByteBufferCDKRequestItem extends AbstractCDKRequestItem<ByteBuffer> {

    public static final Logger LOGGER = Logger.getLogger(ByteBufferCDKRequestItem.class.getName());

    private ByteBufferCDKRequestItem() {}

    /**
     * Saves the request item to the database. If an entry with the same ID exists, it updates it;
     * otherwise, it inserts a new entry.
     *
     * @param connection The database connection used for saving the item.
     * @throws SQLException If a database error occurs during the save operation.
     */
    @Override
    public void save(Connection connection) throws SQLException {
        List<CDKRequestItem> items = new JDBCQueryTemplate<CDKRequestItem>(connection, false) {
            @Override
            public boolean handleRow(ResultSet rs, List<CDKRequestItem> returnsList) throws SQLException {
                CDKRequestItem<?> cacheItemFromResultSet = CDKRequestItemFactory.createCacheItemFromResultSet(rs);
                returnsList.add(cacheItemFromResultSet);
                return true;
            }
        }.executeQuery(String.format("SELECT * FROM query_cache where  %s = ? ", ID_COLUMN),this.getId());


        if (items.isEmpty()) {
            JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);

            String query = String.format("insert into query_cache (%s, %s, %s, %s, %s, %s, %s, %s)" +
                            "values(?,?,?,?,?,?,?,?)",
                    ID_COLUMN,
                    URL_COLUMN,
                    SOURCE_LIBRARY_COLUMN,
                    BYTE_DATA_COLUMN,
                    MIME_TYPE_COLUMN,
                    PID_COLUMN,
                    CREATED_AT_COLUMN,
                    USER_IDENTIFICATION_COLUMN
            );
            LOGGER.log(Level.FINE, String.format("PostgreSQL query %s", query));

                    /*
                    id SERIAL PRIMARY KEY,           -- Unikátní ID záznamu
                    url TEXT NOT NULL,               -- Dotazovaná URL
                    source_library TEXT NOT NULL,     -- Zdrojová knihovna
                    text_data TEXT,                   -- Textová data (pro JSON/XML)
                    binary_data BYTEA,                 -- Binární data (pro obrázky atd.)
                    mime_type TEXT NOT NULL,           -- Mime typ (např. "application/json", "image/png")
                    pid INTEGER NOT NULL,              -- PID procesu, který záznam vytvořil
                    created_at TIMESTAMP DEFAULT NOW() -- Čas vytvoření záznamu
                    */
            template.executeUpdate(String.format(query),
                    this.getId(),
                    this.getUrl(),
                    this.getDLAcronym(),
                    this.getData().array(),

                    this.getMimeType(),
                    this.getPid(),
                    Timestamp.valueOf(this.getTimestamp()),
                    this.getUserIdentification() != null ? this.getUserIdentification() : COMMON_USER
            );

        } else {
            JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);

            String query = String.format("update query_cache set %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=? " +
                            " where %s =? ",
                    URL_COLUMN,
                    SOURCE_LIBRARY_COLUMN,
                    BYTE_DATA_COLUMN,
                    MIME_TYPE_COLUMN,
                    PID_COLUMN,
                    CREATED_AT_COLUMN,
                    USER_IDENTIFICATION_COLUMN,
                    ID_COLUMN
            );

            template.executeUpdate( query,
                    this.getUrl(),
                    this.getDLAcronym(),
                    this.getData().array(),
                    this.getMimeType(),
                    this.getPid(),
                    Timestamp.valueOf(this.getTimestamp()),
                    this.getUserIdentification() != null ? this.getUserIdentification() : new JDBCUpdateTemplate.NullObject(String.class),

                    this.getId()
            );
        }

    }


    public static class Builder {

        private final ByteBufferCDKRequestItem item;

        public Builder() {
            this.item = new ByteBufferCDKRequestItem();
        }

        public Builder data(ByteBuffer data) {
            item.setData(data);
            return this;
        }

        public Builder mimeType(String mimeType) {
            item.setMimeType(mimeType);
            return this;
        }

        public Builder url(String url) {
            item.setUrl(url);
            return this;
        }

        public Builder pid(String pid) {
            item.setPid(pid);
            return this;
        }

        public Builder dlAcronym(String dlAcronym) {
            item.setDlAcronym(dlAcronym);
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            item.setLocalDateTime(timestamp);
            return this;
        }

        public Builder userIdentification(String userIdentification) {
            item.setUserIdentification(userIdentification);
            return this;
        }

        public ByteBufferCDKRequestItem build() {
            return item;
        }
    }
}
