/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.Kramerius.apache.modproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public class BehindModProxyRequest implements InvocationHandler {

    private HttpServletRequest reqest;
    private String url;
    
    BehindModProxyRequest(HttpServletRequest request, String url) {
        super();
        this.reqest = request;
        this.url = url;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getRequestURL")) {
            return new StringBuffer(this.url);
        } else {
            return method.invoke(this.reqest, args);
        }
    }

    
    public static HttpServletRequest newInstance(HttpServletRequest reqest, String  url) {
        return (HttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(BehindModProxyRequest.class.getClassLoader(), 
                new Class[] {ServletRequest.class, HttpServletRequest.class},new BehindModProxyRequest(reqest, url));  
    }
}
