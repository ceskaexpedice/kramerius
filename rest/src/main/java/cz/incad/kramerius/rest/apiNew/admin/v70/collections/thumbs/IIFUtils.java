/*
 * Copyright (C) Nov 20, 2023 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.admin.v70.collections.thumbs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;

import org.apache.commons.lang3.tuple.Pair;

public class IIFUtils {

    public static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> scaleBoundingBox(
            Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> boundingBox, double scaleFactor) {
    
        int x = boundingBox.getKey().getKey();
        int y = boundingBox.getKey().getValue();
        int width = boundingBox.getValue().getKey();
        int height = boundingBox.getValue().getValue();
    
    
        int scaledX = (int) (x * scaleFactor);
        int scaledY = (int) (y * scaleFactor);
    
        int scaledWidth = (int) (width * scaleFactor);
        int scaledHeight = (int) (height * scaleFactor);
    
        Pair<Integer, Integer> scaledOrigin = Pair.of(scaledX, scaledY);
        Pair<Integer, Integer> scaledSize = Pair.of(scaledWidth, scaledHeight);
    
        return Pair.of(scaledOrigin, scaledSize);
    }
}
