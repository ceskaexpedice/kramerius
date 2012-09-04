package cz.incad.kramerius.imaging;

/**
 * Scale factor for original.
 * 
 * <p>
 * If orignal is too big, you can define smaller image as original (by configuration)
 * This options save disk space used by tile cache.
 * </p>
 * @author pavels
 */
public enum DeepZoomFullImageScaleFactor {
    /** Original */
    ORIGINAL(1.0), 
    /** 90 % */
    NINETY_PER(0.9), 
    /** 80 % */
    EIGHTY_PER(0.8), 
    /** 70 % */
    SEVENTY_PER(0.7), 
    /** 60 % */
    SIXTY_PER(0.6), 
    /** 50 % */
    FIFTY_PER(0.5), 
    /** 40 % */
    FORTY_PER(0.4), 
    /** 30 % */
    THIRTY_PER(0.3);

    private double value;

    private DeepZoomFullImageScaleFactor(double value) {
        this.value = value;
    }
    
    /**
     * REturns scaling factor
     * @return scaling factor
     */
    public double getValue() {
        return value;
    }

    /**
     * Find factor from given raw value
     * @param val raw value
     * @return Found factor enum
     */
    public static DeepZoomFullImageScaleFactor findFactor(double val) {
        DeepZoomFullImageScaleFactor[] factors = values();
        for (DeepZoomFullImageScaleFactor factor : factors) {
            if (val == factor.getValue())
                return factor;
        }
        return null;
    }

    /**
     * Returns all allowed scaling factors
     * @return all allowed scaling factors
     */
    public static double[] getAllowedVals() {
        DeepZoomFullImageScaleFactor[] factors = DeepZoomFullImageScaleFactor.values();
        double[] vals = new double[factors.length];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = factors[i].getValue();
        }
        return vals;
    }
}
