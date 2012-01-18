package cz.incad.kramerius.rights.server.views;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.ReferenceField.reference;

import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.View;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.GroupUserAssoction;

public class UserGroupAssoc extends View {

    Structure structure;
    Structure.GroupUserAssoction groupUserAssoction;

    UserView userView;
    GroupView groupView;

    public UserGroupAssoc(Structure structure, GroupUserAssoction assoc, UserView userView, GroupView groupView) {
        super(assoc);

        setLocalizationKey(structure.user.getId());

        this.structure = structure;
        this.groupUserAssoction = assoc;
        this.userView = userView;
        this.groupView = groupView;

        // addProperty(structure.groupUserAssoction.USERS);
        addProperty(structure.groupUserAssoction.USERS.relate(structure.user.NAME));
        addProperty(structure.groupUserAssoction.USERS.relate(structure.user.SURNAME));

        // addProperty(structure.groupUserAssoction.GROUP);
        addProperty(structure.groupUserAssoction.GROUP.relate(structure.group.GNAME));
        setForm(createForm());

    }

    Form createForm() {
        Form form = new Form();
        form.setLayout(column().add(
                reference(structure.groupUserAssoction.USERS, this.userView, column().add(new TextField<String>(structure.groupUserAssoction.USERS.relate(structure.user.NAME))).add(new TextField<String>(structure.groupUserAssoction.USERS.relate(structure.user.SURNAME))))).add(
                reference(structure.groupUserAssoction.GROUP, this.groupView, row().add(new TextField<String>(structure.groupUserAssoction.GROUP.relate(structure.group.GNAME))))));
        return form;
    }

}
