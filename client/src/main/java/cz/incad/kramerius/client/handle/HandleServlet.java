package cz.incad.kramerius.client.handle;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.handle.DisectHandle;

public class HandleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String requestURL = req.getRequestURL().toString();
        String pid = DisectHandle.disectHandle(requestURL);
        pid = URLDecoder.decode(pid,"UTF-8");
        redirect(pid, req, resp);
    }

    void redirect(String pid,  HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map parameterMap = request.getParameterMap();
        String applicationCotext = ApplicationURL.applicationContextPath(request);
        String redirectUrl = "/" + applicationCotext + "/index.vm?page=doc#" + pid;
        response.sendRedirect(redirectUrl);
    }
}
