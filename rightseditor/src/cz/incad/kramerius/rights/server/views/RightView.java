package cz.incad.kramerius.rights.server.views;

import cz.incad.kramerius.rights.server.Structure;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Form;
import org.aplikator.server.descriptor.View;

import static org.aplikator.server.descriptor.Panel.column;

public class RightView extends View {


    public RightView(Entity entity, Structure structure) {
        super(entity);

        addProperty(Structure.rights.ACTION);
        addProperty(Structure.rights.UUID);

        // rozdelit do nejakeho sloupce
        addProperty(Structure.rights.USER);
        addProperty(Structure.rights.GROUP);

        addProperty(Structure.rights.RIGHT_CRITERIUM);

        setForm(createRightForm());

    }

    private Form createRightForm() {
        Form form = new Form(true);
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
