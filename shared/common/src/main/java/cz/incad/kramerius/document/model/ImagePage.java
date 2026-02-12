package cz.incad.kramerius.document.model;

import org.w3c.dom.Document;

/** Represents image page */
public class ImagePage extends AbstractPage {

    /** Physical dimension set == found real scale and unit */
    private boolean physicalDimensionsSet = false;
    /** Physical dimension unit; cm; mm; etc .. */
    private String physicalDimensionUnit;
    private double physicalScaleFactor;

    /** Image width */
    private double width;
    /** Image height */
    private double height;
    /** Scale factor */
    private double scaleFactor;

    private Document altoXML;


    public ImagePage(String modelName, String uuid) {
        super(modelName, uuid);
    }
    // prectena vyska
    public void setHeight(double height) {
        this.height = height;
    }

    public double getHeight() {
        return height;
    }
    // prectena sirka
    public void setWidth(double width) {
        this.width = width;
    }

    public double getWidth() {
        return width;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setPhysicalDimensionUnit(String physicalDimensionUnit) {
        this.physicalDimensionUnit = physicalDimensionUnit;
    }

    public void setPhysicalDimensions(double physicalScale) {
        this.physicalScaleFactor = physicalScale;
    }

    public double getPhysicalScaleFactor() {
        return physicalScaleFactor;
    }

    public void setPhysicalScaleFactor(double physicalScale) {
        this.physicalScaleFactor = physicalScale;
    }

    public boolean isPhysicalDimensionsSet() {
        return physicalDimensionsSet;
    }

    public void setPhysicalDimensionsSet(boolean physicalDimensionsSet) {
        this.physicalDimensionsSet = physicalDimensionsSet;
    }

    public String getPhysicalDimensionUnit() {
        return physicalDimensionUnit;
    }


    public Document getAltoXML() {
        return altoXML;
    }

    public void setAltoXML(Document altoXML) {
        this.altoXML = altoXML;
    }

    @Override
    public void visitPage(PageVisitor visitor, Object obj) {
        visitor.visit(this, obj);
    }

}
