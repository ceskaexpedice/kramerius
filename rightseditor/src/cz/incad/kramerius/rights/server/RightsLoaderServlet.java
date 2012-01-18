package cz.incad.kramerius.rights.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.aplikator.server.ApplicationLoaderServlet;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.Menu;
import org.aplikator.server.descriptor.View;

import cz.incad.kramerius.rights.server.impl.PropertiesMailer;
import cz.incad.kramerius.rights.server.views.PublicUserView;
import cz.incad.kramerius.rights.server.views.RefenrenceToPersonalAdminView;
import cz.incad.kramerius.rights.server.views.UserView;

@SuppressWarnings("serial")
public class RightsLoaderServlet extends ApplicationLoaderServlet {

    private static final Logger LOG = Logger.getLogger(RightsLoaderServlet.class.getName());

    Structure struct;

    UserView userView;
    RefenrenceToPersonalAdminView referenceToAdmin;
    //GroupView groupView;
    View groupUserAssocView;


    PublicUserView publicUserArr;

    @Override
    public void init() throws ServletException {
        try {
            LOG.fine("ApplicationLoader started");
            // SERVER SIDE
            LOG.fine("ApplicationLoader 1");
            struct = (Structure) Application.get();
            LOG.fine("ApplicationLoader 2");

            GeneratePasswordExec generatePasswordForPrivate = new GeneratePasswordExec();

            referenceToAdmin = new RefenrenceToPersonalAdminView(struct);

//            groupView= new GroupView(struct, struct.group, referenceToAdmin);
            userView = new UserView(struct, struct.user, referenceToAdmin, new Function("VygenerovatHeslo", generatePasswordForPrivate));
            {
                generatePasswordForPrivate.setArrangement(userView);
                generatePasswordForPrivate.setMailer(new PropertiesMailer());
                userView.setMailer(new PropertiesMailer());

            }

            GeneratePasswordExec generatePasswordForPublic = new GeneratePasswordExec();
            publicUserArr = new PublicUserView(struct, struct.publicUser, referenceToAdmin, new Function("VygenerovatHeslo", generatePasswordForPublic));
            {
                generatePasswordForPublic.setArrangement(publicUserArr);
                generatePasswordForPrivate.setMailer(new PropertiesMailer());
                userView.setMailer(new PropertiesMailer());

            }
            LOG.fine("ApplicationLoader 3");
            // CLIENT SIDE MENU

            Menu uzivatele = new Menu("Uzivatele");
            uzivatele.addView(userView);
            Menu publicUzivatele = new Menu("Uzivatele");
            publicUzivatele.addView(publicUserArr);
            //uzivatele.addAction(new ActionDTO("Skupiny", new ListEntities("Skupiny", groupArr.getId())));

            /*
             * uzivatele.addAction(new
             * ActionDTO("Vazby (Uzivatele <-> Skupiny)", new ListEntities(
             * "Vazby (Uzivatele <-> Skupiny)", uzivatele,
             * groupUserAssocArr.getId()))); ServiceDTO prava = new
             * ServiceDTO("Prava"); prava.addAction(new ActionDTO("Prava", new
             * ListEntities( "Prava", prava, rightsArr.getId())));
             *
             * prava.addAction(new ActionDTO("Kriteria", new ListEntities(
             * "Kriteria", prava, rightsCriteriumArr.getId())));
             *
             * prava.addAction(new ActionDTO("Parametry kriteria", new
             * ListEntities( "Parametry kriteria", prava,
             * rightsCriteriumParamArr.getId())));
             */
            struct.addMenu(uzivatele);
            struct.addMenu(publicUzivatele);

            /*
             * ServiceDTO functions = new ServiceDTO("Funkce");
             * functions.addAction(new ActionDTO("Vygenerovat heslo", new
             * ExecuteFunction(functions, vygenerovatHeslo.getFunctionDTO())));
             * applicationDescriptor.addService(functions);
             */

            LOG.info("Rightseditor Loader finished");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Rightseditor Loader error:", ex);
            throw new ServletException("Rightseditor Loader error: ", ex);
        }
    }

}
