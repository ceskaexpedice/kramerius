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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.json.JSONObject;


import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class SimpleIIIFGenerator  extends ThumbsGenerator {
    
    public static final Logger LOGGER = Logger.getLogger(SimpleIIIFGenerator.class.getName());
    
    public static final String REGEXP_LEGACY = ".+/iiif/(uuid:[a-fA-F0-9-]+)/\\d+,\\d+,\\d+,\\d+/max/0/default\\.jpg";

    public static final String REGEXP = ".+/(uuid:[a-fA-F0-9-]+)(/image/iiif/)\\d+,\\d+,\\d+,\\d+/max/0/default\\.jpg";
    public static final Pattern REGEXP_PATTERN = Pattern.compile(REGEXP);
    public static final Pattern REGEXP_LEGACY_PATTERN = Pattern.compile(REGEXP_LEGACY);
    
    @Override
    public BufferedImage generateThumbnail(String url) throws IOException {
        Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> parseBoundingBox = parseBoundingBox(url);
        Pair<Integer,Integer> iiifInfo = getIIIFDescriptor(url);
        BufferedImage thumb = getThumb(url);
        double widthScaleFactor = (double) thumb.getWidth() / iiifInfo.getLeft();
        double heightScaleFactor = (double) thumb.getHeight() / iiifInfo.getRight();
        double scaleFactor = Math.min(widthScaleFactor, heightScaleFactor);
        BufferedImage image = SimpleIIIFGenerator.createImate(thumb, scaleBoundingBox(parseBoundingBox, scaleFactor));
        return image;
    }

    @Override
    public boolean acceptUrl(String url) {
        Matcher matcher = REGEXP_PATTERN.matcher(url);
        if(!matcher.matches()) {
            return REGEXP_LEGACY_PATTERN.matcher(url).matches();
        } else return true;
    }


    static Pair<String, Boolean> extractBaseUrl(String imageUrl) throws MalformedURLException {
        Matcher matcher = REGEXP_PATTERN.matcher(imageUrl);
        Matcher legacyMatcher = REGEXP_LEGACY_PATTERN.matcher(imageUrl);
        if (matcher.matches()) {
            String pid = matcher.group(1);
            String imageIiif = matcher.group(2);
            
            int startIndex = imageUrl.indexOf(pid);
            int endIndex = imageUrl.indexOf(imageIiif);

            return Pair.of(imageUrl.substring(0, endIndex), true);
        } else if (legacyMatcher.matches()) {
            String pid = legacyMatcher.group(1);
            
            int startIndex = imageUrl.indexOf(pid);
            int endIndex = imageUrl.indexOf(pid)+pid.length();
            
            return Pair.of(imageUrl.substring(0, endIndex),false);
            
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static BufferedImage getThumb(String url) throws MalformedURLException, IOException {
        Pair<String,Boolean> base = SimpleIIIFGenerator.extractBaseUrl(url);
        if (base.getRight()) {
            BufferedImage img = KrameriusImageSupport.readImage(new URL( base.getLeft()+"/image/thumb" ), ImageMimeType.JPEG, -1);
            return img;
        } else {
            String baseUrl = base.getLeft();
            baseUrl =  baseUrl.replace("/iiif/", "/api/client/v7.0/items/");

            BufferedImage img = KrameriusImageSupport.readImage(new URL( baseUrl+"/image/thumb" ), ImageMimeType.JPEG, -1);
            return img;
        }
    }


    public static String buildInfoJsonUrl(Pair<String,Boolean> baseUrl) throws MalformedURLException {
        if (baseUrl.getRight()) {
            return baseUrl.getLeft() + "/image/iiif/info.json";
        } else {
            return baseUrl.getLeft() + "/info.json";
        }
    }

    public static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> scaleBoundingBox(
            Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> boundingBox, double scaleFactor) {
    
        // Extrahování hodnot z bounding boxu
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

    public static Pair<Integer,Integer> getIIIFDescriptor(String url) throws IOException {
    
        Pair<String,Boolean> baseUrl = SimpleIIIFGenerator.extractBaseUrl(url);
        //String itemId = SimpleIIIFGenerator.extractItemId(url);
        String infoJsonUrl = SimpleIIIFGenerator.buildInfoJsonUrl(baseUrl);

        InputStream inputStream = RESTHelper.inputStream(infoJsonUrl, null, null);
        JSONObject jsonObject = new JSONObject( IOUtils.toString(inputStream, "UTF-8") );
        if (jsonObject != null) {
            int maxWidth = (int) jsonObject.get("width");
            int maxHeight = (int) jsonObject.get("height");
            
            return Pair.of(maxWidth, maxHeight);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static Pair<Pair<Integer,Integer>, Pair<Integer,Integer>> parseBoundingBox(String url) {
        String regex = "/(\\d+),(\\d+),(\\d+),(\\d+)/";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int w = Integer.parseInt(matcher.group(3));
            int h = Integer.parseInt(matcher.group(4));
    
            Pair<Integer, Integer> firstPair = Pair.of(x, y);
            Pair<Integer, Integer> secondPair = Pair.of(w, h);
            Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> resultPair = Pair.of(firstPair, secondPair);
            
            return resultPair;
        } else {
            throw new IllegalArgumentException();
        }    
    }

    public static BufferedImage createImate(BufferedImage puvodniObrazek, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> range) {
        
        Graphics graphics = puvodniObrazek.getGraphics();
        BufferedImage novyObrazek = new BufferedImage(
                puvodniObrazek.getWidth(), puvodniObrazek.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < puvodniObrazek.getHeight(); y++) {
            for (int x = 0; x < puvodniObrazek.getWidth(); x++) {
                Color novaBarva = null;
                if (naOkrajiOblasti(x, y, range)) {
                    novaBarva = new Color(0,0,0,255);
                } else {
                    int alpha = jeVOblasti(x, y, range)  ? 0 : 200;
                    novaBarva = new Color(196, 200, 207, alpha);
                }
                novyObrazek.setRGB(x, y, novaBarva.getRGB());
            }
        }
        
        graphics.drawImage(novyObrazek, 0, 0, novyObrazek.getWidth(), novyObrazek.getHeight(), 0, 0, novyObrazek.getWidth(), novyObrazek.getHeight(), null);
        return puvodniObrazek;
    }
    
    private static boolean naOkrajiOblasti(int x, int y, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> oblast) {
        /**
         *  x horni  startX - startX + sirka -1
         *  y - startY - startY + 1   
         */
        
        int startX = oblast.getKey().getKey();
        int startY = oblast.getKey().getValue();
        int sirka = oblast.getValue().getKey();
        int vyska = oblast.getValue().getValue();

        if ((x >= startX && x <= startX + sirka - 1) && (y == startY || y == startY + vyska-1)) {
            return true;
        } else if ((y >= startY && y <= startY + vyska - 1) && (x == startX || x == startX + sirka-1)) {
            return true;
        }
        
        return false;
    }
    
    private static boolean jeVOblasti(int x, int y, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> oblast) {
        int startX = oblast.getKey().getKey();
        int startY = oblast.getKey().getValue();
        int sirka = oblast.getValue().getKey();
        int vyska = oblast.getValue().getValue();

        return x >= startX && x < startX + sirka && y >= startY && y < startY + vyska;
    }
    
    
    
}
