package cz.incad.kramerius.rights.server.views;


import cz.incad.kramerius.rights.server.Structure;
import org.aplikator.server.descriptor.*;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.ReferenceField.reference;

public class RightsCriteriumView extends View {

    private RightsCriteriumParamView rightsCriteriumParamView;

    public RightsCriteriumView(Entity entity, Structure struct, RightsCriteriumParamView rightsCriteriumParamView) {
        super(entity);
        this.rightsCriteriumParamView = rightsCriteriumParamView;

        addProperty(Structure.rightCriterium.QNAME);
        // addProperty(struct.rightCriterium.FIXED_PRIORITY);

        setForm(createCriteriumForm());
    }

    private Form createCriteriumForm() {
        Form form = new Form(true);
        form.setLayout(column().add(column().add(new ComboBox<String>(Structure.rightCriterium.QNAME)).add(
                reference(Structure.rightCriterium.PARAM, rightsCriteriumParamView, column().add(new TextField<String>(Structure.rightCriterium.PARAM.relate(Structure.criteriumParam.SHORT_DESC)))
                        .add(new TextArea(Structure.rightCriterium.PARAM.relate(Structure.criteriumParam.LONG_DESC))).add(new TextField<String>(Structure.rightCriterium.PARAM.relate(Structure.criteriumParam.VALS)))))));
        return form;
    }

}
