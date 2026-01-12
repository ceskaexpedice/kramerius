    package cz.incad.kramerius.document.model;

    import cz.incad.kramerius.KrameriusModels;

    /**
     * Represents image page
     * @author pavels
     */
    public class ImagePage extends AbstractPage {

        private float physicalWidth;
        private float physicalHeight;
        private String unit;

        public ImagePage(String modelName, String uuid) {
            super(modelName, uuid);
        }


        public void setPhysicalHeight(float physicalHeight) {
            this.physicalHeight = physicalHeight;
        }

        public float getPhysicalHeight() {
            return physicalHeight;
        }

        public void setPhysicalWidth(float physicalWidth) {
            this.physicalWidth = physicalWidth;
        }

        public float getPhysicalWidth() {
            return physicalWidth;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getUnit() {
            return unit;
        }

        @Override
        public void visitPage(PageVisitor visitor, Object obj) {
            visitor.visit(this, obj);
        }

    }
