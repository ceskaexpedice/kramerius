package cz.incad.kramerius.rights.server.views;

import static org.aplikator.server.descriptor.Panel.*;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;

import cz.incad.kramerius.rights.server.Structure;

public class RightsCriteriumParamView extends View {

    private Structure structure;

    public RightsCriteriumParamView(Entity entity, Structure struct) {
        super(entity);
        this.structure = struct;
        setSortProperty(struct.criteriumParam.SHORT_DESC);
        addProperty(struct.criteriumParam.SHORT_DESC);
        addProperty(struct.criteriumParam.VALS);

        setForm(createRightCriteriumParamForm());
    }

    private Form createRightCriteriumParamForm() {
        Form form = new Form();

        TextField<String> shortDesc = new TextField<String>(structure.criteriumParam.SHORT_DESC);
        shortDesc.setWidth("100%");

        TextArea longDesc = new TextArea(structure.criteriumParam.LONG_DESC);
        longDesc.setWidth("100%");

        TextArea values = new TextArea(structure.criteriumParam.VALS);
        values.setWidth("100%");

        form.setLayout(column().add(shortDesc).add(longDesc).add(values));
        return form;
    }

}
