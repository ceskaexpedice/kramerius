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
package cz.incad.kramerius.document;

import java.io.IOException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;

/**
 * Service for creating documents model for printing and generating pdf
 * @author pavels
 */
public interface DocumentService {

    
    AbstractRenderedDocument buildDocumentAsTree(ObjectPidsPath path, String pidFrom, int[]rect) throws IOException, ProcessSubtreeException;

    AbstractRenderedDocument buildDocumentAsFlat(ObjectPidsPath path, String pidFrom, int howMany, int[] rect) throws IOException, ProcessSubtreeException;
    
    
    AbstractRenderedDocument buildDocumentFromSelection(String[] selection, int[] rect) throws IOException, ProcessSubtreeException;
    
}
