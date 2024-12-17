package cz.knav.pdf;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.kramerius.pdf.impl.AbstractPDFRenderSupport.ScaledImageOptions;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Creates searchable PDF (with text under images) from images and ALTO
 * documents. Works with horizontal writing systems.
 */
public class PdfTextUnderImage {

    public static final Logger LOGGER = Logger.getLogger(PdfTextUnderImage.class.getName());

    public static HashMap<String, String> REMAP_FAMILIES = new HashMap<String, String>(){{
        put("arial", "arial ce");
        put("times new roman", "gentium plus");
    }};

    private static boolean registerFontDirectoriesDone = false;


    private Color fontColor = Color.black;
    private boolean debug = false;

    public PdfTextUnderImage() {
        super();
    }

    /**
     * This is an “expensive” method if you have a lot of fonts on your system.
     * Don’t use it in a servlet because it takes time to scan the font
     * directories; it’s better to use it when the JVM starts up, so that you
     * can use the font factory throughout your web application.
     *
     * @param fontDirectories
     */
    public static void registerFontDirectories(List<String> fontDirectories) {
        FontFactory.registerDirectories();
        if (fontDirectories != null) {
            for (String s : fontDirectories) {
                int number = FontFactory.registerDirectory(s);
                LOGGER.log(Level.INFO, "registred fonts {0}", number);
            }
        }
        registerFontDirectoriesDone = true;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void imageWithAlto(Document document, PdfWriter writer, org.w3c.dom.Document alto, ScaledImageOptions options) {
        PdfContentByte canvas = writer.getDirectContentUnder();
        if (this.debug) {
            this.fontColor = Color.yellow;
            canvas = writer.getDirectContent();
        }
        putTextsToPdf(document, alto, canvas, options);
    }

    private void putTextsToPdf(Document document, org.w3c.dom.Document alto, PdfContentByte canvas, ScaledImageOptions options) {
        NodeList nodeList = alto.getElementsByTagName("*");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            if (isString(element) || isSP(element) || isHYP(element)) {
                String text;
                int fontStyle;

                if (isString(element)) {
                    text = element.getAttribute("CONTENT");
                } else if (isSP(element)) {
                    text = " ";
                } else {
                    text = "‐";
                }

                final String style = "STYLE";
                if (element.hasAttribute(style)) {
                    fontStyle = getFontStyle(element.getAttribute(style));
                } else {
                    fontStyle = getFontStyle(element, alto);
                }

                String fontFamily = getFontFamily(element, alto);
                if (REMAP_FAMILIES.containsKey(fontFamily.toLowerCase())) {
                    fontFamily = REMAP_FAMILIES.get(fontFamily.toLowerCase());
                }
                Font font;
                Float fontSize = getFontSize(element, alto, options);
                if(fontFamily.equals("Courier"))
                    font= FontFactory.getFont(FontFactory.COURIER, fontSize, fontStyle, fontColor);
                else
                    font = FontFactory.getFont(fontFamily, BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    fontSize, fontStyle, fontColor);
                Phrase phrase = new Phrase(text, font);
                ColumnText.showTextAligned(canvas, com.lowagie.text.Element.ALIGN_LEFT, phrase,
                        getLowerLeftHorizontal(element, options.getScaleFactor(), options.getXoffset()),
                        getLowerLeftVertical(document, element, options.getScaleFactor(), options.getYoffset()), getRotation(element));
            }
        }
    }

    private String getFontFamily(Element element, org.w3c.dom.Document alto) {
        boolean ignoreSizeAndStyle = KConfiguration.getInstance()
                .getConfiguration()
                .getBoolean("pdfQueue.ignoreMissingSizeAndStyle", true);
        String r = null;
        Element textStyle = getTextStyle(element, alto);
        if (textStyle != null) {
            final String s = "FONTFAMILY";
            if (textStyle.hasAttribute(s)) {
                r = textStyle.getAttribute(s);
            }
        }
        if (r == null) {
            if (isSP(element) || isHYP(element)) {
                r = "Courier";
            } else {
                if (ignoreSizeAndStyle) {
                    r = "Courier";
                } else {
                    throwPdfTextUnderImageException();
                }
            }
        }
        return r;
    }

    private Float getFontSize(Element element, org.w3c.dom.Document alto, ScaledImageOptions options) {
        boolean ignoreSizeAndStyle = KConfiguration.getInstance()
                .getConfiguration()
                .getBoolean("pdfQueue.ignoreMissingSizeAndStyle", true);

        Float r = null;
        Element textStyle = getTextStyle(element, alto);
        if (textStyle != null) {
            final String s = "FONTSIZE";
            if (textStyle.hasAttribute(s)) {
                r = pxOrPtOr(textStyle.getAttribute(s), true);
            }
        }
        if (r == null) {
            if (isSP(element) || isHYP(element)) {
                r = 10f;
            } else if (ignoreSizeAndStyle) {
                String t = element.getAttribute("CONTENT");
                int width = Integer.parseInt(element.getAttribute("WIDTH"));
                Float r2 = (((float)width)/t.length())/0.6f;//Courier's width is 60% of its height
                //Float r1 = Float.valueOf(element.getAttribute("HEIGHT"));//We don't want to extend to other lines
                r = r2;//Math.min(r1,r2);
            } else {
                throwPdfTextUnderImageException();
            }

        }
        return r * options.getScaleFactor();
    }

