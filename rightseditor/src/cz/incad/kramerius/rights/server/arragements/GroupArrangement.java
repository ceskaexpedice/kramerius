package cz.incad.kramerius.rights.server.arragements;


import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.HorizontalPanel;
import org.aplikator.server.descriptor.QueryGenerator;
import org.aplikator.server.descriptor.RefButton;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.VerticalPanel;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.GroupEntity;

public class GroupArrangement extends Arrangement {

	Structure struct;
	Structure.GroupEntity groupEntity;

	
	public GroupArrangement(Structure structure, GroupEntity entity) {
		super(entity);
		this.struct = structure;
		this.groupEntity = entity;
		setReadableName(struct.group.getName());
		addProperty(struct.group.GNAME);

		queryGenerator = new QueryGenerator.Empty();
		form = createGroupForm();
	}
	
	
	private Form createGroupForm() {
		Form form = new Form();
		form.setLayout(new VerticalPanel().addChild(
				new HorizontalPanel()
					.addChild(new TextField(struct.group.GNAME))
					.addChild(new TextArea(struct.group.DESCRIPTION))
					
					.addChild(
							new RefButton(struct.group.PERSONAL_ADMIN,
									this,
									new HorizontalPanel().addChild(new TextField(
											struct.group.PERSONAL_ADMIN
													.relate(struct.group.GNAME)))))

		));
		return form;
	}
	

}
