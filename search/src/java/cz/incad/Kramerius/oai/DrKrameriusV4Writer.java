/*
 * Copyright (C) 2011 Jan Pokorsky
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

package cz.incad.Kramerius.oai;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.relation.Relation;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationService;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;

/**
 * Generator of content intended for Digitization Registry CZ in Kramerius 4 format.
 *
 * @author Jan Pokorsky
 */
public class DrKrameriusV4Writer implements OaiWriter {

    private static final Logger LOG = Logger.getLogger(OaiServlet.class.getName());
    private static final String DR_NS_URI = "http://registrdigitalizace.cz/schemas/drkramerius/v4";
    private static final String DR_NS_PREFIX = "dr";
    private static final QName RECORD_QNAME = new QName(DR_NS_URI, "record", DR_NS_PREFIX);
    private static final QName UUID_QNAME = new QName(DR_NS_URI, "uuid", DR_NS_PREFIX);
    private static final QName TYPE_QNAME = new QName(DR_NS_URI, "type", DR_NS_PREFIX);
    private static final QName POLICY_QNAME = new QName(DR_NS_URI, "policy", DR_NS_PREFIX);
    private static final QName RELATION_QNAME = new QName(DR_NS_URI, "relation", DR_NS_PREFIX);
    private static final QName DESCRIPTOR_QNAME = new QName(DR_NS_URI, "descriptor", DR_NS_PREFIX);

    private final XMLOutputFactory outFactory;
    private final XMLEventFactory eventFactory;
    private final XMLInputFactory inFactory;
    private final RelationService relService;
    private final FedoraAccess fedora;
    private final Set<KrameriusModels> topLevelRelations;
    private ExclusiveBuffer buffer;
    private final String pid;
    private boolean fetched = false;

    public DrKrameriusV4Writer(String pid,
            XMLOutputFactory outFactory, XMLEventFactory eventFactory,
            XMLInputFactory inFactory, RelationService relService, FedoraAccess fedora,
            Set<KrameriusModels> topLevelRelations) {
        this.pid = pid;
        this.outFactory = outFactory;
        this.eventFactory = eventFactory;
        this.inFactory = inFactory;
        this.relService = relService;
        this.fedora = fedora;
        this.topLevelRelations = topLevelRelations;
    }

    @Override
    public long getContentLength() throws IOException {
        fetchContent();
        return buffer.size();
    }

    @Override
    public InputStream getContent() throws IOException {
        fetchContent();
        ByteArrayInputStream content = new ByteArrayInputStream(buffer.getBuf(), 0, buffer.size());
        return content;
    }

    @Override
    public void close() {
    }

    private void fetchContent() throws IOException {
        if (fetched) {
            return ;
        }
        fetched = true;
        // do not use Writer as it returns wrong length for servlet response in UTF-8
        buffer = new ExclusiveBuffer(6000);
        writeResponse(buffer, pid);
        LOG.fine(String.format("pid: %s, length; %s", pid, buffer.size()));
    }

    private void writeResponse(OutputStream writer, String pid) throws IOException {
        RelationModel model = relService.load(pid);
        try {
            XMLEventWriter xmlWriter = outFactory.createXMLEventWriter(writer, "UTF-8");
            generate(new Context(pid, model, xmlWriter));
            xmlWriter.flush();
            xmlWriter.close();
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
    }

    private void generateBiblio(Context ctx) throws XMLStreamException, IOException {
        XMLEventWriter writer = ctx.getWriter();
        XMLEventReader reader = inFactory.createXMLEventReader(getBiblioStream(ctx.getPid()));
        reader = inFactory.createFilteredReader(reader, OaiServlet.EXCLUDE_DOCUMENT_FILTER);
        writer.add(reader);
    }

    private void generateRecord(Context ctx) throws XMLStreamException, IOException {
        XMLEventWriter writer = ctx.getWriter();
        List<Namespace> namespaces = new ArrayList<Namespace>();
        namespaces.add(eventFactory.createNamespace(DR_NS_PREFIX, DR_NS_URI));
        Iterator<Attribute> recordAttrs = null;
        if (topLevelRelations.contains(ctx.getModel().getKind())) {
            Attribute root = eventFactory.createAttribute("root", "true");
            recordAttrs = Arrays.asList(root).iterator();
        }
        writer.add(eventFactory.createStartElement(RECORD_QNAME, recordAttrs, namespaces.iterator()));

        // <uuid>
        writer.add(eventFactory.createStartElement(UUID_QNAME, null, null));
        writer.add(eventFactory.createCharacters(ctx.getUuid()));
        writer.add(eventFactory.createEndElement(UUID_QNAME, null));

        // <type>
        writer.add(eventFactory.createStartElement(TYPE_QNAME, null, null));
        writer.add(eventFactory.createCharacters(ctx.getModel().getKind().toString()));
        writer.add(eventFactory.createEndElement(TYPE_QNAME, null));
        
        // <policy>
        writer.add(eventFactory.createStartElement(POLICY_QNAME, null, null));
        writer.add(eventFactory.createCharacters(getDCPolicy(ctx.getPid())));
        writer.add(eventFactory.createEndElement(POLICY_QNAME, null));

        // <descriptor>
        writer.add(eventFactory.createStartElement(DESCRIPTOR_QNAME, null, null));
        generateBiblio(ctx);
        writer.add(eventFactory.createEndElement(DESCRIPTOR_QNAME, null));

        // <relation>*
        List<Relation> relations = OaiServlet.getRelations(ctx.getModel());
        for (Relation relation : relations) {
            writer.add(eventFactory.createStartElement(RELATION_QNAME, null, null));
            String uuid = OaiServlet.resolveUuid(relation.getPID());
            writer.add(eventFactory.createCharacters(uuid));
            writer.add(eventFactory.createEndElement(RELATION_QNAME, null));
        }

        writer.add(eventFactory.createEndElement(RECORD_QNAME, null));
    }

    private String getDCPolicy(String pid) throws IOException {
        String rights;
        Document document = fedora.getDC(pid);
        NodeList policyElements = document.getElementsByTagName("dc:rights");

        if (policyElements != null && policyElements.getLength() != 0) {
            rights = policyElements.item(0).getTextContent();
        } else {
            LOG.warning("Missing rights for " + pid);
            rights = "unknown";
        }
        return rights;
    }

    private void generate(Context ctx) throws XMLStreamException, IOException {
        XMLEventWriter writer = ctx.getWriter();
        writer.add(eventFactory.createStartDocument());
        generateRecord(ctx);
        writer.add(eventFactory.createEndDocument());
    }

    private InputStream getBiblioStream(String pid) throws IOException {
        InputStream dataStream = fedora.getDataStream(pid, "BIBLIO_MODS");
        return dataStream;
    }

    private static class Context {
        private RelationModel model;
        private String uuid;
        private String pid;
        private XMLEventWriter writer;

        public Context(String pid, RelationModel model, XMLEventWriter writer) {
            this.model = model;
            this.pid = pid;
            this.uuid = OaiServlet.resolveUuid(pid);
            this.writer = writer;
        }

        public String getUuid() {
            return uuid;
        }

        public String getPid() {
            return pid;
        }

        public RelationModel getModel() {
            return model;
        }

        public XMLEventWriter getWriter() {
            return writer;
        }

    }

    /**
     * Helper class to prevent making buffer copy in {@link #toByteArray() toByteArray()}.
     */
    private static final class ExclusiveBuffer extends ByteArrayOutputStream {

        public ExclusiveBuffer(int size) {
            super(size);
        }

        public byte[] getBuf() {
            return buf;
        }

    }

}
