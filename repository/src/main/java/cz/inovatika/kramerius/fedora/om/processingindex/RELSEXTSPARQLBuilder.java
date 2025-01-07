package cz.inovatika.kramerius.fedora.om.processingindex;

import cz.inovatika.kramerius.fedora.om.repository.RepositoryException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Builder is able to prepare SPARQL update script
 * @see RELSEXTSPARQLBuilderListener
 */
interface RELSEXTSPARQLBuilder {

    /**
     * Generate update sparql
     * @param relsExt processing RELS-EXT stream
     * @param listener Listener Listener
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws RepositoryException
     */
    String sparqlProps(String relsExt, RELSEXTSPARQLBuilderListener listener) throws IOException, SAXException, ParserConfigurationException, RepositoryException;
}
