/*
 * Copyright (C) 2010 Jan Pokorsky
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

package cz.incad.kramerius.relation;

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.relation.impl.RelationModelImpl;
import cz.incad.kramerius.utils.XMLUtils;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.utils.DomUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for relations stuff.
 * <p/>XXX needs some refactoring to put methods to proper *Util classes
 *
 * @author Jan Pokorsky
 */
public final class RelationUtils {

    private RelationUtils() {
    }

    public static RelationModel emptyModel(String pid, KrameriusModels kind) {
        return new RelationModelImpl(pid, kind);
    }

    public static Document getDC(String pid, AkubraRepository akubraRepository) throws IOException {
        return getDataStream(pid, "DC", akubraRepository);
    }

    public static Document getRelsExt(String pid, AkubraRepository akubraRepository) throws IOException {
        return getDataStream(pid, "RELS-EXT", akubraRepository);
    }

    public static Document getMods(String pid, AkubraRepository akubraRepository) throws IOException {
        return getDataStream(pid, "BIBLIO_MODS", akubraRepository);
    }

    private static Document getDataStream(String pid, String streamName, AkubraRepository akubraRepository) throws IOException {
        Document doc = akubraRepository.getDatastreamContent(pid, streamName).asDom(true);
        return doc;
    }

}
