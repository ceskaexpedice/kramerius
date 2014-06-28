package cz.incad.Kramerius.views.localprint;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

public class AbstractPrepareViewObject {

    protected List<String> imgelements = new ArrayList<String>();
    protected List<String> styles = new ArrayList<String>();

    public List<String> getStyles() {
        return this.styles;
    }

    public List<String> getImgelements() {
        return imgelements;
    }

    protected String width(String ident) {
        return "#"+ident+"{ width:100%;}";
    }

    protected String height(String ident) {
        return "#"+ident+"{ height:100%;}";
    }

    protected String createIdent(int i, int bits) {
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < bits; j++) {
            int v = (i >> j) & 1;
            builder.append(v==0 ? 'a':'z');
        }
        return builder.toString();
    }

    protected int numberOfBits(int length) {
        double bits = Math.floor( Math.pow(length,0.5));
        if (Math.pow(bits, 2) < length) {
            bits = bits +1;
        }
        return (int) bits;
    }

    protected void createStyle(double ratio, String ident, Dimension readDim) {
        double calculated = (double)readDim.height/(double)readDim.width;
        if (calculated< ratio) {
            this.styles.add(width(ident));
        } else {
            this.styles.add(height(ident));
        }
    }
}
