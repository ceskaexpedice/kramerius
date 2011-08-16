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
package cz.incad.Kramerius.processes;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import antlr.RecognitionException;
import antlr.TokenStreamException;

public class ParamsParserTest {

    public static void main(String[] args) throws RecognitionException, TokenStreamException, UnsupportedEncodingException {
        //String string = "{delete;{uuid\\:430d7f60-b03b-11dd-82fa-000d606f5dc6,uuid\\:0eaa6730-9068-11dd-97de-000d606f5dc6/uuid\\:430d7f60-b03b-11dd-82fa-000d606f5dc6};{uuid\\:4a7c2e50-af36-11dd-9643-000d606f5dc6,uuid\\:0eaa6730-9068-11dd-97de-000d606f5dc6/uuid\\:4a7c2e50-af36-11dd-9643-000d606f5dc6}}";
        
        String decode = URLDecoder.decode("%20Drobn%C5%AFstky%20", "UTF-8");
        String string = "{reindex;{fromKrameriusModel;uuid\\:0eaa6730-9068-11dd-97de-000d606f5dc6;"+decode+"}}";
        System.out.println(string);
        
        
        ParamsParser parser = new ParamsParser(new ParamsLexer(new StringReader(string)));
        List params = parser.params();
        for (int i = 0; i < params.size(); i++) {
            Object object = params.get(i);
            System.out.println(object);
        }

        
    }
}
