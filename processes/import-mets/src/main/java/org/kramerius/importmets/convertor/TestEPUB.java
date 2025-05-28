/*
 * Copyright (C) Sep 13, 2023 Pavel Stastny
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
package org.kramerius.importmets.convertor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class TestEPUB {
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File parentFolder = new File("/c:/Users/happy/TMP/ebooks");
        //String file = "sdilejte_16264276045682.epub";
        String file = "digistory_aneb_konec_oslich_usi.epub";

        File ebookFile = new File(parentFolder, file);
        
        EpubReader epubReader = new EpubReader();
        Book book = epubReader.readEpub(new FileInputStream(ebookFile));
        String content = BaseConvertor.readTextFromEPUB(book);
 
        System.out.println(content);

        Document parsed = Jsoup.parse(content);
        String wholeText = parsed.wholeText();
        System.out.println(wholeText);
//        
//        //Document doc = Jsoup.connect("https://en.wikipedia.org/").get();
        
    }
}
