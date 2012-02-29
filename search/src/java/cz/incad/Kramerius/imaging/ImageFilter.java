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
package cz.incad.Kramerius.imaging;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class ImageFilter implements Filter {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ImageFilter.class.getName());
    
    private FilterConfig filterConfig;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain arg2) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest servRequest = (HttpServletRequest) request;
            String string = servRequest.getRequestURL().toString();
            URL url = new URL(string);
            
            Map<String, String> map = disectObject(url.getPath());
            this.filterConfig.getServletContext().getRequestDispatcher(constructFromMap(map))
                .forward(request,response);
        } 
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        this.filterConfig = arg0;
    }


    private static String constructFromMap(Map<String, String> m) {
        StringBuilder builder = new StringBuilder();
        builder.append("/img?action=GETRAW&stream=").append(m.get("stream")).append("&uuid=").append(m.get("pid"));
        if (m.containsKey("page")) {
            builder.append("#page=").append(m.get("page"));
        }
        return builder.toString();
    }

    private static Map<String, String> disectObject(String p) {
        Map<String, String> map = new HashMap<String, String>();
        List<String> expectingTokens = new ArrayList<String>(Arrays.asList(new String[]{"application","filter","stream", "pid","page"}));
        StringTokenizer tokenizer = new StringTokenizer(p,"/");
        while(tokenizer.hasMoreTokens()) {
            if (!expectingTokens.isEmpty()) {
                String key = expectingTokens.remove(0);
                map.put(key,tokenizer.nextToken()); 
            } else {
                LOGGER.log(Level.SEVERE,"skiping "+tokenizer.nextToken());
            }
        }
        return map;
    }
    
    public static void main(String[] args) throws MalformedURLException {
        //String url = "http://vmkramerius:8080/search/img/IMG_FULL/uuid:ae876087-435d-11dd-b505-00145e5790ea";

        //http://vmkramerius:8080/search/img?uuid=uuid:3ee97ce8-e548-11e0-9867-005056be0007&stream=IMG_FULL&action=GETRAW#page=1
        String url = "http://vmkramerius:8080/search/img/IMG_FULL/uuid:ae876087-435d-11dd-b505-00145e5790ea#page=3";

        URL urlObject = new URL(url);
        Map<String, String> map = disectObject(urlObject.getPath());
        System.out.println(map);
        
        System.out.println(constructFromMap(map));
        
    }
}
