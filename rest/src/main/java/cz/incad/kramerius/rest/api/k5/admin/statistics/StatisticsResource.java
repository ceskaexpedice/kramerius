/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.api.k5.admin.statistics;

import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.annotations.DefaultParameterValue;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.database.Offset;

public class StatisticsResource {

	@Inject
	StatisticsAccessLog statisticsAccessLog;

	@Inject
	IsActionAllowed actionAllowed;

	@Inject
	Provider<User> userProvider;
 	
	public Response getReportPage(
			@QueryParam("reportname") String rip, 
			@QueryParam("action") @DefaultValue("READ") String raction, 
			@QueryParam("filter") String filterVal, 
			@QueryParam("offset") String filterOffset,
            @QueryParam("resultSize") String filterResultSize
			) {
		if (permit(userProvider.get())) {
			try {
				StatisticReport report = statisticsAccessLog.getReportById(rip);
				Offset offset = null;
				if (StringUtils.isAnyString(filterOffset)) {
					offset = new Offset(filterOffset, filterResultSize);
				}

				List<Map<String, Object>> repPage = report.getReportPage(ReportedAction.valueOf(raction), offset, filterVal);
				JSONArray jsonArr = new JSONArray();
				for (Map<String, Object> map : repPage) {
					JSONObject json = createJSON(map);
					jsonArr.add(json);
				}
				return Response.ok().entity(jsonArr.toString()).build();
			} catch (StatisticsReportException e) {
				e.printStackTrace();
			}
			
		} else {
    		throw new ActionNotAllowed("not allowed");
    	}
		
		return null;
	}

    private JSONObject createJSON(Map<String, Object> map) {
    	JSONObject json = new JSONObject();
    	
    	return null;
	}

	boolean permit(User user) {
    	if (user != null)
    		return  this.actionAllowed.isActionAllowed(user,SecuredActions.SHOW_STATISTICS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH);
    	else 
    		return false;
    }

}
