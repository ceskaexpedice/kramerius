package cz.incad.kramerius.rights.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.aplikator.client.local.command.ListEntities;
import org.aplikator.server.ApplicationLoaderServlet;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Menu;

import cz.incad.kramerius.rights.server.views.UserView;

@SuppressWarnings("serial")
public class RightsLoaderServlet extends ApplicationLoaderServlet {

    private static final Logger LOG = Logger.getLogger(RightsLoaderServlet.class.getName());

    @Override
    public void init() throws ServletException {
        try {
            Structure struct = (Structure) Application.get();
            Menu uzivatele = new Menu("Uzivatele");
            UserView userView = new UserView();
            uzivatele.addView(userView);
            struct.addMenu(uzivatele);
            struct.setShowNavigation(false);
            struct.setDefaultAction(new ListEntities(userView.getId()).getPlace().getToken());
            LOG.info("Rightseditor Loader finished");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Rightseditor Loader error:", ex);
            throw new ServletException("Rightseditor Loader error: ", ex);
        }
    }

}
