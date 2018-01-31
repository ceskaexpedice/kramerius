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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
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

    private enum Format {
        /*drkramerius, */drkramerius4
    }

    private static final Logger LOG = Logger.getLogger(OaiServlet.class.getName());
    static final EventFilter EXCLUDE_DOCUMENT_FILTER = new ExcludeDocumentFilter();
    private static final String FORMAT_PARAMETER = "format";

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
        outFactory = XMLOutputFactory.newInstance();
        eventFactory = XMLEventFactory.newInstance();
        inFactory = XMLInputFactory.newInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pid = req.getParameter(IKeys.PID_PARAMETER);
        Format format = resolveFormatParameter(req);
        if (pid == null || pid.length() == 0 || format == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        OaiWriter oaiWriter = resolveWriter(format, pid);
        try {
            sendResponse(oaiWriter, resp);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, pid + " doesn't exist in Fedora.");
            if (!resp.isCommitted()) {
                resp.reset();
                sendErrorMessage(resp, ex.getMessage());
            }
        } finally {
            oaiWriter.close();
        }

    }
    
    private void sendErrorMessage(HttpServletResponse resp, String errMessage) {
        errMessage = "<error>" + errMessage + "</error>";
        resp.setContentType("text/xml");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(errMessage.length());

        try (PrintWriter respWriter = resp.getWriter()) {
            respWriter.write(errMessage);
            respWriter.flush();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "response writer", ex);
        }
    }

    private static Format resolveFormatParameter(HttpServletRequest req) {
        String formatVal = req.getParameter(FORMAT_PARAMETER);
        Format format = Format.drkramerius4;
        if (formatVal != null) {
            try {
                format = Format.valueOf(formatVal);
            } catch (Exception e) {
                format = null;
            }
        }
        return format;
    }

    private OaiWriter resolveWriter(Format format, String pid) {
        OaiWriter writer;
        switch (format) {
//            case drkramerius:
//                writer = new DrKrameriusV1Writer(pid, outFactory, eventFactory,
//                        inFactory, relService, fedora, TOP_LEVEL_RELATIONS);
//                break;
            case drkramerius4:
                writer = new DrKrameriusV4Writer(pid, outFactory, eventFactory,
                        inFactory, relService, fedora, TOP_LEVEL_RELATIONS);
                break;
            default:
                throw new IllegalStateException("unknown format: " + format);
        }
        return writer;
    }

    private void sendResponse(OaiWriter oaiWriter, HttpServletResponse resp) throws IOException {
        InputStream fileReader = oaiWriter.getContent();
        resp.setContentType("text/xml");
        resp.setContentLength((int) oaiWriter.getContentLength());
        resp.setCharacterEncoding("UTF-8");
        OutputStream responseWriter = resp.getOutputStream();

        byte[] buffer = new byte[2048];
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
    
    static String resolveUuid(String pid) {
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

    static List<Relation> getRelations(RelationModel model) throws IOException {
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

    private static final class ExcludeDocumentFilter implements EventFilter {

        @Override
        public boolean accept(XMLEvent event) {
            // filter out start and end document not to break merged xml
            return !(event.isStartDocument() || event.isEndDocument());
        }

    }

}
