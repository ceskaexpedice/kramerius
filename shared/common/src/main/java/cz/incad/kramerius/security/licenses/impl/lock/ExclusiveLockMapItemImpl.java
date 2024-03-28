/*
 * Copyright (C) Mar 10, 2024 Pavel Stastny
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
package cz.incad.kramerius.security.licenses.impl.lock;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.json.JSONObject;

import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMapItem;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMapItemRefreshItervalMissedException;
import cz.incad.kramerius.security.licenses.lock.ExclusiveMapException;
import cz.incad.kramerius.security.licenses.lock.ExclusiveMapItemAfterMaxTimeExcepion;

public class ExclusiveLockMapItemImpl implements ExclusiveLockMapItem {
    
    //private String pid;
    
    private String tokenId;
    private int refreshInterval;
    private Instant createdTime;
    private Instant refreshedTime;
    private Instant maxTime;
    private String userId;

        
    public ExclusiveLockMapItemImpl(String tokenId, int refreshInterval, Instant createdTime, Instant refreshedTime,
            Instant maxTime/*, String pid*/, String userName) {
        super();
        this.tokenId = tokenId;
        this.refreshInterval = refreshInterval;
        this.createdTime = createdTime;
        this.refreshedTime = refreshedTime;
        this.maxTime = maxTime;
        this.userId = userName;
    }


    



    @Override
    public String getUserId() {
        return this.userId;
    }



    @Override
    public String getTokenId() {
        return this.tokenId;
    }
    
    
    @Override
    public Instant getCreatedTime() {
        return this.createdTime;
    }

    @Override
    public Instant getRefreshedTime() {
        return this.refreshedTime;
    }

    @Override
    public Instant getMaxTime() {
        return this.maxTime;
    }
    
    @Override
    public int getRefreshInterval() {
        return this.refreshInterval;
    }


 

    
    @Override
    public void refresh(Instant refresh) {
        this.refreshedTime = refresh;
        
    }

    @Override
    public boolean isValid(Instant instant) {
        Instant thresholdInstant = this.refreshedTime.plusSeconds(this.refreshInterval);
        if (instant.isBefore(thresholdInstant) && thresholdInstant.isBefore(this.getMaxTime())) {
            return true;
        } else if (instant.isAfter(thresholdInstant)) {
            return false;
        } else if (thresholdInstant.isAfter(this.getMaxTime())) {
            return false;
        }
        return false;
    }
    
    

    @Override
    public JSONObject toJSONObject() {
        JSONObject retval = new JSONObject();
        retval.put("username", this.userId);
        retval.put("tokenId", this.tokenId);
        retval.put("refreshInterval", this.refreshInterval);
        retval.put("createdTime", DateTimeFormatter.ISO_INSTANT.format(this.createdTime));
        retval.put("refreshedTime", DateTimeFormatter.ISO_INSTANT.format(this.refreshedTime));
        retval.put("maxTime", DateTimeFormatter.ISO_INSTANT.format(this.maxTime));
        return retval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdTime, maxTime, refreshInterval, refreshedTime, tokenId);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExclusiveLockMapItemImpl other = (ExclusiveLockMapItemImpl) obj;
        return Objects.equals(createdTime, other.createdTime) && Objects.equals(maxTime, other.maxTime)
                && refreshInterval == other.refreshInterval && Objects.equals(refreshedTime, other.refreshedTime)
                && Objects.equals(tokenId, other.tokenId);
    }

}
