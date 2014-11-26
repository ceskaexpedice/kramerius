package cz.incad.kramerius.client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.client.utils.ApiCallsHelp;
import cz.incad.kramerius.client.utils.RedirectHelp;

public class AudioConfigurationServlet extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(AudioConfigurationServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String pid = req.getParameter("pid");
            if (pid != null) {
                String add = RedirectHelp.redirectApplication(req);


                JSONObject dataJSON = new JSONObject();
                dataJSON.put("oga", add+"search/audioProxy/"+pid+"/OGG");
                dataJSON.put("wav", add+"search/audioProxy/"+pid+"/WAV");
                dataJSON.put("mp3", add+"search/audioProxy/"+pid+"/MP3");
                String res = ApiCallsHelp.getJSON(add+"search/audioTracks?action=getTracks&pid_path="+pid);
                JSONObject infoJSON = new JSONObject(res);
                
                JSONObject value = new JSONObject();
                value.put("data", dataJSON);
                value.put("info", infoJSON);
                
                resp.setContentType("application/json");
                resp.getWriter().write(value.toString());
                
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            

        } catch (ConfigurationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}
