package cz.incad.kramerius.rights.server.arragements;

import org.aplikator.server.descriptor.Arrangement;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.VerticalPanel;

import cz.incad.kramerius.rights.server.Structure;

public class RightArrangement extends Arrangement {

    private Structure struct;

    public RightArrangement(Entity entity, Structure structure) {
        super(entity);
        this.struct = structure;

        addProperty(struct.rights.ACTION);
        addProperty(struct.rights.UUID);

        // rozdelit do nejakeho sloupce
        addProperty(struct.rights.USER);
        addProperty(struct.rights.GROUP);

        addProperty(struct.rights.RIGHT_CRITERIUM);

        setForm(createRightForm());

    }

    private Form createRightForm() {
        Form form = new Form();
        form.setLayout(new VerticalPanel().addChild(new VerticalPanel()
        // .addChild(new RefButton(property, arrangement, child))
        // .addChild(new ComboBox(struct.rightCriterium.QNAME))
        // .addChild(
        // new RefButton(struct.rightCriterium.PARAM, rightsCriteriumParamArr,
        // new HorizontalPanel()
        // .addChild(new
        // TextField(struct.rightCriterium.PARAM.relate(struct.criteriumParam.VALS)).setWidth("30em"))
        // ))
                ));
        return form;
    }

}
