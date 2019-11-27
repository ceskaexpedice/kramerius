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
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.filters.DateFilter;
import cz.incad.kramerius.statistics.filters.IPAddressFilter;
import cz.incad.kramerius.statistics.filters.ModelFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFilter;
import cz.incad.kramerius.statistics.filters.StatisticsFiltersContainer;
import cz.incad.kramerius.statistics.filters.VisibilityFilter;
import cz.incad.kramerius.statistics.filters.VisibilityFilter.VisbilityType;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.database.Offset;

@Path("/v5.0/admin/statistics")
public class StatisticsResource {

    @Inject
    StatisticsAccessLog statisticsAccessLog;

    @Inject
    IsActionAllowed actionAllowed;

    @Inject
    Provider<User> userProvider;

    @GET
    @Path("{report}")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response getReportPage(@PathParam("report") String rip,
            @QueryParam("action") String raction,
            @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo,
            @QueryParam("model") String model,
            @QueryParam("visibility") String visibility,
            @QueryParam("offset") String filterOffset,
            @QueryParam("resultSize") String filterResultSize) {
        
        //TODO: syncrhonization
        if (permit(userProvider.get())) {
            try {
                DateFilter dateFilter = new DateFilter();
                dateFilter.setFromDate(dateFrom);
                dateFilter.setToDate(dateTo);
                
                ModelFilter modelFilter = new ModelFilter();
                modelFilter.setModel(model);
                
                VisibilityFilter visFilter = new VisibilityFilter();
                visFilter.setSelected(VisbilityType.valueOf(visibility));
                
                IPAddressFilter ipAddr = new IPAddressFilter();
                
                StatisticReport report = statisticsAccessLog.getReportById(rip);
                Offset offset = new Offset("0", "25");
                if (StringUtils.isAnyString(filterOffset)
                        && StringUtils.isAnyString(filterResultSize)) {
                    offset = new Offset(filterOffset, filterResultSize);
                }
                
                report.prepareViews(raction != null ? ReportedAction.valueOf(raction) : null, new StatisticsFiltersContainer(new StatisticsFilter[] {dateFilter, modelFilter,visFilter, ipAddr}));
                List<Map<String, Object>> repPage = report.getReportPage(
                        raction != null ? ReportedAction.valueOf(raction)
                                : null,new StatisticsFiltersContainer(new StatisticsFilter[] {dateFilter, modelFilter, ipAddr}), offset);

                JSONArray jsonArr = new JSONArray();
                for (Map<String, Object> map : repPage) {
                    JSONObject json = createJSON(map);
                    jsonArr.put(json);
                }
                return Response.ok().entity(jsonArr.toString()).build();
            } catch (StatisticsReportException e) {
                throw new GenericApplicationException(e.getMessage());
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    private JSONObject createJSON(Map<String, Object> map) throws JSONException {
        JSONObject json = new JSONObject();
        Set<String> keys = map.keySet();
        for (String k : keys) {
            json.put(k, map.get(k));
        }
        return json;
    }

    boolean permit(User user) {
        if (user != null)
            return this.actionAllowed.isActionAllowed(user,
                    SecuredActions.SHOW_STATISTICS.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH);
        else
            return false;
    }

}
