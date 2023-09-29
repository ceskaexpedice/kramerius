/*
 * Copyright (C) Sep 29, 2023 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.admin.v70.files;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

// maximum links 
public class GenerateDownloadLinks {
    
    public static final String HASH_ALG = "MD5";
    
    public static final Logger LOGGER = Logger.getLogger(GenerateDownloadLinks.class.getName());
    
    public static final int MAXIMUM_LINKS_IN_MAP = 120;

    private Map<String, String> generatedLinksMap = new HashMap<>();
    private Queue<String> generatedLinks = new LinkedList<>();
    
    public GenerateDownloadLinks() {
    }

    public synchronized String generateTmpLink(File f)  {
        try {
            String path = f.getAbsolutePath();
            MessageDigest md = MessageDigest.getInstance(HASH_ALG);
            byte[] dataBytes = path.getBytes("UTF-8");
            md.update(dataBytes, 0, dataBytes.length);
            byte[] hashBytes = md.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            if (generatedLinks.size() > MAXIMUM_LINKS_IN_MAP) {
                String first = generatedLinks.poll();
                generatedLinksMap.remove(first);
            }

            if (!generatedLinks.contains(path)) {
                generatedLinksMap.put(hexString.toString(), path);
                generatedLinks.add(hexString.toString());
                
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException |UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }
    
    
    public File getGeneratedTmpFile(String link) {
        if (this.generatedLinks.contains(link)) {
            String path = this.generatedLinksMap.get(link);
            if (path != null) {
                this.generatedLinks.remove(link);
                this.generatedLinksMap.remove(link);
                return new File(path);
            } else return null;
        } else return null;
    }
    
}
