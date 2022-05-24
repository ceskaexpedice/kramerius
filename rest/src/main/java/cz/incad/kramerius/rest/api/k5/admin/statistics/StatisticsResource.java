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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.google.inject.name.Named;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.*;

import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.processes.LRResource;
import cz.incad.kramerius.statistics.filters.*;
import cz.incad.kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.terracotta.statistics.Statistic;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsReportException;
import cz.incad.kramerius.statistics.filters.VisibilityFilter.VisbilityType;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.database.Offset;



@Path("/admin/v7.0/statistics")
public class StatisticsResource {

    public static final Semaphore STATISTIC_SEMAPHORE = new Semaphore(1);


    @Inject
    @Named("database")
    StatisticsAccessLog statisticsAccessLog;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    @Inject
    Set<StatisticsReportFormatter> reportFormatters;

    @Inject
    LRProcessManager lrProcessManager;

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @DELETE
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response cleanData( @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo) {
    	// mel by byt nekdo jiny nez ten kdo resi prohlizeni statistik
    	if (permit()) {
    		try {
    			if (StringUtils.isAnyString(dateFrom) && StringUtils.isAnyString(dateTo)) {
    				try {
						Date dateFromd = StatisticReport.DATE_FORMAT.parse(dateFrom);
						Date dateTod = StatisticReport.DATE_FORMAT.parse(dateTo);
						
						int cleanData = this.statisticsAccessLog.cleanData(dateFromd, dateTod);
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("deleted",cleanData);
						return Response.ok().entity(jsonObject.toString()).build();
					} catch (ParseException e) {
	        	    	throw new BadRequestException("parsing exception dateFrom, dateTo");
					}
    			} else {
        	    	throw new BadRequestException("expecting parameters dateFrom, dateTo");
    				
    			}
    		} catch (IOException e) {
                throw new GenericApplicationException(e.getMessage());
    		}
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response reports() {
        if (permit()) {
        	
        	StatisticReport[] allReports = this.statisticsAccessLog.getAllReports();
        	List<String> ids = Arrays.asList(allReports).stream().map(StatisticReport::getReportId).collect(Collectors.toList());
        	
        	JSONObject jsonObject = new JSONObject();
        	JSONArray jsonArray = new JSONArray();
        	ids.stream().forEach(jsonArray::put);
        	jsonObject.put("reports", jsonArray);

        	return Response.ok().entity(jsonObject.toString()).build();
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }

    
    
    
    @GET
    @Path("{report}")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response getReportPage(@PathParam("report") String rip,
            @QueryParam("action") String raction,
            @QueryParam("dateFrom") String dateFrom,
            @QueryParam("dateTo") String dateTo,
            @QueryParam("model") String model,
            @QueryParam("models") String models,

            @QueryParam("license") String license,
            @DefaultValue("ALL") @QueryParam("visibility") String visibility,

            @QueryParam("offset") String filterOffset,
            @QueryParam("pids") String pids,
            @QueryParam("resultSize") String filterResultSize) {
        
        if (permit()) {
            try {
                StatisticsFiltersContainer container = container(dateFrom, dateTo, model, visibility,
						pids, license, models);
                StatisticReport report = statisticsAccessLog.getReportById(rip);
                if (report != null) {
                    Offset offset = new Offset("0", "10");
                    if (StringUtils.isAnyString(filterOffset)
                            && StringUtils.isAnyString(filterResultSize)) {
                        offset = new Offset(filterOffset, filterResultSize);
                    }
                    
                    List<String> results = report.verifyFilters(raction != null ? ReportedAction.valueOf(raction) : null, container);
                    if (results.isEmpty()) {
                        List<Map<String, Object>> repPage = report.getReportPage(
                                raction != null ? ReportedAction.valueOf(raction)
                                        : null,container, offset);
                        if (repPage.size() > 1) {
                            JSONArray jsonArr = new JSONArray();
                            for (Map<String, Object> map : repPage) {
                                JSONObject json = new JSONObject(map);
                                jsonArr.put(json);
                            }
                            return Response.ok().entity(jsonArr.toString()).build();
                        } else if (repPage.size() == 1){
                            JSONObject json = new JSONObject(repPage.get(0));
                            return Response.ok().entity(json.toString()).build();
                        } else {
                            return Response.ok().entity(new JSONArray().toString()).build();
                        }
                    } else {
                    	String body = results.stream().collect(Collectors.joining("\n"));
                        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
                    }
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                
            } catch (StatisticsReportException e) {
                throw new GenericApplicationException(e.getMessage());
            } catch (JSONException e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }



	private StatisticsFiltersContainer container(String dateFrom, String dateTo, String model, String visibility,
			 String pids, String license, String models) {
		DateFilter dateFilter = new DateFilter();
		dateFilter.setFromDate(dateFrom);
		dateFilter.setToDate(dateTo);
		
		ModelFilter modelFilter = new ModelFilter();
		modelFilter.setModel(model);
		
		VisibilityFilter visFilter = new VisibilityFilter();
		visFilter.setSelected(VisbilityType.valueOf(visibility));
		
		PidsFilter pidsFilter = new PidsFilter();
		pidsFilter.setPids(pids);
		LicenseFilter licenseFilter = new LicenseFilter(license);

		MultimodelFilter multiModel = new MultimodelFilter();
		if (models != null) {
			multiModel.setModels(Arrays.asList(models.split(",")));
		}
		
		StatisticsFiltersContainer container = new StatisticsFiltersContainer(new StatisticsFilter[] {dateFilter, modelFilter, /*ipAddr,uniqueIPFilter,*/ visFilter, licenseFilter,multiModel});

		return container;
	}



    @GET
    @Path("{report}/export")
    @Consumes({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response exportJSON(@PathParam("report") String rip,
                                  @QueryParam("action") String action,
                                  @QueryParam("dateFrom") String dateFrom,
                                  @QueryParam("dateTo") String dateTo,
                                  @QueryParam("model") String model,
                                  @DefaultValue("ALL") @QueryParam("visibility") String visibilityValue,
                                  @QueryParam("annualyear") String annual,
                                  @QueryParam("pids") String pids,
                                 
                                  @DefaultValue("export.data") @QueryParam("file") String file) {

        return export(rip, action, dateFrom, dateTo, model, visibilityValue,  annual,
				pids, file,  MediaType.APPLICATION_JSON);
    }


    @GET
    @Path("{report}/export")
    @Consumes({ "text/csv" })
    @Produces({ "text/csv" })
    public Response exportCSV(@PathParam("report") String rip,
                                  @QueryParam("action") String action,
                                  @QueryParam("dateFrom") String dateFrom,
                                  @QueryParam("dateTo") String dateTo,
                                  @QueryParam("model") String model,
                                  @DefaultValue("ALL") @QueryParam("visibility") String visibilityValue,
                                  @QueryParam("annualyear") String annual,
                                  @QueryParam("pids") String pids,
                                 
                                  @DefaultValue("export.data") @QueryParam("file") String file) {

        return export(rip, action, dateFrom, dateTo, model, visibilityValue, annual,
				pids, file,  "text/csv");
    }
    
    //public Response delete
    
    
    @GET
    @Path("{report}/export")
    @Consumes({ MediaType.APPLICATION_XML + ";charset=utf-8"})
    @Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
    public Response exportXML(@PathParam("report") String rip,
                                  @QueryParam("action") String action,
                                  @QueryParam("dateFrom") String dateFrom,
                                  @QueryParam("dateTo") String dateTo,
                                  @QueryParam("model") String model,
                                  @DefaultValue("ALL")  @QueryParam("visibility") String visibilityValue,
                                  @QueryParam("annualyear") String annual,
                                  @QueryParam("pids") String pids,
                                  @DefaultValue("export.data") @QueryParam("file") String file) {

        return export(rip, action, dateFrom, dateTo, model, visibilityValue,  annual,
				pids, file,  MediaType.APPLICATION_XML);
    }


	private Response export(String rip, String action, String dateFrom, String dateTo, String model,
			String visibilityValue,  String annual, String pids,
			String file, String format) {
		AnnualYearFilter annualYearFilter = new AnnualYearFilter();
        annualYearFilter.setAnnualYear(annual);

        DateFilter dateFilter = new DateFilter();
        dateFilter.setFromDate(dateFrom != null && (!dateFrom.trim().equals("")) ? dateFrom : null);
        dateFilter.setToDate(dateTo != null && (!dateTo.trim().equals("")) ? dateTo : null);

        ModelFilter modelFilter = new ModelFilter();
        modelFilter.setModel(model);


        PidsFilter pidsFilter = new PidsFilter();
        pidsFilter.setPids(pids);


        if (action != null && action.equals("null")) {
            action = null;
        }

        if (dateFrom == null) {
            dateFrom = "";
        }

        if (dateTo == null) {
            dateTo = "";
        }

        MultimodelFilter multimodelFilter = new MultimodelFilter();

        
        VisibilityFilter visFilter = new VisibilityFilter();
		visFilter.setSelected(VisbilityType.valueOf(visibilityValue));

        if (permit()) {
            if (rip != null && (!rip.equals(""))) {
            	StatisticReport report = this.statisticsAccessLog.getReportById(rip);
            	Optional<StatisticsReportFormatter> opts = reportFormatters.stream().filter(formatter -> {
                    return formatter.getMimeType().contains(format) && formatter.getReportId().equals(report.getReportId());
                }).findAny();
            	
                if (opts.isPresent()) {

                    List<String> results = report.verifyFilters(null, new StatisticsFiltersContainer(new StatisticsFilter []{dateFilter,modelFilter,visFilter, multimodelFilter, annualYearFilter,  pidsFilter}));
                    if (results.isEmpty()) {
                    	StatisticsReportFormatter selectedFormatter =  opts.get();
                        String info = null;
                        info = ((annual == null) ? "" : annual + ", ") + ((model == null) ? "" : model + ", ") + ((dateFrom.equals("")) ? "" : "od: " + dateFrom + ", ") + ((dateTo.equals("")) ? "" : "do: " + dateTo + ", ")
                                + "akce: " + ((action == null) ? "ALL" : action) + ", viditelnosti: " + visibilityValue + ", "
                                //+ ((ipAddr.getIpAddress().equals("")) ? "" : "zakázané IP adresy: " + ipAddr.getIpAddress() + ", ")
                                //+ "unikátní IP adresy: " + uniqueIpAddresses + ".";
                                + "";

                        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        try {
                            STATISTIC_SEMAPHORE.acquire();
                            selectedFormatter.addInfo(bos, info);
                            selectedFormatter.beforeProcess(bos);

                            // Must be synchronized - only one report at the time
                            report.prepareViews(action != null ? ReportedAction.valueOf(action) : null,new StatisticsFiltersContainer(new StatisticsFilter []{dateFilter,modelFilter,  multimodelFilter, annualYearFilter, pidsFilter}));
                            report.processAccessLog(action != null ? ReportedAction.valueOf(action) : null, selectedFormatter,new StatisticsFiltersContainer(new StatisticsFilter []{dateFilter,modelFilter,visFilter, multimodelFilter, annualYearFilter,  pidsFilter}));
                            selectedFormatter.afterProcess(bos);

                            return Response.ok(new StreamingOutput() {
                                @Override
                                public void write(OutputStream output) throws IOException, WebApplicationException {
                                    IOUtils.copyStreams(new ByteArrayInputStream(bos.toByteArray()), output);
                                }
                            }).header("Content-disposition",  "attachment; filename="+file).build();
                        } catch (IOException e) {
                            throw new GenericApplicationException(e.getMessage());
                        } catch (StatisticsReportException e) {
                            throw new GenericApplicationException(e.getMessage());
                        } catch (InterruptedException e) {
                            throw new GenericApplicationException(e.getMessage());
                        } finally {
                            STATISTIC_SEMAPHORE.release();
                        }
                    } else {
                    	String body = results.stream().collect(Collectors.joining("\n"));
                        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
                    }
                } else {
                    throw new BadRequestException("For selected report and selected mimtype I cannot find the formatter");
                }
            } else {
                throw new BadRequestException("No report selected");
            }
        } else {
            throw new ActionNotAllowed("not allowed");
       }
	}

	
	
	

    boolean permit() {
        User user = null;
        String authToken = this.requestProvider.get().getHeader(LRResource.AUTH_TOKEN_HEADER_KEY);
        if (authToken != null && !lrProcessManager.isAuthTokenClosed(authToken)) {
            String sessionKey = lrProcessManager.getSessionKey(authToken);
            if (sessionKey != null) {
                user =  this.loggedUsersSingleton.getUser(sessionKey);
            } else {
                user = this.userProvider.get();
            }
        } else {
            user = this.userProvider.get();
        }
        return permit(user);
    }


    boolean permit(User user) {
        if (user != null)
            return this.rightsResolver.isActionAllowed(user,
                    SecuredActions.SHOW_STATISTICS.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }
    
    
}
