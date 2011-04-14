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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.relation.Relation;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.utils.IKeys;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;

/**
 * The servlet is intended to provide content to OAI server implementation.
 * It should be registered as a dissemination service in Fedora.
 *
 * <p>The present implementation generates librarian descriptors (BIBLIO_MODS)
 * for each PID and all its child relations and puts them to one document
 * in case of top level model. Otherwise it generates document with empty element.</p>
 *
 * <p>
 * XXX replace relation kind enums with model that is not static and that is configurable
 * </p>
 *
 * @author Jan Pokorsky
 */
public class OaiServlet extends GuiceServlet {

    private static final Logger LOG = Logger.getLogger(OaiServlet.class.getName());
    private static final EventFilter EXCLUDE_DOCUMENT_FILTER = new ExcludeDocumentFilter();

    private static final String DR_NAMESPACE_URI = "http://registrdigitalizace.cz/schemas/drkramerius/v1";
    private static final String DR_NAMESPACE = "dr";
    private static final QName RECORD_QNAME = new QName(DR_NAMESPACE_URI, "record", DR_NAMESPACE);
    private static final QName UUID_QNAME = new QName(DR_NAMESPACE_URI, "uuid", DR_NAMESPACE);
    private static final QName TYPE_QNAME = new QName(DR_NAMESPACE_URI, "type", DR_NAMESPACE);
    private static final QName DESCRIPTOR_QNAME = new QName(DR_NAMESPACE_URI, "descriptor", DR_NAMESPACE);

    // XXX make configurable
    private static Set<KrameriusModels> TOP_LEVEL_RELATIONS = EnumSet.of(
            KrameriusModels.MONOGRAPH, KrameriusModels.PERIODICAL);
    // XXX make configurable
    private static Set<KrameriusModels> EXCLUDE_RELATIONS = EnumSet.of(
//            KrameriusModels.DONATOR, KrameriusModels.INTERNALPART);
            KrameriusModels.DONATOR, KrameriusModels.INTERNALPART, KrameriusModels.PAGE);

    @Inject
    RelationService relService;
    @Inject
    @Named("rawFedoraAccess")
    FedoraAccess fedora;
    private XMLOutputFactory outFactory;
    private XMLEventFactory eventFactory;
    private XMLInputFactory inFactory;

