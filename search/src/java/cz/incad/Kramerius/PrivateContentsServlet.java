package cz.incad.Kramerius;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;

public class PrivateContentsServlet extends GuiceServlet {

    @Inject
    @Named("securedFedoraAccess")
    transient FedoraAccess fedoraAccess;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String parameter = req.getParameter("uuids");
        if (parameter != null) {
            String [] uuids = parameter.split("/");
            Set<String> uuidsSet = new HashSet<String>(Arrays.asList(uuids));
            Map<String, Boolean> data = new HashMap<String, Boolean>();
            for (String uuid : uuidsSet) {
                data.put(uuid, !fedoraAccess.isContentAccessible(uuid));
            }
            StringTemplate template = new StringTemplate("({ $data.keys:{uuid | '$uuid$':$data.(uuid)$ };separator=\",\"$ })");
            template.setAttribute("data", data);
            resp.getWriter().println(template.toString());
        }
    }
}
