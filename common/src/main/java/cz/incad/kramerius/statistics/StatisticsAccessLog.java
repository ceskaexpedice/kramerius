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

import javax.servlet.http.HttpServletRequest;

/**
 * Accesslog for statistics usages
 * @author pavels
 */
public interface StatisticsAccessLog {

    /**
     * Report one access 
     * @param pid accessing pid 
     * @param streamName accessing stream
     * @throws IOException IO error has been occured
     */
    public void reportAccess(String pid, String streamName) throws IOException;
    
    /**
     * Report one access 
     * @param pid
     * @param streamName
     * @param actionName
     * @throws IOException
     */
    public void reportAccess(String pid, String streamName, String actionName) throws IOException;
    
    /**
     * Returns true if access to  given pid and stream should be reported
     * @param pid accessing pid
     * @param streamName accessing stream
     * @return
     */
    public boolean isReportingAccess(String pid, String streamName);

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
    
}
