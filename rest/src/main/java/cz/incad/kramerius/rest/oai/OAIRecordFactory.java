/*
 * Copyright (C) 2025  Inovatika
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
package cz.incad.kramerius.rest.oai;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.oai.record.OAIRecordSupplement;
import cz.incad.kramerius.rest.oai.record.SupplementType;
import cz.incad.kramerius.rest.oai.representativepage.RepresentativePageFinderFactory;
import cz.incad.kramerius.rest.oai.strategies.MetadataExportStrategy;
import cz.incad.kramerius.rest.oai.utils.OAITools;
import cz.incad.kramerius.utils.XMLUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OAIRecordFactory {

    public static final Logger LOGGER = Logger.getLogger(OAIRecordFactory.class.getName());


    public static OAIRecord createRecord(String oaiIdentifier,SolrAccess sa, MetadataExportStrategy metadataExportStrategy) {
        try {
            String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
            String encodedQuery = URLEncoder.encode(String.format("pid:\"%s\"", pid),"UTF-8");
            String query = String.format("q=%s", encodedQuery);
            String solrResponseXml = sa.requestWithSelectReturningString(query, "xml", null);
            Document responseDocument = XMLUtils.parseDocument(new StringReader(solrResponseXml));
            Element doc = XMLUtils.findElement(responseDocument.getDocumentElement(), (elm) -> {
                return elm.getNodeName().equals("doc");
            });
            return createRecordInternal(oaiIdentifier,null, doc, sa, metadataExportStrategy);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static OAIRecord createRecord(String host, Element doc, SolrAccess sa, MetadataExportStrategy metadataExportStrategy) {
        return createRecordInternal(null, host, doc, sa, metadataExportStrategy);
    }

    @NotNull
    private static OAIRecord createRecordInternal(String oaidentifier, String host, Element doc, SolrAccess sa, MetadataExportStrategy metadataExportStrategy) {
        Element pidElm = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                return name.equals("pid");
            }
        });


        List<Element> collections  = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                return name.equals("cdk.collection");

            }
        });

        Element dateElm = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                return name.equals("indexed");

            }
        });

        Element dsImgFull = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                return name.equals("ds.img_full.mime");

            }
        });

        Element rootPid = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                return name.equals("root.pid");

            }
        });

        Element ownPidPath = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                return name.equals("own_pid_path");

            }
        });

        Element ownParentPid = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                String name = element.getAttribute("name");
                return name.equals("own_parent.pid");

            }
        });

        List<OAIRecordSupplement> supplements = new ArrayList<>();
        if (rootPid != null) {
            supplements.add(new OAIRecordSupplement(rootPid.getTextContent(), SupplementType.ROOT_PID));
        }
        if (ownPidPath != null) {
            supplements.add(new OAIRecordSupplement(ownPidPath.getTextContent(), SupplementType.OWN_PID_PATH));
        }
        if (ownParentPid != null) {
            supplements.add(new OAIRecordSupplement(ownParentPid.getTextContent(), SupplementType.OWN_PARENT_PID));
        }

        // dohledavani mimetype a firstpage
        if (dsImgFull != null) {
            supplements.add(new OAIRecordSupplement(dsImgFull.getTextContent(), SupplementType.REPRESENTATIVE_PAGE_MIME_TYPE));
            supplements.add(new OAIRecordSupplement(pidElm.getTextContent(), SupplementType.REPRESENTATIVE_PAGE_PID));
        }

        if (collections != null && !collections.isEmpty()) {
            List<String> cdkCollections = collections.stream().map(Element::getTextContent).collect(Collectors.toList());
            supplements.add(new OAIRecordSupplement(cdkCollections, SupplementType.CDK_COLLECTIONS));
        }

        // identifier

        String recident = oaidentifier != null ? oaidentifier : OAITools.oaiIdentfier(host, pidElm.getTextContent());
        OAIRecord oaiRecord = new OAIRecord(pidElm.getTextContent(), recident,dateElm != null ? dateElm.getTextContent() : "");
        supplements.stream().forEach(oaiRecord::addSupplement);

        // Find repre page
        // TODO: Should be removed to kramerius side
        //RepresentativePageFinderFactory.create().findRepresentativePage(oaiRecord, sa, metadataExportStrategy);

        return oaiRecord;
    }
}
