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
package cz.incad.kramerius.printing;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.print.PrintException;
import javax.print.PrintService;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.document.model.PreparedDocument;

/**
 * Print service 
 * @author pavels
 */
public interface PrintingService {
    
    /**
     * Prints master option (top selection)
     * @param imgUrl Image servlet URL
     * @param i18nUrl I18N servlet URL
     * @throws IOException 
     * @throws ProcessSubtreeException
     * @throws PrinterException
     * @throws PrintException
     */
    public void printMaster( String pidFrom, String imgUrl, String i18nUrl) throws IOException, ProcessSubtreeException, PrinterException, PrintException;
    
    /**
     * Print selection option (from - to)
     * @param selection Selected pids
     * @param imgUrl Image servlet URL
     * @param i18nUrl I18N servlet URL
     * @throws IOException
     * @throws ProcessSubtreeException
     * @throws PrinterException
     * @throws PrintException
     */
    public void printSelection(String[] selection,   String imgUrl, String i18nUrl) throws IOException, ProcessSubtreeException, PrinterException, PrintException;
}
