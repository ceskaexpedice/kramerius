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

package cz.incad.kramerius.editor.client.view;

import com.allen_sauer.gwt.dnd.client.HasDragHandle;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 * XXX implement proper handling of inaccessible thumbnails
 * 
 * @author Jan Pokorsky
 */
public final class ElementViewImpl extends Composite implements ElementView, HasDragHandle {

    public interface ElementViewUiBinder extends UiBinder<Widget, ElementViewImpl> {}

    private static ElementViewUiBinder uiBinder = GWT.create(ElementViewUiBinder.class);

    private Callback callback;
    private PopupPanel popupPanel;

    @UiField Image elmImage;
    @UiField Label elmLabel;
    @UiField Anchor elmPreview;
    @UiField Anchor elmOpen;

    public ElementViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setLocation(String url) {
        elmImage.setUrl(url);
        // XXX ImageConsumerQueue needs more investigation
//        ImageConsumerQueue.addConsumer(elmImage, url);
    }

    @Override
    public void showPreview(String url) {
        if (popupPanel == null) {
            initPopupUI(url);
        }
//        menuPopup.hide();
        popupPanel.center();
    }

    @Override
    public void setLabel(String s) {
        elmLabel.setText(s);
    }

    @Override
    public void setTooltip(String s) {
        elmLabel.setTitle(s);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public Widget getDragHandle() {
        return elmImage;
    }

    @Override
    public void setCallback(Callback c) {
        this.callback = c;
    }

    @Override
    public void setOpenEnabled(boolean b) {
        elmOpen.setVisible(b);
    }

    @UiHandler("elmPreview")
    void onPreviewClick(ClickEvent ce) {
        callback.onPreviewClick();
    }

    @UiHandler("elmOpen")
    void onOpenClick(ClickEvent ce) {
        callback.onOpenClick();
    }

    private void initPopupUI(final String url) {
        popupPanel = new PopupPanel(true, true);
        popupPanel.setAnimationEnabled(true);
        final Image preview = new Image();
        final Label status = new Label("Loading...");
        FlowPanel flowPanel = new FlowPanel();
        flowPanel.add(status);
        flowPanel.add(preview);
        popupPanel.setWidget(flowPanel);
        preview.setVisible(false);
        preview.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent event) {
                preview.setVisible(true);
                status.setVisible(false);
                centerUpdatedPopupWorkaround(popupPanel);
            }
        });
        preview.addErrorHandler(new ErrorHandler() {

            @Override
            public void onError(ErrorEvent event) {
                status.setText("Cannot load image " + url);
                centerUpdatedPopupWorkaround(popupPanel);
            }
        });
        preview.setUrl(url);
    }

    /**
     * @see http://code.google.com/p/google-web-toolkit/issues/detail?id=4221
     */
    private static void centerUpdatedPopupWorkaround(PopupPanel pp) {
        pp.center();
//        pp.center();
//        pp.center();
    }

    @Override
    public String toString() {
        String objectToString = getClass().getName() + "@" + Integer.toHexString(hashCode());
        return objectToString + "[" + (elmLabel == null ? "not initialized" : elmLabel.getText()) + "]";
    }

    public interface ImageConsumer {
        void consumeImage(Image im);
    }

    /**
     * The class should help not to overload browser and web server
     * with multiple concurrent requests to load images. The browser seems to be
     * more responsive then.
     * XXX try to load 4 images at once
     */
    private static final class ImageConsumerQueue implements LoadHandler, ErrorHandler {

        private static ImageConsumerQueue INSTANCE = new ImageConsumerQueue();
        private List<String> locations = new ArrayList<String>();
        private List<Image> iconsumers = new ArrayList<Image>();
        private HandlerRegistration imageLoadHandler;
        private HandlerRegistration imageErrorHandler;

        public static void addConsumer(Image consumer, String location) {
            boolean isRunning = !INSTANCE.locations.isEmpty();
            INSTANCE.locations.add(location);
            INSTANCE.iconsumers.add(consumer);
            if (!isRunning) {
                INSTANCE.runTask();
            }
        }

        private void prepareNextTask() {
            String location = locations.remove(0);
            iconsumers.remove(0);
            imageLoadHandler.removeHandler();
            imageErrorHandler.removeHandler();
//            System.out.println("##runTask.loaded: " + location);
            runTask();
        }

        private void runTask() {
            if (locations.isEmpty()) {
                return;
            }
            String location = locations.get(0);
            Image image = iconsumers.get(0);
            imageLoadHandler = image.addLoadHandler(this);
            imageErrorHandler = image.addErrorHandler(this);
            image.setUrl(location);
//            System.out.println("##runTask.location: " + location);
        }

        @Override
        public void onLoad(LoadEvent event) {
            prepareNextTask();
        }

        @Override
        public void onError(ErrorEvent event) {
            prepareNextTask();
        }

    }

}
