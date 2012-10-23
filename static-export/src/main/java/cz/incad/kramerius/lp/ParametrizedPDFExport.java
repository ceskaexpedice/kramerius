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
package cz.incad.kramerius.lp;

import java.io.File;

import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.processes.impl.ProcessStarter;

public class ParametrizedPDFExport {

    
    @Process
    public static void process(@ParameterName("outputFolder") File outputFolder, 
                                @ParameterName("medium") String medium, 
                                @ParameterName("pid") String pid,
                                @ParameterName("imgUrl") String imgUrl,
                                @ParameterName("i18nUrl") String i18nUrl,
                                @ParameterName("country") String country,
                                @ParameterName("lang") String lang) throws Exception {

        //TODO: i18N
        ProcessStarter.updateName("Staticky export titulu "+pid+" na "+medium);
        
        PDFExport.main(new String[] {outputFolder.getAbsolutePath(), medium, pid, imgUrl, i18nUrl, country, lang});
    }
}
