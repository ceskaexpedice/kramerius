package cz.incad.kramerius.rights.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.aplikator.client.command.ListEntities;
import org.aplikator.client.descriptor.ActionDTO;
import org.aplikator.client.descriptor.ApplicationDTO;
import org.aplikator.client.descriptor.ServiceDTO;
import org.aplikator.server.ApplicationLoaderServlet;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.Function;

import cz.incad.kramerius.rights.server.arragements.GroupArrangement;
import cz.incad.kramerius.rights.server.arragements.RefenrenceToPersonalAdminArrangement;
import cz.incad.kramerius.rights.server.arragements.RightArrangement;
import cz.incad.kramerius.rights.server.arragements.RightsCriteriumArrangement;
import cz.incad.kramerius.rights.server.arragements.RightsCriteriumParamArrangement;
import cz.incad.kramerius.rights.server.arragements.UserArrangement;
import cz.incad.kramerius.rights.server.impl.PropertiesMailer;

@SuppressWarnings("serial")
public class RightsLoaderServlet extends ApplicationLoaderServlet {

    private static final Logger LOG = Logger.getLogger(RightsLoaderServlet.class.getName());

    Structure struct;

    UserArrangement userArr;
    RefenrenceToPersonalAdminArrangement referenceToAdmin;
    //GroupArrangement groupArr;
    Arrangement groupUserAssocArr;

    RightArrangement rightsArr;
    RightsCriteriumArrangement rightsCriteriumArr;
    RightsCriteriumParamArrangement rightsCriteriumParamArr;

    @Override
    public void init() throws ServletException {
        try {
            LOG.fine("ApplicationLoader started");
            // SERVER SIDE
            LOG.fine("ApplicationLoader 1");
            struct = (Structure) Application.get();
            LOG.fine("ApplicationLoader 2");

            VygenerovatHeslo execVygenerovatHeslo = new VygenerovatHeslo();

            referenceToAdmin = new RefenrenceToPersonalAdminArrangement(struct);

//            groupArr = new GroupArrangement(struct, struct.group, referenceToAdmin);
            userArr = new UserArrangement(struct, struct.user, referenceToAdmin, new Function("VygenerovatHeslo", execVygenerovatHeslo));
            {
                execVygenerovatHeslo.setUserArr(userArr);
                execVygenerovatHeslo.setMailer(new PropertiesMailer());
                userArr.setMailer(new PropertiesMailer());

            }

            rightsArr = new RightArrangement(struct.rights, struct);
            rightsCriteriumParamArr = new RightsCriteriumParamArrangement(struct.criteriumParam, struct);
            rightsCriteriumArr = new RightsCriteriumArrangement(struct.rightCriterium, struct, rightsCriteriumParamArr);

            LOG.fine("ApplicationLoader 3");
            // CLIENT SIDE MENU
            ApplicationDTO applicationDescriptor = ApplicationDTO.get();

            ServiceDTO uzivatele = new ServiceDTO("Uzivatele");
            uzivatele.addAction(new ActionDTO("Uzivatele", new ListEntities("Uzivatele", uzivatele, userArr.getId())));
            //uzivatele.addAction(new ActionDTO("Skupiny", new ListEntities("Skupiny", uzivatele, groupArr.getId())));

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
            applicationDescriptor.addService(uzivatele);

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
