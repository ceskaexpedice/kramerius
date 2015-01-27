package cz.incad.kramerius.rest.api.k5.client.pdf.utils;

import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.writeImageToStream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.logging.Logger;

import com.google.inject.Provider;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;

import cz.incad.kramerius.pdf.impl.AbstractPDFRenderSupport.ScaledImageOptions;
import cz.incad.kramerius.service.TextsService;

public class Images {

    public static final Logger LOGGER = Logger
            .getLogger(Images.class.getName());

    public static ScaledImageOptions insertJavaImage(Document document,
            float percentage, BufferedImage javaImg) throws IOException,
            BadElementException, MalformedURLException, DocumentException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeImageToStream(javaImg, "jpeg", bos);

        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(bos
                .toByteArray());

        Float ratio = ratio(document, percentage, javaImg);

        int fitToPageWidth = (int) (javaImg.getWidth(null) * ratio);
        int fitToPageHeight = (int) (javaImg.getHeight(null) * ratio);

        int offsetX = ((int) document.getPageSize().getWidth() - fitToPageWidth) / 2;
        int offsetY = ((int) document.getPageSize().getHeight() - fitToPageHeight) / 2;

        img.scaleAbsoluteHeight(ratio * img.getHeight());

        img.scaleAbsoluteWidth(ratio * img.getWidth());
        img.setAbsolutePosition((offsetX), document.getPageSize().getHeight()
                - offsetY - (ratio * img.getHeight()));

        document.add(img);

        ScaledImageOptions options = new ScaledImageOptions();
        options.setXdpi(img.getDpiX());
        options.setYdpi(img.getDpiY());

        options.setXoffset(offsetX);
        options.setYoffset(offsetY);

        options.setWidth(fitToPageWidth);
        options.setHeight(fitToPageHeight);
        options.setScaleFactor(ratio);

        return options;
    }

    public static Float ratio(Document document, float percentage,
            BufferedImage javaImg) {
        Float wratio = document.getPageSize().getWidth()
                / javaImg.getWidth(null);
        Float hratio = document.getPageSize().getHeight()
                / javaImg.getHeight(null);
        Float ratio = Math.min(wratio, hratio);
        if (percentage != 1.0) {
            ratio = ratio * percentage;
        }
        return ratio;
    }


    public static void imageNotAvailable(Document document, TextsService textsService, Provider<Locale> localesProvider, Font font) throws IOException, DocumentException {
        String text = textsService.getText("security_fail",localesProvider.get());
        text = text != null ? text : "security_fail";
        Chunk chunk = new Chunk(text, font);
        Paragraph na = new Paragraph();
        na.add(chunk);
        document.add(na);
    }
}
