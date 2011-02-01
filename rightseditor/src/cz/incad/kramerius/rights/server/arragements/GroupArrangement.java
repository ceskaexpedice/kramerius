package cz.incad.kramerius.rights.server.arragements;


import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.HorizontalPanel;
import org.aplikator.server.descriptor.QueryGenerator;
import org.aplikator.server.descriptor.RefButton;
import org.aplikator.server.descriptor.RepeatedForm;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.VerticalPanel;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.GroupEntity;
import cz.incad.kramerius.rights.server.arragements.UserArrangement.RefGroupArrangement;
import cz.incad.kramerius.rights.server.arragements.UserArrangement.UserGroupsArrangement;

public class GroupArrangement extends Arrangement {

	Structure struct;
	Structure.GroupEntity groupEntity;
	RefenrenceToPersonalAdminArrangement reference;
	
	public GroupArrangement(Structure structure, GroupEntity entity, RefenrenceToPersonalAdminArrangement reference) {
		super(entity);
		this.struct = structure;
		this.groupEntity = entity;
		this.reference = reference;
		setReadableName(struct.group.getName());
		addProperty(struct.group.GNAME);
		setSortProperty(struct.group.GNAME);
		setForm(createGroupForm());
		this.trigger = new GroupTriggers(this.struct);
	}
	
	
	private Form createGroupForm() {
		Form form = new Form();
		form.setLayout(new VerticalPanel()
					.addChild(new TextField(struct.group.GNAME))
					.addChild(new TextArea(struct.group.DESCRIPTION).setWidth("100%"))
					.addChild(
							new RefButton(struct.group.PERSONAL_ADMIN,
									this.reference,
									new HorizontalPanel().addChild(new TextField(
											struct.group.PERSONAL_ADMIN
													.relate(struct.group.GNAME)))))
					.addChild(new RepeatedForm(struct.group.USER_ASSOCIATIONS, new GroupUsersArrangement()))

		);
		return form;
	}
	

	
	public class GroupUsersArrangement extends Arrangement{
        
        public GroupUsersArrangement(){
            super(struct.groupUserAssoction);
            setReadableName(struct.user.getReadableName());
            
            //addProperty(structure.groupUserAssoction.GROUP);
            addProperty(struct.groupUserAssoction.USERS.relate(struct.user.LOGINNAME));
            setForm(createForm());
        }
        
        Form createForm() {
            Form form = new Form();
            form.setLayout(new VerticalPanel()
                               .addChild(
                                    new RefButton(struct.groupUserAssoction.USERS,new RefUserArrangement(),
                                        new VerticalPanel()
                                          .addChild(new TextField(struct.groupUserAssoction.USERS.relate(struct.user.LOGINNAME)))
                                          .addChild(new TextField(struct.groupUserAssoction.USERS.relate(struct.user.NAME)))
                                          .addChild(new TextField(struct.groupUserAssoction.USERS.relate(struct.user.SURNAME)))
                                    )
                               )
                     );
            return form;
        }
        
    }
	
	public class RefUserArrangement extends Arrangement{
        public RefUserArrangement() {
            super(struct.user);
            setReadableName(struct.user.getName());
            addProperty(struct.user.LOGINNAME);
            setSortProperty(struct.user.LOGINNAME);
            setForm(createUserForm());
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
                                    reference,
                                    new HorizontalPanel().addChild(new TextField(
                                            struct.user.PERSONAL_ADMIN
                                                    .relate(struct.group.GNAME)))))
                    

            );
            return form;
        }
        
    }
}
