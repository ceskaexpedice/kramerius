/*
 * Copyright (C) Dec 6, 2023 Pavel Stastny
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;


public class ClientIIIFGenerator extends ThumbsGenerator {

    public static final String uuidRegex = "(?i)uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    public static final Pattern compiledUuidRegex =  Pattern.compile(uuidRegex);
    
    public static final Logger LOGGER = Logger.getLogger(ClientIIIFGenerator.class.getName());
    
    @Override
    public BufferedImage generateThumbnail(String url) throws IOException {

        URL urlObject = new URL(url);
        String query = urlObject.getQuery();
        if (query != null) {
            Map<String, String> queryParameters = getQueryParameters(query);
            String bb = queryParameters.get("bb");
            Pattern pattern = Pattern.compile(uuidRegex);
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String pid = matcher.group(0);
                
                List<Integer> collected = Arrays.stream(bb.split(",")).map(Integer::valueOf).collect(Collectors.toList());
                if (collected.size() == 4) {
                    Pair<Pair<Integer,Integer>, Pair<Integer,Integer>> boudningBox = Pair.of(Pair.of(collected.get(0), collected.get(1)), Pair.of(collected.get(2), collected.get(3)));
                    String iiifJSON = KConfiguration.getInstance().getConfiguration().getString("api.client.point") + (KConfiguration.getInstance().getConfiguration().getString("api.client.point").endsWith("/") ? "" : "/") + String.format("items/%s/image/iiif/info.json", pid);
                    Pair<Integer, Integer> iiifInfo = getIIIFDescriptor(iiifJSON);
                    String thumbImage = KConfiguration.getInstance().getConfiguration().getString("api.client.point") + (KConfiguration.getInstance().getConfiguration().getString("api.client.point").endsWith("/") ? "" : "/") + String.format("items/%s/image/thumb", pid);
                    BufferedImage thumb = getThumb(thumbImage);

                    double widthScaleFactor = (double) thumb.getWidth() / iiifInfo.getLeft();
                    double heightScaleFactor = (double) thumb.getHeight() / iiifInfo.getRight();
                    double scaleFactor = Math.min(widthScaleFactor, heightScaleFactor);
                    BufferedImage image = SimpleIIIFGenerator.createImate(thumb, IIFUtils.scaleBoundingBox(boudningBox, scaleFactor));
                    return image;
                }
            }
        }
        return null;
    }

    @Override
    public boolean acceptUrl(String url) {
        try {
            // https://www.digitalniknihovna.cz/mzk/uuid/uuid:224d66f8-f48e-4a92-b41e-87c88a076dc0?bb=353,140,708,773
            URL urlObject = new URL(url);
            String query = urlObject.getQuery();
            if (query != null) {
                Map<String, String> queryParameters = getQueryParameters(query);
                boolean containsKey = queryParameters.containsKey("bb");
                if (containsKey) {
                    Pattern pattern = Pattern.compile(uuidRegex);
                    Matcher matcher = pattern.matcher(url);
                    if (matcher.find()) {
                        return true;
                    } 
                }
            }
            return false;
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        
        return false;
    }

    public static BufferedImage getThumb(String url) throws MalformedURLException, IOException {
        BufferedImage img = KrameriusImageSupport.readImage(new URL( url ), ImageMimeType.JPEG, -1);
        return img;
    }

    
    public static Pair<Integer,Integer> getIIIFDescriptor(String infoJsonUrl) throws IOException {
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

    
    private static Map<String, String> getQueryParameters(String query) throws UnsupportedEncodingException {
        Map<String, String> parameters = new HashMap();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0],"UTF-8");
                    String value = URLDecoder.decode(keyValue[1],"UTF-8");
                    parameters.put(key, value);
                }
            }
        }
        return parameters;
    }
    
    
}
