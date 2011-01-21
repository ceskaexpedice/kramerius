package cz.incad.kramerius.rights.server.arragements;

import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.HorizontalPanel;
import org.aplikator.server.descriptor.QueryGenerator;
import org.aplikator.server.descriptor.RefButton;
import org.aplikator.server.descriptor.RepeatedForm;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.VerticalPanel;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.UserEntity;

public class UserArrangement extends Arrangement {

	Structure struct;
	Structure.UserEntity userEntity;
	RefenrenceToPersonalAdminArrangement referenceToAdmin;
//	GroupArrangement groupArrangement;

	public UserArrangement(Structure struct, UserEntity entity,
			RefenrenceToPersonalAdminArrangement reference) {
		super(entity);
		this.struct = struct;
		this.userEntity = entity;
		this.referenceToAdmin = reference;
	//	this.groupArrangement = groupArrangement;

		setReadableName(struct.user.getName());

		addProperty(struct.user.NAME).addProperty(struct.user.SURNAME)
				.addProperty(struct.user.LOGINNAME);

		queryGenerator = new QueryGenerator.Empty();

		form = createUserForm();

	}

	private Form createUserForm() {
		Form form = new Form();
		form.setLayout(new VerticalPanel()
				.addChild(
						new VerticalPanel().addChild(
								new TextField(struct.user.NAME)).addChild(
								new TextField(struct.user.SURNAME)))

				.addChild(
						new VerticalPanel().addChild(
								new TextField(struct.user.LOGINNAME)).addChild(
								new TextField(struct.user.PASSWORD)))


				.addChild(
						new RefButton(struct.user.PERSONAL_ADMIN,
								this.referenceToAdmin,
								new HorizontalPanel().addChild(new TextField(
										struct.user.PERSONAL_ADMIN
												.relate(struct.group.GNAME)))))

		);
		return form;
	}

}
