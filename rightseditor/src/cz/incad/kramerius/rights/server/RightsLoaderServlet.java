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
import cz.incad.kramerius.rights.server.arragements.PublicUserArrangement;
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


	PublicUserArrangement publicUserArr;

    @Override
    public void init() throws ServletException {
        try {
            LOG.fine("ApplicationLoader started");
            // SERVER SIDE
            LOG.fine("ApplicationLoader 1");
            struct = (Structure) Application.get();
            LOG.fine("ApplicationLoader 2");

            GeneratePasswordExec generatePasswordForPrivate = new GeneratePasswordExec();

            referenceToAdmin = new RefenrenceToPersonalAdminArrangement(struct);

			userArr = new UserArrangement(struct, struct.user, referenceToAdmin,  new Function("VygenerovatHeslo", generatePasswordForPrivate));
            {
                generatePasswordForPrivate.setArrangement(userArr);
                generatePasswordForPrivate.setMailer(new PropertiesMailer());
                userArr.setMailer(new PropertiesMailer());
            }
            
            GeneratePasswordExec generatePasswordForPublic = new GeneratePasswordExec();
            publicUserArr = new PublicUserArrangement(struct, struct.publicUser, referenceToAdmin, new Function("VygenerovatHeslo", generatePasswordForPublic));
            {
            	generatePasswordForPublic.setArrangement(publicUserArr);
                generatePasswordForPrivate.setMailer(new PropertiesMailer());
                userArr.setMailer(new PropertiesMailer());
            	
            }


            LOG.fine("ApplicationLoader 3");
            // CLIENT SIDE MENU
            ApplicationDTO applicationDescriptor = ApplicationDTO.get();
            //TODO: I18N
            ServiceDTO uzivatele = new ServiceDTO("Interni uzivatele");
            uzivatele.addAction(new ActionDTO("Uzivatele", new ListEntities("Uzivatele", uzivatele, userArr.getId())));
            //TODO: I18N
            ServiceDTO publicUzivatele = new ServiceDTO("Verejni Uzivatele");
            publicUzivatele.addAction(new ActionDTO("Uzivatele", new ListEntities("IUzivatele", publicUzivatele, publicUserArr.getId())));
    
            applicationDescriptor.addService(uzivatele);
            applicationDescriptor.addService(publicUzivatele);

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
