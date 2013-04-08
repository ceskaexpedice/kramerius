package cz.incad.kramerius.rights.server.views;

import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.Structure.GroupUserAssoction;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.View;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.ReferenceField.reference;

public class UserGroupAssoc extends View {

    Structure structure;
    Structure.GroupUserAssoction groupUserAssoction;

    UserView userView;
    GroupView groupView;

    public UserGroupAssoc(Structure structure, GroupUserAssoction assoc, UserView userView, GroupView groupView) {
        super(assoc);

        setLocalizationKey(Structure.user.getId());

        this.structure = structure;
        this.groupUserAssoction = assoc;
        this.userView = userView;
        this.groupView = groupView;

        // addProperty(structure.groupUserAssoction.USERS);
        addProperty(Structure.groupUserAssoction.USERS.relate(Structure.user.NAME));
        addProperty(Structure.groupUserAssoction.USERS.relate(Structure.user.SURNAME));

        // addProperty(structure.groupUserAssoction.GROUP);
        addProperty(Structure.groupUserAssoction.GROUP.relate(Structure.group.GNAME));
        setForm(createForm());

    }

    Form createForm() {
        Form form = new Form(true);
        form.setLayout(column().add(
                reference(Structure.groupUserAssoction.USERS, this.userView, column().add(new TextField<String>(Structure.groupUserAssoction.USERS.relate(Structure.user.NAME))).add(new TextField<String>(Structure.groupUserAssoction.USERS.relate(Structure.user.SURNAME))))).add(
                reference(Structure.groupUserAssoction.GROUP, this.groupView, row().add(new TextField<String>(Structure.groupUserAssoction.GROUP.relate(Structure.group.GNAME))))));
        return form;
    }

}
