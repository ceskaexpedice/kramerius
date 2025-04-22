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

import cz.inovatika.cdk.cache.CDKRequestItem;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class AbstractCDKRequestItem<T> implements CDKRequestItem<T> {

    private T data;
    private String mimeType;
    private String url;
    private String pid;
    private String dlAcronym;
    private String userIdentification;
    private LocalDateTime localDateTime;

    public AbstractCDKRequestItem() {}

    @Override
    public String getId() {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String identifier = String.format("%s_%s_%s", this.dlAcronym, this.url, this.pid);
            byte[] digest = md5.digest(identifier.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LocalDateTime getTimestamp() {
        return this.localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    @Override
    public String getDLAcronym() {
        return this.dlAcronym;
    }

    public void setDlAcronym(String dlAcronym) {
        this.dlAcronym = dlAcronym;
    }

    public String getDlAcronym() {
        return dlAcronym;
    }

    @Override
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }


    @Override
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String getUserIdentification() {
        return userIdentification;
    }

    public void setUserIdentification(String userIdentification) {
        this.userIdentification = userIdentification;
    }

    public boolean isExpired(int days) {
        if (this.localDateTime == null) {
            return false;
        }
        return this.localDateTime.isBefore(LocalDateTime.now().minus(days, ChronoUnit.DAYS));
    }

    @Override
    public String toString() {
        return "AbstractCDKRequestItem{" +
                "data=" + data +
                ", mimeType='" + mimeType + '\'' +
                ", url='" + url + '\'' +
                ", pid='" + pid + '\'' +
                ", dlAcronym='" + dlAcronym + '\'' +
                ", userIdentification='" + userIdentification + '\'' +
                ", localDateTime=" + localDateTime +
                '}';
    }
}
