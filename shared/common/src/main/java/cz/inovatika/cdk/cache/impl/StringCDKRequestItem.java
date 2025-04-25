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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a cached request item that stores text-based data.
 * This class extends {@link AbstractCDKRequestItem} and provides methods
 * for saving cache entries to a database.
 */
public class StringCDKRequestItem extends AbstractCDKRequestItem<String> {

    public static final Logger LOGGER = Logger.getLogger(StringCDKRequestItem.class.getName());

    /**
     * Saves the current cache item to the database.
     * If an entry with the same ID already exists, it updates the existing entry.
     *
     * @param connection The database connection used for executing SQL operations.
     * @throws SQLException If a database error occurs while inserting or updating the cache item.
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
        }.executeQuery("SELECT * FROM query_cache where  id = ? ",this.getId());


        if (items.isEmpty()) {
            JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);



            String query = String.format("insert into query_cache (%s, %s, %s, %s, %s, %s, %s, %s) " +
                            " values(?,?,?,?,?,?,?,?)",
                    ID_COLUMN,
                    URL_COLUMN,
                    SOURCE_LIBRARY_COLUMN,
                    TEXT_DATA_COLUMN,
                    MIME_TYPE_COLUMN,
                    PID_COLUMN,
                    USER_IDENTIFICATION_COLUMN,
                    CREATED_AT_COLUMN
            );
            LOGGER.log(Level.FINE, String.format("PostgreSQL query %s", query));

            template.executeUpdate(query,
                    this.getId(), // 1
                    this.getUrl(), //2
                    this.getDLAcronym() != null ? this.getDLAcronym() :  new JDBCUpdateTemplate.NullObject(String.class), //3
                    this.getData(), //4
                    this.getMimeType() != null ? this.getMimeType() : new JDBCUpdateTemplate.NullObject(String.class), //5
                    this.getPid() != null ? this.getPid() : new JDBCUpdateTemplate.NullObject(String.class), //6
                    this.getUserIdentification() != null ? this.getUserIdentification() : CDKRequestItem.COMMON_USER,
                    Timestamp.valueOf(this.getTimestamp()) //8
                );


        } else {
            JDBCUpdateTemplate template = new JDBCUpdateTemplate(connection, true);



            String query = String.format("update query_cache set %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=? " +
                            " where %s =? ",
                    URL_COLUMN,
                    SOURCE_LIBRARY_COLUMN,
                    TEXT_DATA_COLUMN,
                    MIME_TYPE_COLUMN,
                    PID_COLUMN,
                    CREATED_AT_COLUMN,
                    USER_IDENTIFICATION_COLUMN,
                    ID_COLUMN
            );

            LOGGER.log(Level.FINE, String.format("PostgreSQL query %s", query));



            template.executeUpdate(query,

                    this.getUrl(),
                    this.getDLAcronym() != null ? this.getDLAcronym() : new JDBCUpdateTemplate.NullObject(String.class),
                    this.getData(),
                    this.getMimeType() != null ? this.getMimeType() : new JDBCUpdateTemplate.NullObject(String.class),
                    this.getPid() != null ? this.getPid() : new JDBCUpdateTemplate.NullObject(String.class), //6
                    Timestamp.valueOf(this.getTimestamp()),
                    this.getUserIdentification() != null ? this.getUserIdentification() : CDKRequestItem.COMMON_USER,

                    this.getId()
                    );
        }
    }

    public static class Builder {
        private final StringCDKRequestItem item;

        public Builder() {
            this.item = new StringCDKRequestItem();
        }

        public Builder data(String data) {
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

        public StringCDKRequestItem build() {
            return item;
        }
    }




}
