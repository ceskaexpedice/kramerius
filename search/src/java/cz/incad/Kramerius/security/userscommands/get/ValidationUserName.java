package cz.incad.Kramerius.security.userscommands.get;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.Kramerius.security.userscommands.ServletUsersCommand;
import cz.incad.kramerius.security.User;

public class ValidationUserName extends ServletUsersCommand {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PublicUserActivation.class.getName());
    
    public static final String KEY = "uname";
    
    
    @Override
    public void doCommand() throws IOException {
        try {
            JSONObject jsonOrg = new JSONObject();
            // other validation
            HttpServletRequest request = this.requestProvider.get();
            String uNameParam = request.getParameter(KEY);
            if (uNameParam != null) {
                User user = this.userManager.findUserByLoginName(uNameParam.trim());
                if (user != null) {
                    jsonOrg.put("valid", false);
                } else {
                    jsonOrg.put("valid", true);
                }
            } else {
                jsonOrg.put("valid", false);
            }
            this.responseProvider.get().setContentType("application/json");
            this.responseProvider.get().getWriter().write(jsonOrg.toString());
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

}
