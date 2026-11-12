/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius.statistics;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Accesslog for statistics usages
 * @author pavels
 */
public interface StatisticsAccessLog {

    /**
     * Report one access 
     * @param pid
     * @param streamName
     * @param actionName
     * @throws IOException
     */
    public void reportAccess(String pid, String streamName, String actionName) throws IOException;

    /**
     * Report one access with additional fields stored in the log record as they are
     * (e.g. stream_mime, image_kind). Implementations that do not support extra fields ignore them.
     * @param pid
     * @param streamName
     * @param actionName
     * @param extraFields field name to value, may be null or empty
     * @throws IOException
     */
    public default void reportAccess(String pid, String streamName, String actionName, Map<String, String> extraFields) throws IOException {
        reportAccess(pid, streamName, actionName);
    }
    
    /**
     * Process all log
     * @param sup
     */
    public void processAccessLog(ReportedAction reportedAction, StatisticsAccessLogSupport sup);
    
    /**
     * Returns all predefined reports
     * @return
     */
    public StatisticReport[] getAllReports();

    
    /**
     * Find report by given id
     * @param reportId
     * @return
     */
    public StatisticReport getReportById(String reportId);

    
    /**
     * TODO: Presunout jinam
     * Cleaning statistics tables 
     * @param dateFrom From date
     * @param dateTo To date
     */
    public int cleanData(Date dateFrom, Date dateTo) throws IOException;

    /**
     * TODO: Presunout jinam
     * @throws IOException
     */
    public void refresh() throws IOException;
}
