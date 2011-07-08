package cz.incad.kramerius.rights.server.arragements;

import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.HorizontalPanel;
import org.aplikator.server.descriptor.RefButton;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.VerticalPanel;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.GroupUserAssoction;

public class UserGroupAssoc extends Arrangement {

    Structure structure;
    Structure.GroupUserAssoction groupUserAssoction;

    UserArrangement userArrangement;
    GroupArrangement groupArrangement;

    public UserGroupAssoc(Structure structure, GroupUserAssoction assoc, UserArrangement userArr, GroupArrangement groupArr) {
        super(assoc);

        setReadableName(structure.user.getName());

        this.structure = structure;
        this.groupUserAssoction = assoc;
        this.userArrangement = userArr;
        this.groupArrangement = groupArr;

        // addProperty(structure.groupUserAssoction.USERS);
        addProperty(structure.groupUserAssoction.USERS.relate(structure.user.NAME));
        addProperty(structure.groupUserAssoction.USERS.relate(structure.user.SURNAME));

        // addProperty(structure.groupUserAssoction.GROUP);
        addProperty(structure.groupUserAssoction.GROUP.relate(structure.group.GNAME));
        setForm(createForm());

    }

    Form createForm() {
        Form form = new Form();
        form.setLayout(new VerticalPanel().addChild(
                new RefButton(structure.groupUserAssoction.USERS, this.userArrangement, new VerticalPanel().addChild(new TextField<String>(structure.groupUserAssoction.USERS.relate(structure.user.NAME))).addChild(new TextField<String>(structure.groupUserAssoction.USERS.relate(structure.user.SURNAME))))).addChild(
                new RefButton(structure.groupUserAssoction.GROUP, this.groupArrangement, new HorizontalPanel().addChild(new TextField<String>(structure.groupUserAssoction.GROUP.relate(structure.group.GNAME))))));
        return form;
    }

}
