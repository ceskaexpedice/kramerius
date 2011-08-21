/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.imaging.utils;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;

public class ImageUtils {

    public static BufferedImage scaleByHeight(BufferedImage img, Rectangle pageBounds, int height, ScalingMethod scalingMethod) {
        if (scalingMethod == null) scalingMethod = ScalingMethod.BILINEAR;
        int nHeight = height;
    	double div = (double)pageBounds.getHeight() / (double)nHeight;
    	double nWidth = (double)pageBounds.getWidth() / div;
    	BufferedImage scaledImage = KrameriusImageSupport.scale(img, (int)nWidth, nHeight, scalingMethod, false);
    	return scaledImage;
    }

    public static BufferedImage scaleByWidth(BufferedImage img, Rectangle pageBounds, int width ,ScalingMethod scalingMethod) {
        if (scalingMethod == null) scalingMethod = ScalingMethod.BILINEAR;
    	int nWidth = width;
    	double div = (double)pageBounds.getWidth() / (double)nWidth;
    	double nHeight = (double)pageBounds.getHeight() / div;
    	BufferedImage scaledImage = KrameriusImageSupport.scale(img, nWidth,(int) nHeight,scalingMethod, false);
    	return scaledImage;
    }

    public static BufferedImage scaleByPercent(BufferedImage img, Rectangle pageBounds, double percent, ScalingMethod scalingMethod) {
       if (scalingMethod == null) scalingMethod = ScalingMethod.BILINEAR;
        if ((percent <= 0.99) || (percent >= 1.11)) {
    		int nWidth = (int) (pageBounds.getWidth() * percent);
    		int nHeight = (int) (pageBounds.getHeight() * percent);
    		BufferedImage scaledImage = KrameriusImageSupport.scale(img, nWidth, nHeight,scalingMethod, false);
    		return scaledImage;
    	} else return (BufferedImage) img;
    }

}
