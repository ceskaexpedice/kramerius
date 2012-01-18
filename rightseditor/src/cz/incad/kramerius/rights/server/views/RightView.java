package cz.incad.kramerius.rights.server.views;

import static org.aplikator.server.descriptor.Panel.*;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;

import cz.incad.kramerius.rights.server.Structure;

public class RightView extends View {

    private Structure struct;

    public RightView(Entity entity, Structure structure) {
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
        form.setLayout(column().add(column()
        // .addChild(new RefButton(property, view, child))
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
