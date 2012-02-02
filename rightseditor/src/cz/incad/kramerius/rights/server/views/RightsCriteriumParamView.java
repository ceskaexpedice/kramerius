package cz.incad.kramerius.rights.server.views;

import static org.aplikator.server.descriptor.Panel.*;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;

import cz.incad.kramerius.rights.server.Structure;

public class RightsCriteriumParamView extends View {


    public RightsCriteriumParamView(Entity entity, Structure struct) {
        super(entity);
        setSortProperty(Structure.criteriumParam.SHORT_DESC);
        addProperty(Structure.criteriumParam.SHORT_DESC);
        addProperty(Structure.criteriumParam.VALS);

        setForm(createRightCriteriumParamForm());
    }

    private Form createRightCriteriumParamForm() {
        Form form = new Form();

        TextField<String> shortDesc = new TextField<String>(Structure.criteriumParam.SHORT_DESC);
        shortDesc.setWidth("100%");

        TextArea longDesc = new TextArea(Structure.criteriumParam.LONG_DESC);
        longDesc.setWidth("100%");

        TextArea values = new TextArea(Structure.criteriumParam.VALS);
        values.setWidth("100%");

        form.setLayout(column().add(shortDesc).add(longDesc).add(values));
        return form;
    }

}
