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
        super(structure.group);
        this.struct = structure;
        addProperty(struct.group.GNAME);
        setSortProperty(struct.group.GNAME);
        setForm(createGroupForm());
    }

    private Form createGroupForm() {
        Form form = new Form();
        form.setLayout(column().add(column().add(new TextField<String>(struct.group.GNAME)).add(new TextArea(struct.group.DESCRIPTION))

        ));
        return form;
    }

}
