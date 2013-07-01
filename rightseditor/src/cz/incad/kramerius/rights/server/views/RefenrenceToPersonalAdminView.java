package cz.incad.kramerius.rights.server.views;

import cz.incad.kramerius.rights.server.Structure;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.View;

import static org.aplikator.server.descriptor.Panel.column;

public class RefenrenceToPersonalAdminView extends View {


    public RefenrenceToPersonalAdminView() {
        super(Structure.group, "RefenrenceToPersonalAdmin");
        addProperty(Structure.group.GNAME);
        setDefaultSortProperty(Structure.group.GNAME);
        setForm(createGroupForm());
    }

    private Form createGroupForm() {
        Form form = new Form(true);
        form.setLayout(column().add(column().add(new TextField<String>(Structure.group.GNAME)).add(new TextArea(Structure.group.DESCRIPTION))

        ));
        return form;
    }

}
