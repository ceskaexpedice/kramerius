package cz.incad.kramerius.imaging;

/**
 * 
 * @author pavels
 */
public enum DeepZoomFullImageScaleFactor {

    ORIGINAL(1.0), NINETY_PER(0.9), EIGHTY_PER(0.8), SEVENTY_PER(0.7), SIXTY_PER(0.6), FIFTY_PER(0.5), FORTY_PER(0.4), THIRTY_PER(0.3);

    private double value;

    private DeepZoomFullImageScaleFactor(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public static DeepZoomFullImageScaleFactor findFactor(double val) {
        DeepZoomFullImageScaleFactor[] factors = values();
        for (DeepZoomFullImageScaleFactor factor : factors) {
            if (val == factor.getValue())
                return factor;
        }
        return null;
    }

    public static double[] getAllowedVals() {
        DeepZoomFullImageScaleFactor[] factors = DeepZoomFullImageScaleFactor.values();
        double[] vals = new double[factors.length];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = factors[i].getValue();
        }
        return vals;
    }
}
