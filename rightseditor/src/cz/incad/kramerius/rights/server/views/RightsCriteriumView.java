package cz.incad.kramerius.rights.server.views;


import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.ReferenceField.reference;

import org.aplikator.server.descriptor.ComboBox;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.View;

import cz.incad.kramerius.rights.server.Structure;

public class RightsCriteriumView extends View {

    private Structure struct;
    private RightsCriteriumParamView rightsCriteriumParamView;

    public RightsCriteriumView(Entity entity, Structure struct, RightsCriteriumParamView rightsCriteriumParamView) {
        super(entity);
        this.struct = struct;
        this.rightsCriteriumParamView = rightsCriteriumParamView;

        addProperty(struct.rightCriterium.QNAME);
        // addProperty(struct.rightCriterium.FIXED_PRIORITY);

        setForm(createCriteriumForm());
    }

    private Form createCriteriumForm() {
        Form form = new Form();
        form.setLayout(column().add(column().add(new ComboBox<String>(struct.rightCriterium.QNAME)).add(
                reference(struct.rightCriterium.PARAM, rightsCriteriumParamView, column().add(new TextField<String>(struct.rightCriterium.PARAM.relate(struct.criteriumParam.SHORT_DESC)).setWidth("30em"))
                        .add(new TextArea(struct.rightCriterium.PARAM.relate(struct.criteriumParam.LONG_DESC)).setWidth("30em")).add(new TextField<String>(struct.rightCriterium.PARAM.relate(struct.criteriumParam.VALS)).setWidth("30em"))))));
        return form;
    }

}
