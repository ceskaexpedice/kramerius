package cz.incad.kramerius.rest.apiNew.admin.v10.reharvest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;


import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;

import static cz.incad.kramerius.rest.apiNew.admin.v10.reharvest.ReharvestItem.*;

@Path("/admin/v7.0/reharvest")
public class ReharvestResource {

    public Logger LOGGER = Logger.getLogger(ReharvestResource.class.getName());

    @Inject
    private ReharvestManager reharvestManager;

    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;

    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHarvests() {
        JSONArray jsonArray = new JSONArray();
        this.reharvestManager.getItems().forEach(ri -> {
            jsonArray.put(ri.toJSON());
        });
        return Response.ok(jsonArray.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHarvest(@PathParam("id") String id) {
        ReharvestItem item = this.reharvestManager.getItemById(id);
        if (item != null) {
            return Response.ok(item.toJSON().toString()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        ReharvestItem item = this.reharvestManager.getItemById(id);
        if (item != null) {
            this.reharvestManager.deregister(id);
            return Response.ok(item.toJSON().toString()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(String json) {
        try {
            // switch reharvest vs new harvest
            JSONObject jsonObj = new JSONObject(json);
            if (!jsonObj.has(ID_KEYWORD)) {
                jsonObj.put(ID_KEYWORD, UUID.randomUUID().toString());
            }
            if (!jsonObj.has(PID_KEYWORD)) {
                throw new BadRequestException(" Request must contain pid ");
            }
            
            if (!jsonObj.has(OWN_PID_PATH) || !jsonObj.has(ROOT_PID)) { 
                Document solrDataByPid = this.solrAccess.getSolrDataByPid(jsonObj.getString(PID_KEYWORD));
                Element rootPid = XMLUtils.findElement(solrDataByPid.getDocumentElement(),  new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("root.pid");
                        }
                        return false;
                    }
                });
                Element ownPidPath = XMLUtils.findElement(solrDataByPid.getDocumentElement(),  new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("own_pid_path");
                        }
                        return false;
                    }
                });

                Element cdkCollection = XMLUtils.findElement(solrDataByPid.getDocumentElement(),  new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("arr")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("cdk.collection");
                        }
                        return false;
                    }
                });

                
                if (rootPid != null && !jsonObj.has(ROOT_PID)) {
                    jsonObj.put(ROOT_PID, rootPid.getTextContent());
                }
                if (ownPidPath != null && !jsonObj.has(OWN_PID_PATH)) {
                    jsonObj.put(OWN_PID_PATH, ownPidPath.getTextContent());
                }
                
                if (cdkCollection != null && !jsonObj.has(LIBRARIES_KEYWORD)) {
                    List<String> collections = XMLUtils.getElements(cdkCollection).stream().map(Element::getTextContent).collect(Collectors.toList());
                    JSONArray jsonArr = new JSONArray();
                    collections.forEach(jsonArr::put);
                    jsonObj.put(LIBRARIES_KEYWORD, jsonArr);
                }
            }

            // nasel to v indexu (pro polozky, ktere nejsou v indexu je potreba novy typ harvestu
            ReharvestItem item = ReharvestItem.fromJSON(jsonObj);
            ReharvestItem alreadyRegistredItem = this.reharvestManager.getOpenItemByPid(item.getPid());
            if (alreadyRegistredItem != null) {
                return Response.status(Response.Status.CONFLICT).build();
            } else {
                switch(item.getTypeOfReharvest()) {

                case delete_pid:
                case only_pid:
                    if (item != null && StringUtils.isAnyString(item.getPid())) {
                        item.setTimestamp(Instant.now());
                        this.reharvestManager.register(item);
                        return Response.ok(item.toJSON().toString()).build();
                    } else {
                        JSONObject errorObject = new JSONObject();
                        errorObject.put("error", "No pid");
                        return Response.status(Response.Status.BAD_REQUEST).entity(errorObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
                    }
                case delete_tree:    
                case root:
                case children:
                    if (item != null && StringUtils.isAnyString(item.getRootPid()) && StringUtils.isAnyString(item.getOwnPidPath())) {
                        item.setTimestamp(Instant.now());
                        this.reharvestManager.register(item);
                        return Response.ok(item.toJSON().toString()).build();
                    } else {
                        JSONObject errorObject = new JSONObject();
                        errorObject.put("error", "No root pid or own_pid_path");
                        return Response.status(Response.Status.BAD_REQUEST).entity(errorObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
                    }
                
                case new_root:
                    if (item != null && StringUtils.isAnyString(item.getRootPid())) {
                        item.setTimestamp(Instant.now());
                        this.reharvestManager.register(item);
                        return Response.ok(item.toJSON().toString()).build();
                    } else {
                        JSONObject errorObject = new JSONObject();
                        errorObject.put("error", "No root pid");
                        return Response.status(Response.Status.BAD_REQUEST).entity(errorObject.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
                    }
                default:
                    throw new IllegalStateException(String.format("Uknown type of reharvest %s", item.getTypeOfReharvest()));
                }
            }
        } catch (JSONException | ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch(AlreadyRegistedPidsException e) {
            throw new BadRequestException(" Request contains already registered pids "+e.getPids());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
  

  
  @GET
  @Path("top")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTopHarvest(@QueryParam("state")String state) {
      ReharvestItem topItem = this.reharvestManager.getTopItem(state);
      if (topItem != null) {
          return Response.ok(topItem.toJSON().toString()).build();
      } else {
          return Response.status(Response.Status.NOT_FOUND).build();
      }
  }


  @GET
  @Path("open")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOpenHarvest(@QueryParam("pid")String pid) {
      LOGGER.info("Returning open harvest");
      if (StringUtils.isAnyString(pid)) {
          ReharvestItem topItem = this.reharvestManager.getOpenItemByPid(pid);
          if (topItem != null) {
              return Response.ok(topItem.toJSON().toString()).build();
          } else {
              LOGGER.info("Open harvest not found");
              return Response.status(Response.Status.NOT_FOUND).build();
          }
      } else {
          LOGGER.info("Open harvest Bad request");
          return Response.status(Response.Status.BAD_REQUEST).build();
      }
  }

  @PUT
  @Path("{id}/state")
  @Produces(MediaType.APPLICATION_JSON)
  public Response changeState(@PathParam("id") String id, @QueryParam("state") String state) {
      try {
        ReharvestItem itemById = reharvestManager.getItemById(id);
          if (itemById != null) {
              itemById.setState(state);
              this.reharvestManager.update(itemById);
              return Response.ok(itemById.toJSON().toString()).build();
          } else {
              return Response.status(Response.Status.NOT_FOUND).build();
          }
    } catch (UnsupportedEncodingException | JSONException | ParseException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PUT
  @Path("{id}/pod")
  @Produces(MediaType.APPLICATION_JSON)
  public Response changePod(@PathParam("id") String id, @QueryParam("pod") String pod) {
      try {
        ReharvestItem itemById = reharvestManager.getItemById(id);
          if (itemById != null) {
              itemById.setPodname(pod);
              this.reharvestManager.update(itemById);
              return Response.ok(itemById.toJSON().toString()).build();
          } else {
              return Response.status(Response.Status.NOT_FOUND).build();
          }
    } catch (UnsupportedEncodingException | JSONException | ParseException e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }
  
}
