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
package cz.incad.Kramerius.exts.menu.context.impl.utils;

import static cz.incad.utils.IKeys.PID_PARAMETER;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.imgs.ImageMimeType;

public class MenuMimeTypesUtils {

    public static String mimeTypeDisect(String pid, FedoraAccess fedoraAccess) throws IOException, XPathExpressionException {
    	boolean imgfullAvailable = (pid != null && !pid.trim().equals("")) ? fedoraAccess.isImageFULLAvailable(pid) : false;
        String mimeType = imgfullAvailable ? fedoraAccess.getImageFULLMimeType(pid) : "";
        return mimeType;
    }

    public static  boolean isPDFMimeType(String mimeType) {
        return ImageMimeType.PDF.getValue().toLowerCase().equals(mimeType.toLowerCase());
    }

}
