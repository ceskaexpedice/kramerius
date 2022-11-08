package cz.incad.kramerius.services.transform;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class SourceToDestTransform {

    public static enum Format {

        COPY {
            @Override
            public SourceToDestTransform create() {
                return new BasicSourceToDestTransform();
            }
        },
        K7 {
            @Override
            public SourceToDestTransform create() {
                return new K7SourceToDestTransform();
            }
        };

        public static SourceToDestTransform findTransform(String transformString) {
            Format[] values = Format.values();
            for (Format f: values) {
                if (f.name().toUpperCase().equals(transformString)) {
                    return f.create();
                }
            }
            return new BasicSourceToDestTransform();
        }

        public abstract  SourceToDestTransform create();
    }

    public abstract void  transform(Element sourceDocElm, Document destDocument, Element destDocElem);

    public abstract String getField(String fieldId);


}
