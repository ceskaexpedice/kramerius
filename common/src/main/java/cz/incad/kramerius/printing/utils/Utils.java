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
package cz.incad.kramerius.printing.utils;

import java.awt.Dimension;

public class Utils {

    public static Dimension A4 = new Dimension(210, 297);
    public static int DEFAUTL_DPI = 72;

    public static float getInches(int dim) {
        return (float) (dim/25.4f);
    }

    
    public static void main(String[] args) {
        int imgWidth = 1362;
        int imgHeight = 1983;
        
        
        float imgWidthInches = (float)imgWidth/ (float)DEFAUTL_DPI;
        float imgHeightInches = (float)imgHeight / (float)DEFAUTL_DPI;
        
        System.out.println(imgWidthInches);
        System.out.println(imgHeightInches);
        
        float pageWidth = Utils.getInches(A4.width);
        float pageHeight = Utils.getInches(A4.height);
        
        if ((imgHeightInches>pageHeight) || (imgWidthInches > pageWidth)) {
            //scaling..
             float hscale =  (pageHeight / imgHeightInches);
             float wscale =  (pageWidth / imgWidthInches);
             System.out.println(hscale);
             System.out.println(wscale);
             
             float scale = Math.min(hscale, wscale);
             
             
        } else {
            
        }
        
//        System.out.println(pageWidth);
//        System.out.println(pageHeight);
        
    }
}
