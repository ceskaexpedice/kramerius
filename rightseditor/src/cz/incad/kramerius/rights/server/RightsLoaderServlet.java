package cz.incad.kramerius.rights.server;

import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.aplikator.client.command.ExecuteFunction;
import org.aplikator.client.command.ListEntities;
import org.aplikator.client.descriptor.ActionDTO;
import org.aplikator.client.descriptor.ApplicationDTO;
import org.aplikator.client.descriptor.ServiceDTO;
import org.aplikator.server.ApplicationLoaderServlet;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.CheckBox;
import org.aplikator.server.descriptor.ComboBox;
import org.aplikator.server.descriptor.DateField;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.HorizontalPanel;
import org.aplikator.server.descriptor.QueryGenerator;
import org.aplikator.server.descriptor.RefButton;
import org.aplikator.server.descriptor.RepeatedForm;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.VerticalPanel;

import cz.incad.kramerius.rights.server.arragements.GroupArrangement;
import cz.incad.kramerius.rights.server.arragements.RightArrangement;
import cz.incad.kramerius.rights.server.arragements.RightsCriteriumArrangement;
import cz.incad.kramerius.rights.server.arragements.RightsCriteriumParamArrangement;
import cz.incad.kramerius.rights.server.arragements.UserArrangement;
import cz.incad.kramerius.rights.server.arragements.UserGroupAssoc;
import cz.incad.kramerius.rights.server.arragements.RefenrenceToPersonalAdminArrangement;
import cz.incad.kramerius.security.IsActionAllowedBase;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.jaas.K4UserPrincipal;

@SuppressWarnings("serial")
public class RightsLoaderServlet extends ApplicationLoaderServlet {

	Structure struct;

	UserArrangement userArr;
	RefenrenceToPersonalAdminArrangement referenceToAdmin;
	GroupArrangement groupArr;
	Arrangement groupUserAssocArr;

	
	RightArrangement rightsArr;
	RightsCriteriumArrangement rightsCriteriumArr;
	RightsCriteriumParamArrangement rightsCriteriumParamArr;

	private Function vygenerovatHeslo;

	private IsActionAllowedBase isActionAllowedBase;
	
	@Override
	public void init() throws ServletException {
		try {
			System.out.println("ApplicationLoader started");
			// SERVER SIDE
			System.out.println("ApplicationLoader 1");
			struct = (Structure) Application.get();
			System.out.println("ApplicationLoader 2");

            vygenerovatHeslo = new Function("VygenerovatHeslo",new VygenerovatHeslo());
			
			referenceToAdmin = new RefenrenceToPersonalAdminArrangement(struct, struct.group);
			
			groupArr = new GroupArrangement(struct, struct.group, referenceToAdmin);
			userArr = new UserArrangement(struct, struct.user, referenceToAdmin, vygenerovatHeslo);
			
			rightsArr = new RightArrangement(struct.rights, struct);
			rightsCriteriumParamArr = new RightsCriteriumParamArrangement(struct.criteriumParam, struct);
			rightsCriteriumArr = new RightsCriteriumArrangement(struct.rightCriterium, struct, rightsCriteriumParamArr);

	
			System.out.println("ApplicationLoader 3");
			// CLIENT SIDE MENU
			ApplicationDTO applicationDescriptor = ApplicationDTO.get();
			
			ServiceDTO uzivatele = new ServiceDTO("Uzivatele");
			uzivatele.addAction(new ActionDTO("Uzivatele", new ListEntities(
					"Uzivatele", uzivatele, userArr.getId())));
			uzivatele.addAction(new ActionDTO("Skupiny", new ListEntities(
					"Skupiny", uzivatele, groupArr.getId())));

			/*uzivatele.addAction(new ActionDTO("Vazby (Uzivatele <-> Skupiny)", new ListEntities(
					"Vazby (Uzivatele <-> Skupiny)", uzivatele, groupUserAssocArr.getId())));
			*/
			ServiceDTO prava = new ServiceDTO("Prava");
			prava.addAction(new ActionDTO("Prava", new ListEntities(
					"Prava", prava, rightsArr.getId())));

			prava.addAction(new ActionDTO("Kriteria", new ListEntities(
					"Kriteria", prava, rightsCriteriumArr.getId())));

			prava.addAction(new ActionDTO("Parametry kriteria", new ListEntities(
					"Parametry kriteria", prava, rightsCriteriumParamArr.getId())));

			applicationDescriptor.addService(uzivatele);
			applicationDescriptor.addService(prava);
			
           /* ServiceDTO functions = new ServiceDTO("Funkce");
            functions.addAction(new ActionDTO("Vygenerovat heslo", new ExecuteFunction(functions, vygenerovatHeslo.getFunctionDTO())));
            applicationDescriptor.addService(functions);*/

			

			System.out.println("ApplicationLoader finished");
		} catch (Exception ex) {
			System.out.println("ApplicationLoader error:" + ex);
			throw new ServletException("ApplicationLoader error: ", ex);
		}
	}

	
	

	public User getCurrentLoggedUser(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            K4UserPrincipal k4principal = (K4UserPrincipal) principal;
            User user = k4principal.getUser();
            return user;
        } else return null;		
	}
}
