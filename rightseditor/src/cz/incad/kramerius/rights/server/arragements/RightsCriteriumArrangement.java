package cz.incad.kramerius.rights.server.arragements;

import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.ComboBox;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.RefButton;
import org.aplikator.server.descriptor.TextArea;
import org.aplikator.server.descriptor.TextField;
import org.aplikator.server.descriptor.VerticalPanel;

import cz.incad.kramerius.rights.server.Structure;

public class RightsCriteriumArrangement extends Arrangement {

    private Structure struct;
    private RightsCriteriumParamArrangement rightsCriteriumParamArrangement;

    public RightsCriteriumArrangement(Entity entity, Structure struct, RightsCriteriumParamArrangement rightsCriteriumParamArrangement) {
        super(entity);
        this.struct = struct;
        this.rightsCriteriumParamArrangement = rightsCriteriumParamArrangement;

        addProperty(struct.rightCriterium.QNAME);
        // addProperty(struct.rightCriterium.FIXED_PRIORITY);

        setForm(createCriteriumForm());
    }

    private Form createCriteriumForm() {
        Form form = new Form();
        form.setLayout(new VerticalPanel().addChild(new VerticalPanel().addChild(new ComboBox<String>(struct.rightCriterium.QNAME)).addChild(
                new RefButton(struct.rightCriterium.PARAM, rightsCriteriumParamArrangement, new VerticalPanel().addChild(new TextField<String>(struct.rightCriterium.PARAM.relate(struct.criteriumParam.SHORT_DESC)).setWidth("30em"))
                        .addChild(new TextArea(struct.rightCriterium.PARAM.relate(struct.criteriumParam.LONG_DESC)).setWidth("30em")).addChild(new TextField<String>(struct.rightCriterium.PARAM.relate(struct.criteriumParam.VALS)).setWidth("30em"))))));
        return form;
    }

}
