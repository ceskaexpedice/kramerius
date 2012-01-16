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
package cz.incad.kramerius;

import java.io.IOException;

import org.w3c.dom.Document;

/**
 * Class for access to SOLR
 * @author pavels
 *
 */
public interface SolrAccess {

        public Document getSolrDataDocument(String pid) throws IOException;
        
        public Document getSolrDataDocumentByHandle(String handle) throws IOException;
        
        public ObjectPidsPath[] getPath(String pid) throws IOException;

        public ObjectPidsPath[] getPath(String datastreamName, Document solrDataDoc) throws IOException;
        
        public ObjectModelsPath[] getPathOfModels(String pid) throws IOException;
        
}