    private int getFontStyle(Element element, org.w3c.dom.Document alto) {
        int r = Font.NORMAL;
        Element textStyle = getTextStyle(element, alto);
        if (textStyle != null) {
            final String s = "FONTSTYLE";
            if (textStyle.hasAttribute(s)) {
                r = getFontStyle(textStyle.getAttribute(s));
            }
        }
        return r;
    }

    private static int getFontStyle(String fontStylesType) {
        int r = Font.NORMAL;
        if (fontStylesType.contains("bold")) {
            r |= Font.BOLD;
        }
        if (fontStylesType.contains("italics")) {
            r |= Font.ITALIC;
        }
        if (fontStylesType.contains("underline")) {
            r |= Font.UNDERLINE;
        }
        return r;
    }

    private static float getAttrFromElementOrParent(Element element, String attrName, Float scale) {
        float r = 0;
        if (element.hasAttribute(attrName)) {
            r = pxOrPtOr(element.getAttribute(attrName));
        } else if (((Element) element.getParentNode()).hasAttribute(attrName)) {
            r = pxOrPtOr(((Element) element.getParentNode()).getAttribute(attrName));
        } else {
            throwPdfTextUnderImageException();
        }
        return r * scale;

    }

    private static float getHeight(Element element, Float scale) {
        return getAttrFromElementOrParent(element, "HEIGHT", scale);
    }

    private static float getLowerLeftHorizontal(Element element, Float scale, int xoffset) {
        return getAttrFromElementOrParent(element, "HPOS", scale) + xoffset;
    }

    private float getLowerLeftVertical(Document document, Element element, Float scale, int yoffset) {
        return document.getPageSize().getHeight() - getAttrFromElementOrParent(element, "VPOS", scale) - getHeight(element, scale) + yoffset;
    }

    private static float getRotation(Element element) {
        float r = 0;
        final String rotation = "ROTATION";
        Element e = element;
        while (!e.hasAttribute(rotation) && e.getParentNode() != null && e.getParentNode() instanceof Element) {
            e = (Element) e.getParentNode();
        }
        if (e.hasAttribute(rotation)) {
            r = Float.parseFloat(e.getAttribute("rotation"));
            //r = (new Float(e.getAttribute(rotation))).floatValue();
        }
        return r;
    }

    private Element getTextStyle(Element element, org.w3c.dom.Document alto) {
        Element r = null;
        final String stylerefs = "STYLEREFS";
        Element e = element;
        while (!e.hasAttribute(stylerefs) && e.getParentNode() != null) {
            if (e.getParentNode() instanceof Element) {
                e = (Element) e.getParentNode();
            } else {
                break;
            }
        }
        if (e.hasAttribute(stylerefs)) {
            String textStyleIdAttribute = e.getAttribute(stylerefs);

            for (String textStyleId : textStyleIdAttribute.split(" ")) {
                NodeList nodeList = alto.getElementsByTagName("TextStyle");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element textStyle = (Element) nodeList.item(i);
                    final String id = "ID";
                    if (textStyle.hasAttribute(id) && textStyleId.equals(textStyle.getAttribute(id))) {
                        r = textStyle;
                    }
                }
            }
        }
        return r;
    }

    private static boolean isString(Element e) {
        return e.getTagName().equals("String");
    }

    private static boolean isSP(Element e) {
        return e.getTagName().equals("SP");
    }

    private static boolean isHYP(Element e) {
        return e.getTagName().equals("HYP");
    }

    private static float pxOrPtOr(String s) {
        return pxOrPtOr(s, false);
    }

    private static float pxOrPtOr(String s, boolean ptToPx) {
        float r = Float.parseFloat(s);
        if (ptToPx) {
            r = 15 * r / 4;
        } /*else {
            //bad: r = 4 * r / 15;
        }*/
        return r;
        /* bad:
    	float r = (new Float(s)).floatValue();
    	if (ptToPx) {
    		r = 3 * r / 4;
    	} else {
    		//r = 4 * r / 3;
    	}
    	return r;
         */
    }

    private static void throwPdfTextUnderImageException() throws PdfTextUnderImageException {
        throw new PdfTextUnderImageException();
    }

    private static void throwPdfTextUnderImageException(String s) throws PdfTextUnderImageException {
        throw new PdfTextUnderImageException(s);
    }

    private static void throwPdfTextUnderImageException(Exception e) throws PdfTextUnderImageException {
        throw new PdfTextUnderImageException(e.getMessage(), e);
    }

}
