package cz.incad.kramerius.rights.server.views;

import cz.incad.kramerius.rights.server.Structure;
import org.aplikator.server.descriptor.*;

import static org.aplikator.server.descriptor.Panel.column;

public class RightsCriteriumParamView extends View {


    public RightsCriteriumParamView(Entity entity, Structure struct) {
        super(entity);
        setDefaultSortProperty(Structure.criteriumParam.SHORT_DESC);
        addProperty(Structure.criteriumParam.SHORT_DESC);
        addProperty(Structure.criteriumParam.VALS);

        setForm(createRightCriteriumParamForm());
    }

    private Form createRightCriteriumParamForm() {
        Form form = new Form(true);

        TextField<String> shortDesc = new TextField<String>(Structure.criteriumParam.SHORT_DESC);

        TextArea longDesc = new TextArea(Structure.criteriumParam.LONG_DESC);

        TextArea values = new TextArea(Structure.criteriumParam.VALS);

        form.setLayout(column().add(shortDesc).add(longDesc).add(values));
        return form;
    }

}
