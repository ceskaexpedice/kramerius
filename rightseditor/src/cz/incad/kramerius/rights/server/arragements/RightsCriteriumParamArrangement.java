package cz.incad.kramerius.rights.server.arragements;

import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.VerticalPanel;

import cz.incad.kramerius.rights.server.Structure;

public class RightsCriteriumParamArrangement extends Arrangement {

    private Structure structure;

    public RightsCriteriumParamArrangement(Entity entity, Structure struct) {
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

        form.setLayout(new VerticalPanel().addChild(shortDesc).addChild(longDesc).addChild(values));
        return form;
    }

}
