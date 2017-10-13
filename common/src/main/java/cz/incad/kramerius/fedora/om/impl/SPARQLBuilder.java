package cz.incad.kramerius.fedora.om.impl;

import cz.incad.kramerius.fedora.om.RepositoryException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface SPARQLBuilder {

    public String sparqlProps(String relsExt, SPARQLBuilderListener listener) throws IOException, SAXException, ParserConfigurationException, RepositoryException;


}
