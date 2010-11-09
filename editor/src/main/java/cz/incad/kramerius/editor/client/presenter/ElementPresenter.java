/*
 * Copyright (C) 2010 Jan Pokorsky
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package cz.incad.kramerius.editor.client.presenter;

import cz.incad.kramerius.editor.client.view.ElementView;
import cz.incad.kramerius.editor.client.view.ViewUtils;
import cz.incad.kramerius.editor.share.GWTKrameriusObject;
import cz.incad.kramerius.editor.share.GWTKrameriusObject.Kind;

/**
 *
 * @author Jan Pokorsky
 */
public class ElementPresenter implements Presenter, ElementView.Callback {

    private ElementView display;
    private GWTKrameriusObject model;
    private final EditorPresenter ebus;
    private boolean isBound = false;

    public ElementPresenter(ElementView display, EditorPresenter bus) {
        this.display = display;
        this.ebus = bus;
    }

    public void setModel(GWTKrameriusObject model) {
        this.model = model;
    }
    public void bind() {
        if (isBound) {
            return;
        }
        isBound = true;
        display.setCallback(this);

        display.setLocation(model.getLocation());
        display.setLabel(ViewUtils.makeLabelVisible(model.getTitle(), 15));
        display.setTooltip(model.getKind().toLocalizedString() + ": " + model.getTitle());
        display.setOpenEnabled(model.getKind() != Kind.PAGE);
    }

    @Override
    public Display getDisplay() {
        return display;
    }

    public GWTKrameriusObject getModel() {
        return model;
    }

    @Override
    public void onPreviewClick() {
        display.showPreview(model.getPreviewLocation());
    }

    @Override
    public void onOpenClick() {
        ebus.load(model.getPID());
    }

}