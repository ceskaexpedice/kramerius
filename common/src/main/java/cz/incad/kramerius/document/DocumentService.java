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
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.OutOfRangeException;

/**
 * Document model service
 */
public interface DocumentService {

    /**
     * Creates document model with structure 
     * @param path Object's pid path
     * @param pidFrom Pid of first page of resulting document. All pages before has been skipped. It can be null.
     * @param rect page size
     * @return Created document model
     * @throws IOException IO error has been occurred
     * @throws ProcessSubtreeException Error has been occurred during tree processing
     */
    PreparedDocument buildDocumentAsTree(ObjectPidsPath path, String pidFrom, int[]rect) throws IOException, ProcessSubtreeException;

    
    /**
     * Creates flat document model
     * @param path Object's pid path
     * @param pidFrom Pid of first page of resulting document. All pages before has been skipped. It can be null.
     * @param rect page size
     * @return Created document model
     * @throws IOException IO error has been occurred
     * @throws ProcessSubtreeException Error has been occurred during tree processing
     */
    PreparedDocument buildDocumentAsFlat(ObjectPidsPath path, String pidFrom, int howMany, int[] rect) throws IOException, ProcessSubtreeException,OutOfRangeException;
    
    /**
     * Creates flat document model from pids selection
     * @param selection PID's selection
     * @param rect page size
     * @return Created document model
     * @throws IOException IO error has been occurred
     * @throws ProcessSubtreeException Error has been occurred during tree processing
     */
    PreparedDocument buildDocumentFromSelection(String[] selection, int[] rect) throws IOException, ProcessSubtreeException, OutOfRangeException;
    

}
