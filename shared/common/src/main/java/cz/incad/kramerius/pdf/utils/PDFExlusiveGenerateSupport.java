/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.pdf.utils;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class PDFExlusiveGenerateSupport {

    // controls genrating PDF
    public static final Semaphore PDF_SEMAPHORE = new Semaphore(KConfiguration
            .getInstance().getConfiguration()
            .getInt("pdfQueue.activeProcess", 5));

    // stores handle for pdf
    public static HashMap<String, File> PREPARED_FILES = new HashMap<String, File>();

    public static synchronized void pushFile(String uuid, File renderedPDF) {
        PREPARED_FILES.put(uuid, renderedPDF);
    }

    public static synchronized File popFile(String uuid) {
        return PREPARED_FILES.remove(uuid);
    }

}
