package cz.incad.kramerius.rights.server.views;

import static org.aplikator.server.descriptor.Panel.*;

import org.aplikator.server.descriptor.View;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;

import cz.incad.kramerius.rights.server.Structure;

public class RefenrenceToPersonalAdminView extends View {

    Structure struct;

    public RefenrenceToPersonalAdminView(Structure structure) {
        super(Structure.group);
        this.struct = structure;
        addProperty(Structure.group.GNAME);
        setSortProperty(Structure.group.GNAME);
        setForm(createGroupForm());
    }

    private Form createGroupForm() {
        Form form = new Form();
        form.setLayout(column().add(column().add(new TextField<String>(Structure.group.GNAME)).add(new TextArea(Structure.group.DESCRIPTION))

        ));
        return form;
    }

}
