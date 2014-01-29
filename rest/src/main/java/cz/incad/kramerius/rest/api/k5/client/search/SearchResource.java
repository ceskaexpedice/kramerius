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
package cz.incad.kramerius.rest.api.k5.client.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.bouncycastle.jce.provider.JCEBlockCipher.DES;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.JSONDecorator;
import cz.incad.kramerius.rest.api.k5.client.JSONDecoratorsAggregate;
import cz.incad.kramerius.rest.api.k5.client.item.ItemResource;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.net.URLEncoder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Path("/v5.0/search")
public class SearchResource {

	public static Logger LOGGER = Logger.getLogger(SearchResource.class.getName());
	
    @Inject
    SolrAccess solrAccess;

    @Inject
    JSONDecoratorsAggregate jsonDecoratorAggregates;
    
    @GET
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response select(@Context UriInfo uriInfo) {
        try {
        	
        	MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            StringBuilder builder = new StringBuilder();
            Set<String> keys = queryParameters.keySet();
            for (String k : keys) {
                for (String v : queryParameters.get(k)) {
                	builder.append(k + "=" + URLEncoder.encode(v, "UTF-8"));
                    builder.append("&");
                }
            }
            InputStream istream = this.solrAccess.request(builder.toString(), "json");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(istream, bos);
            String rawString = new String(bos.toByteArray(),"UTF-8");

            String uri = UriBuilder.fromResource(SearchResource.class).path("").build().toString();
            JSONObject jsonObject = changeResult(rawString, uri);
            
            return Response.ok().entity(jsonObject.toString()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    
	public JSONObject changeResult(String rawString, String context)
			throws UnsupportedEncodingException {
		
		List<JSONDecorator> decs = this.jsonDecoratorAggregates.getDecorators();
		
		JSONObject resultJSONObject = JSONObject.fromObject(rawString);
		JSONObject responsObject = resultJSONObject.getJSONObject("response");
		if (responsObject.containsKey("docs")) {
		    JSONArray jsonArray = responsObject.getJSONArray("docs");
		    for (Object obj : jsonArray) {
				JSONObject docJSON = (JSONObject) obj;
				// check master pid 
				changeMasterPid(docJSON);
				// fiter protected fields
				filterFields(docJSON);
				
				//decorators
				decorators(context, decs,  docJSON);
				
				// replace pids
				replacePids(docJSON);
		    }
		}
		return resultJSONObject;
	}


	public void decorators(String context, List<JSONDecorator> decs,
			JSONObject docJSON) {
		//decorators
		Map<String, Object> runtimeCtx = new HashMap<String, Object>();
		for (JSONDecorator d : decs) { d.before(runtimeCtx); }
		for (JSONDecorator jsonDec : decs) {
			if (jsonDec.apply(docJSON, context)) {
				jsonDec.decorate(docJSON, runtimeCtx);
			}
		}
		for (JSONDecorator d : decs) { d.after(); }
	}

	public static void replacePids(JSONObject jsonObj) {
		// repair results
		String[] apiReplace = KConfiguration.getInstance().getAPIPIDReplace();
		for (String k : apiReplace) {
			if (k.equals("PID")) continue; //already replaced
				if (jsonObj.containsKey(k)) {
					Object object = jsonObj.get(k);
					if (object instanceof String) {
						String s = jsonObj.getString(k);
						if (s.indexOf("/@") > 0) {
							s.replace("/@", "@");
							jsonObj.put(k, s);
						}
					} else if (object instanceof JSONArray) {
						JSONArray jsonArr = (JSONArray) object;
						JSONArray newJSONArray = new JSONArray();
						int size = jsonArr.size();
						for (int i = 0; i < size; i++) {
							Object sObj = jsonArr.get(i);
							if (sObj instanceof String) {
								String s = (String) sObj;
								s= s.replace("/@", "@");
								newJSONArray.add(s);
								
							} else {
								LOGGER.warning("skipping object type '"+sObj.getClass().getName()+"'");
							}
						}
						jsonObj.put(k, newJSONArray);
					} else {
						LOGGER.warning("skipping object type '"+object.getClass().getName()+"'");
					}
				}
		}
	}

	public static void filterFields(JSONObject jsonObj) {
		// filter
		String[] filters = KConfiguration.getInstance().getAPISolrFilter();
		for (String filterKey : filters) {
			if (jsonObj.containsKey(filterKey)) {
				jsonObj.remove(filterKey);
			}
		}
	}

	public static void changeMasterPid(JSONObject jsonObj) {
		if (jsonObj.containsKey("PID")) {
			// pid contains '/' char
			String pid = jsonObj.getString("PID");
			if (pid.contains("/")) {
				pid = pid.replace("/", "");
				jsonObj.put("PID", pid);
			}
			
		}
	}

    @GET
    @Path("terms")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response terms(@Context UriInfo uriInfo) {
        try {
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            StringBuilder builder = new StringBuilder();
            Set<String> keys = queryParameters.keySet();
            for (String k : keys) {
                for (String v : queryParameters.get(k)) {
                    builder.append(k + "=" + URLEncoder.encode(v, "UTF-8"));
                    builder.append("&");
                }
            }
            InputStream istream = this.solrAccess.terms(builder.toString(), "json");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(istream, bos);

            String rawString = new String(bos.toByteArray(),"UTF-8");
            String uri = UriBuilder.fromResource(SearchResource.class).path("terms").build().toString();
            JSONObject jsonObject = changeResult(rawString, uri);

            return Response.ok().entity(jsonObject.toString()).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
