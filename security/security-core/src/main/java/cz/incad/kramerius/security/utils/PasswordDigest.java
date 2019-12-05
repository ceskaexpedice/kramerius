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
package cz.incad.kramerius.security.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import biz.sourcecode.base64Coder.Base64Coder;

/**
 * Digesting algorithm
 * @author pavels
 */
public class PasswordDigest {
    
    /** We are using SHA */
    public static final String ALGORITHM = "SHA";
    
    /**
     * Hash function applied on input string
     * @param input Input string
     * @return hashed string
     * @throws NoSuchAlgorithmException Cannot find algorithm
     * @throws UnsupportedEncodingException System doesn't support UTF-8 encoding
     */
    public static String messageDigest(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        byte[] digested = md.digest(input.getBytes("UTF-8"));
        return new String(Base64Coder.encode(digested));
    }
    
    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String pswd = "krameriusAdmin";
        System.out.println(messageDigest(pswd));
        
    }
}
