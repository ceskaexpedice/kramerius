package cz.incad.kramerius.client;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.incad.kramerius.client.kapi.auth.AdminUser;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.client.kapi.auth.User;
import cz.incad.kramerius.client.utils.ApiCallsHelp;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class FeedbackServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String k4host = KConfiguration.getInstance().getConfiguration().getString("k4.host");
        
        String pid = req.getParameter("pid");
        String from = req.getParameter("from");
        String content = req.getParameter("content");
        /*
        //http://localhost:8080/search/feedback?from=pavel.stastny@gmail.com&pid=[http://localhost:8080/search/handle/uuid:cd2b2ad0-62d4-11dd-ac0e-000d606f5dc6]%20&content=testik
        CallUserController controll = (CallUserController) req.getSession().getAttribute(CallUserController.KEY);
        AdminUser adm = controll.getAdminCaller();
        String uname = adm.getUserName();
        String pwd = adm.getPassword();
        */
        
        ApiCallsHelp.postParams(k4host+"/feedback", null, null, "pid="+pid,"from="+from,"content="+content);
    }
}
