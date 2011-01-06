package cz.incad.kramerius.rights.server;

import javax.servlet.ServletException;

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

@SuppressWarnings("serial")
public class RightsLoaderServlet extends ApplicationLoaderServlet {

	Structure struct;
	Arrangement userArr;
	Arrangement groupArr;
	Arrangement groupUserAssocArr;
	
	Arrangement groupUserAssoc_UserArr;

	
	RightArrangement rightsArr;
	RightsCriteriumArrangement rightsCriteriumArr;
	RightsCriteriumParamArrangement rightsCriteriumParamArr;

	@Override
	public void init() throws ServletException {
		try {
			System.out.println("ApplicationLoader started");
			// SERVER SIDE
			System.out.println("ApplicationLoader 1");
			struct = (Structure) Application.get();
			System.out.println("ApplicationLoader 2");

			userArr = new Arrangement(struct.user) {{
					setReadableName(struct.user.getName());

					addProperty(struct.user.NAME).addProperty(
							struct.user.SURNAME).addProperty(
							struct.user.LOGINNAME);

					queryGenerator = new QueryGenerator.Empty();

					form = createUserForm();
			}};
			
			groupArr = new Arrangement(struct.group) {{
					setReadableName(struct.group.getName());

					addProperty(struct.group.GNAME);

					queryGenerator = new QueryGenerator.Empty();

					form = createGroupForm();

			}};

			groupUserAssoc_UserArr = new Arrangement(struct.groupUserAssoction) {{
					addProperty(struct.groupUserAssoction.USERS);
					addProperty(struct.groupUserAssoction.GROUP);
					form = _createForm();
				}

				Form _createForm() {
					Form form = new Form();
					form.setLayout(
							new VerticalPanel()
								.addChild(new RefButton(struct.groupUserAssoction.USERS, userArr,
										new HorizontalPanel()
			                    		.addChild(new TextField(struct.groupUserAssoction.USERS.relate(struct.user.NAME)))
			                    		.addChild(new TextField(struct.groupUserAssoction.USERS.relate(struct.user.SURNAME)))
//			                    		.addChild(new TextField(struct.groupUserAssoction.USERS.relate(struct.user.LOGINNAME)))
//			                    		.addChild(new TextField(struct.groupUserAssoction.USERS.relate(struct.user.PASSWORD)))
									))
									.addChild(new RefButton(struct.groupUserAssoction.GROUP, groupArr,
										new HorizontalPanel()
											.addChild(new TextField(struct.groupUserAssoction.GROUP.relate(struct.group.GNAME)))
									)));
					return form;
				}
			};
			
			groupUserAssocArr = new Arrangement(struct.groupUserAssoction) {{
					setReadableName(struct.groupUserAssoction.getName());

					addProperty(struct.groupUserAssoction.USERS);
					addProperty(struct.groupUserAssoction.GROUP);

					queryGenerator = new QueryGenerator.Empty();

					form = createAssocForm();
				}
			};
			
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

			uzivatele.addAction(new ActionDTO("Vazby (Uzivatele <-> Skupiny)", new ListEntities(
					"Vazby (Uzivatele <-> Skupiny)", uzivatele, groupUserAssocArr.getId())));

			
			ServiceDTO prava = new ServiceDTO("Prava");

			prava.addAction(new ActionDTO("Prava", new ListEntities(
					"Prava", prava, rightsArr.getId())));

			prava.addAction(new ActionDTO("Kriteria", new ListEntities(
					"Kriteria", prava, rightsCriteriumArr.getId())));

			prava.addAction(new ActionDTO("Parametry kriteria", new ListEntities(
					"Parametry kriteria", prava, rightsCriteriumParamArr.getId())));

			applicationDescriptor.addService(uzivatele);
			applicationDescriptor.addService(prava);
			
			

			System.out.println("ApplicationLoader finished");
		} catch (Exception ex) {
			System.out.println("ApplicationLoader error:" + ex);
			throw new ServletException("ApplicationLoader error: ", ex);
		}
	}

	private Form createUserForm() {
		Form form = new Form();
		form.setLayout(new VerticalPanel()

				.addChild(new HorizontalPanel()
								.addChild(new TextField(struct.user.NAME))
								.addChild(new TextField(struct.user.SURNAME))
				)
				
				.addChild(new HorizontalPanel()
					.addChild(new TextField(struct.user.LOGINNAME))
					.addChild(new TextField(struct.user.PASSWORD))
				)
				);
		return form;
	}
	
	private Form createRightCriteriumParamForm() {
		Form form = new Form();
		TextArea textArea = new TextArea(struct.criteriumParam.VALS);
		textArea.setWidth("100%");
		form.setLayout(new VerticalPanel().addChild(
				new HorizontalPanel()
					.addChild(textArea)
		));
		return form;
	}

	
	
	
	private Form createGroupForm() {
		Form form = new Form();
		form.setLayout(new VerticalPanel().addChild(
				new HorizontalPanel()
					.addChild(new TextField(struct.group.GNAME))
		));
		return form;
	}
	
	private Form createAssocForm() {
		Form form = new Form();
		form.setLayout(
				new VerticalPanel()

					.addChild(new RefButton(struct.groupUserAssoction.USERS, userArr,
							new HorizontalPanel()
								.addChild(new TextField(struct.groupUserAssoction.USERS.relate(struct.user.NAME)))
						))

//					.addChild(new RefButton(struct.groupUserAssoction.GROUP, groupArr,
//							new HorizontalPanel()
//								.addChild(new TextField(struct.groupUserAssoction.GROUP.relate(struct.group.GNAME)))
//						))
						
					);
		return form;
	}
}