    @Override
    public void init() throws ServletException {
        super.init();
        outFactory = XMLOutputFactory.newFactory();
        eventFactory = XMLEventFactory.newFactory();
        inFactory = XMLInputFactory.newFactory();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pid = req.getParameter(IKeys.PID_PARAMETER);
        if (pid == null || pid.length() == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile("oaiexp", null);
            tempFile.deleteOnExit();
            writeResponse(tempFile, pid);
            LOG.fine(String.format("pid: %s, length; %s, %s", pid, tempFile.length(), tempFile.toURI()));
            sendResponse(tempFile, resp);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, pid, ex);
            if (!resp.isCommitted()) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }

    }

    private void sendResponse(File tempFile, HttpServletResponse resp) throws IOException {
        Reader fileReader = new InputStreamReader(new FileInputStream(tempFile), "UTF-8");
        resp.setContentType("text/xml");
        resp.setContentLength((int) tempFile.length());
        resp.setCharacterEncoding("UTF-8");
        Writer responseWriter = resp.getWriter();

        char[] buffer = new char[2048];
        try {
            int length;
            while ((length = fileReader.read(buffer)) > 0) {
                responseWriter.write(buffer, 0, length);
            }
            responseWriter.flush();
        } finally {
            try {
                responseWriter.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "response writer", ex);
            }
            try {
                fileReader.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "temp reader", ex);
            }
        }
    }
    private void writeResponse(File tempFile, String pid) throws IOException {
        RelationModel model = relService.load(pid);
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
        try {
            XMLEventWriter xmlWriter = outFactory.createXMLEventWriter(writer);
            if (TOP_LEVEL_RELATIONS.contains(model.getKind())) {
                generate(new Context(pid, model, xmlWriter));
            } else {
                generateEmpty(xmlWriter);
            }
            writer.flush();
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        } finally {
            writer.close();
        }
    }
    
    private static class Context {
        private RelationModel model;
        private String uuid;
        private String pid;
        private XMLEventWriter writer;

        public Context(String pid, RelationModel model, XMLEventWriter writer) {
            this.model = model;
            this.pid = pid;
            this.uuid = resolveUuid(pid);
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

    private void generateBiblio(Context ctx) throws XMLStreamException, IOException {
        XMLEventWriter writer = ctx.getWriter();
        XMLEventReader reader = inFactory.createXMLEventReader(getBiblioStream(ctx.getPid()));
        reader = inFactory.createFilteredReader(reader, EXCLUDE_DOCUMENT_FILTER);
        writer.add(reader);
    }

    private void generateRecord(Context ctx, boolean topLevel) throws XMLStreamException, IOException {
        XMLEventWriter writer = ctx.getWriter();
        if (topLevel) {
            List<Namespace> namespaces = new ArrayList<Namespace>();
            namespaces.add(eventFactory.createNamespace(DR_NAMESPACE, DR_NAMESPACE_URI));
            writer.add(eventFactory.createStartElement(RECORD_QNAME, namespaces.iterator(), null));
        } else {
            writer.add(eventFactory.createStartElement(RECORD_QNAME, null, null));
        }

        // <uuid>
        writer.add(eventFactory.createStartElement(UUID_QNAME, null, null));
        writer.add(eventFactory.createCharacters(ctx.getUuid()));
        writer.add(eventFactory.createEndElement(UUID_QNAME, null));

        // <type>
        writer.add(eventFactory.createStartElement(TYPE_QNAME, null, null));
        writer.add(eventFactory.createCharacters(ctx.getModel().getKind().toString()));
        writer.add(eventFactory.createEndElement(TYPE_QNAME, null));

        // <descriptor>
        writer.add(eventFactory.createStartElement(DESCRIPTOR_QNAME, null, null));
        generateBiblio(ctx);
        writer.add(eventFactory.createEndElement(DESCRIPTOR_QNAME, null));

        // <record>*
        List<Relation> relations = getRelations(ctx.getModel());
        for (Relation relation : relations) {
            RelationModel relModel = relService.load(relation.getPID());
            generateRecord(new Context(relation.getPID(), relModel, ctx.getWriter()), false);
        }

        writer.add(eventFactory.createEndElement(RECORD_QNAME, null));
    }

    private void generate(Context ctx) throws XMLStreamException, IOException {
        XMLEventWriter writer = ctx.getWriter();
        writer.add(eventFactory.createStartDocument());
        generateRecord(ctx, true);
        writer.add(eventFactory.createEndDocument());
    }

    private void generateEmpty(XMLEventWriter writer) throws XMLStreamException {
        writer.add(eventFactory.createStartDocument());
        List<Namespace> namespaces = new ArrayList<Namespace>();
        namespaces.add(eventFactory.createNamespace(DR_NAMESPACE, DR_NAMESPACE_URI));
        writer.add(eventFactory.createStartElement(RECORD_QNAME, namespaces.iterator(), null));
        writer.add(eventFactory.createEndElement(RECORD_QNAME, null));
        writer.add(eventFactory.createEndDocument());
    }

    private InputStream getBiblioStream(String pid) throws IOException {
        InputStream dataStream = fedora.getDataStream(pid, "BIBLIO_MODS");
        return dataStream;
    }
    
    private static String resolveUuid(String pid) {
        try {
            PIDParser parser = new PIDParser(pid);
            parser.objectPid();
            String uuid = parser.getObjectId();
            if (uuid == null || uuid.length() == 0) {
                throw new IllegalArgumentException(pid);
            }
            return uuid;
        } catch (LexerException ex) {
            throw new IllegalStateException(pid, ex);
        }
    }

    private List<Relation> getRelations(RelationModel model) throws IOException {
        Set<KrameriusModels> relKinds = model.getRelationKinds();
        if (relKinds.isEmpty()) {
            return Collections.emptyList();
        }
        relKinds = EnumSet.copyOf(relKinds);
        relKinds.removeAll(EXCLUDE_RELATIONS);
        List<Relation> result = new ArrayList<Relation>();
        for (KrameriusModels relKind : relKinds) {
            List<Relation> kindRelations = model.getRelations(relKind);
            result.addAll(kindRelations);
        }
        return result;
    }

    private static class ExcludeDocumentFilter implements EventFilter {

        @Override
        public boolean accept(XMLEvent event) {
            // filter out start and end document not to break merged xml
            return !(event.isStartDocument() || event.isEndDocument());
        }

    }

}
