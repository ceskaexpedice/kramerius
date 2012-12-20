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
 * Implementations of this interface can receive infomrations about 
 * @author pavels
 */
public interface StatisticsAccessLog {

    public void reportAccess(String pid, String streamName) throws IOException;
    
    public boolean isReportingAccess(String pid, String streamName);

    public StatisticReport[] getAllReports();
    
    public StatisticReport getReportById(String reportId);
}
