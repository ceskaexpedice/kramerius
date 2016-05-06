package cz.incad.kramerius.client.shib;

import static cz.incad.kramerius.client.AuthenticationServlet.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ShibbolethCallerFilter implements Filter {

    public static final Logger LOGGER = Logger.getLogger(ShibbolethCallerFilter.class.getName());
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) httpReq.getSession(true).getAttribute(CallUserController.KEY);
        if (callUserController ==null || callUserController.getClientCaller() == null) {
            
            Object userName = httpReq.getSession().getAttribute(UserUtils.USER_NAME_PARAM);
            Object userPass = httpReq.getSession().getAttribute(UserUtils.PSWD_PARAM);
            String authUrl = KConfiguration.getInstance().getConfiguration().getString("api.point")+"/user";
            
            if (userName != null && userPass != null) {
                try {
                    String returned = get(authUrl, userName.toString(), userPass.toString());
                    CallUserController caller = createCaller(httpReq, userName.toString(), userPass.toString(), returned);
                    JSONObject jsonObject = new JSONObject(returned);
                    String firstname = jsonObject.getString("firstname");
                    String surname  = jsonObject.getString("surname");
                    caller.getClientCaller().updateInformation(firstname, surname);
                } catch (JSONException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                } catch (ConfigurationException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
