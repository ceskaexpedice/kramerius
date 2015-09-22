package cz.incad.kramerius.client;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.captcha.Captcha;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import biz.sourcecode.base64Coder.Base64Coder;
import cz.incad.kramerius.client.kapi.auth.AdminUser;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.client.utils.ApiCallsHelp;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class RegistrationUsersServlet extends HttpServlet {

    public static final String PUBLIC_ROLE = "public_users";

    public static final Logger LOGGER = Logger
            .getLogger(RegistrationUsersServlet.class.getName());

    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        GetActions aAction = GetActions.valueOf(action);
        try {
            String authUrl = KConfiguration.getInstance().getConfiguration().getString("api.point") + "/admin";
            aAction.perform(authUrl, req, resp);
        } catch (JSONException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        PostActions aAction = PostActions.valueOf(action);
        try {
            String authUrl = KConfiguration.getInstance().getConfiguration().getString("api.point") + "/admin";
            aAction.perform(authUrl, req, resp);
        } catch (JSONException  e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }


    public static enum GetActions {
        validateName {

            @Override
            public void perform(String remoteAddr, HttpServletRequest req, HttpServletResponse resp)
                    throws UnsupportedEncodingException, IOException,
                    JSONException {
                String lname = req.getParameter("name");

                try {
                    AuthenticationServlet.createCaller(req, null, null,null);
                    CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                            .getSession(true).getAttribute(CallUserController.KEY);
                    AdminUser adminCaller = callUserController.getAdminCaller();
                    String json = ApiCallsHelp.getJSON(remoteAddr + "/users?lname="+lname, adminCaller.getUserName(), adminCaller.getPassword());
                    JSONArray jsonArr = new JSONArray(json);

                    JSONObject returnJSON = new JSONObject();

                    returnJSON.put("valid",jsonArr.length() == 0);

                    resp.setContentType("application/json");
                    resp.getWriter().write(returnJSON.toString());

                } catch (ConfigurationException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        };        
        public abstract void perform(String remoteAddr, HttpServletRequest req,
                HttpServletResponse resp) throws UnsupportedEncodingException,
                IOException, JSONException;
    }    
    public static enum PostActions {
        create {
            @Override
            public void perform(String remoteAddr, HttpServletRequest req,
                    HttpServletResponse resp)
                    throws UnsupportedEncodingException, IOException,
                    JSONException {


                String encodedData = req.getParameter("encodedData");
                if (encodedData != null) {
                    try {
                        AuthenticationServlet.createCaller(req, null, null,null);

                        CallUserController callUserController = (cz.incad.kramerius.client.kapi.auth.CallUserController) req
                                .getSession(true).getAttribute(CallUserController.KEY);

                        AdminUser adminCaller = callUserController.getAdminCaller();

                        byte[] decoded = Base64Coder.decode(encodedData);
                        JSONObject jsonObject = new JSONObject(new String(
                                decoded, "UTF-8"));
                        String captchaString = jsonObject.getString("captcha");
                        Captcha expected = (Captcha) req.getSession()
                                .getAttribute(Captcha.NAME);
                        if (expected.getAnswer().equals(captchaString)) {

                            String lname = jsonObject.getString("lname");
                            String json = ApiCallsHelp.getJSON(remoteAddr + "/users?lname="+lname, adminCaller.getUserName(), adminCaller.getPassword());
                            JSONArray jsonArr = new JSONArray(json);
                            if (jsonArr.length() > 0) {
                                throw new BadRequestException("user with name '"+lname+"' already exists");
                            } else {
                                String nm = jsonObject.getString("username");

                                StringTokenizer tokenizer = new StringTokenizer(nm," ");

                                String firstName = tokenizer.hasMoreTokens() ? tokenizer
                                        .nextToken() : nm;
                                String surName = tokenizer.hasMoreTokens() ? tokenizer
                                        .nextToken() : "";

                                String pswd = jsonObject.getString("password");

                                JSONObject creatingUser = new JSONObject();
                                creatingUser.put("lname", lname);
                                creatingUser.put("firstname", firstName);
                                creatingUser.put("surname", surName);
                                creatingUser.put("password", pswd);


                                String jsonRepre = ApiCallsHelp.postJSON(remoteAddr + "/users", creatingUser,
                                        adminCaller.getUserName(),
                                        adminCaller.getPassword());

                                // login 
                                AuthenticationServlet.createCaller(req, lname, pswd,jsonRepre);
                                
                                JSONObject retvalJSON = new JSONObject(jsonRepre);
                                String firstname = retvalJSON.getString("firstname");
                                String surname  = retvalJSON.getString("surname");
                                callUserController.getClientCaller().updateInformation(firstname, surname);

                                resp.setContentType("application/json");
                                resp.getWriter().write(creatingUser.toString());
                            }
                            

                        } else {
                            resp.setStatus(HttpServletResponse.SC_CONFLICT);
                            resp.setContentType("application/json");
                            JSONObject data = new JSONObject();
                            data.put("error", "bad_captcha");
                            resp.getWriter().write(data.toString());
                        }
                    } catch (ConfigurationException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                }
            }
        };
        public abstract void perform(String remoteAddr, HttpServletRequest req,
                HttpServletResponse resp) throws UnsupportedEncodingException,
                IOException, JSONException;
    }

}
