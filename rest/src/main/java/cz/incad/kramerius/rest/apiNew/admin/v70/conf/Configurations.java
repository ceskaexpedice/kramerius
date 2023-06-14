package cz.incad.kramerius.rest.apiNew.admin.v70.conf;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.incad.kramerius.utils.conf.KConfiguration;

@Path("/admin/v7.0/conf")
public class Configurations {
    
    //TODO: Specific to one process - move ? 
    @GET
    @Path("flagtolicense")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info() {
        
        List<String> models = KConfiguration.getInstance().getConfiguration().getList("processess.flag_to_license.models", Arrays.asList(
                "monograph",
                "periodicalvolume",
                "manuscript",
                "soundrecording",
                "convolute",
                "map",
                "sheetmusic",
                "graphic",
                "archive", 
                "convolute"
        )).stream().map(Objects::toString).collect(Collectors.toList());

        JSONObject confObject = new JSONObject();
        JSONArray retConf = new JSONArray();
        models.forEach(retConf::put);
        confObject.put("processess.flag_to_license.models", retConf);
        
        return Response.ok().entity(confObject.toString(2)).build();
    }

}
