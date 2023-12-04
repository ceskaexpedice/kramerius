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
    
    public static void main(String[] args) throws MalformedURLException, IOException {
        //String url = "https://k7.inovatika.dev/search/api/client/v7.0/items/uuid:722b67fb-0e53-44b4-b4fe-12b89cca4da7/image/iiif/395,715,780,419/max/0/default.jpg";
        String url = "https://k7.inovatika.dev/search/api/client/v7.0/items/uuid:e7385d75-fac8-4ac0-b673-9e298d65dd68/image/iiif/360,913,1147,664/max/0/default.jpg";

        Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> parseBoundingBox = SimpleIIIFGenerator.parseBoundingBox(url);
        Pair<Integer,Integer> iiifInfo = SimpleIIIFGenerator.getIIIFDescriptor(url);

        BufferedImage thumb = SimpleIIIFGenerator.getThumb(url);
        
        
        double widthScaleFactor = (double) thumb.getWidth() / iiifInfo.getLeft();
        double heightScaleFactor = (double) thumb.getHeight() / iiifInfo.getRight();

        // Vybrání menšího scale factoru, aby zůstal obrázek uvnitř maximálních rozměrů
        double scaleFactor = Math.min(widthScaleFactor, heightScaleFactor);
        System.out.println(scaleFactor);
        
        System.out.println(SimpleIIIFGenerator.scaleBoundingBox(parseBoundingBox, scaleFactor));
        
        BufferedImage createImate = SimpleIIIFGenerator.createImate(thumb, SimpleIIIFGenerator.scaleBoundingBox(parseBoundingBox, scaleFactor));
        
        File f = new File(System.getProperty("user.home")+File.separator+"test.png");
        
        ImageIO.write(createImate, "png", f);
        
        
        
    }
}
