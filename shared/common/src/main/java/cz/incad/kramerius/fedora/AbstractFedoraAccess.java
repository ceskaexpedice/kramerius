package cz.incad.kramerius.fedora;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessStackAware;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public abstract class AbstractFedoraAccess implements FedoraAccess {

    public static final Logger LOGGER = Logger.getLogger(AbstractFedoraAccess.class.getName());
    protected XPathFactory xPathFactory;
    protected KConfiguration configuration;

    @Inject
    public AbstractFedoraAccess(KConfiguration configuration, @Nullable StatisticsAccessLog accessLog)
            throws IOException {
        super();
        this.configuration = configuration;
        this.xPathFactory = XPathFactory.newInstance();
    }

    protected String makeSureObjectPid(String pid) throws LexerException {
        PIDParser pidParser = new PIDParser(pid);
        pidParser.objectPid();
        String sureObjectPid = pidParser.isPagePid() ? pidParser.getParentObjectPid() : pidParser.getObjectPid();
        return sureObjectPid;
    }

    @Override
    public String getDonator(Document relsExt) {
        try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasDonator",
                    FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                return pidParser.getObjectId();
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    protected void changeStack(TreeNodeProcessor processor, Stack<String> pidStack) {
        if (processor instanceof TreeNodeProcessStackAware) {
            TreeNodeProcessStackAware stackAware = (TreeNodeProcessStackAware) processor;
            stackAware.changeProcessingStack(pidStack);
        }
    }

    protected boolean processSubtreeInternal(String pid, Document relsExt, TreeNodeProcessor processor, int level,
            Stack<String> pidStack)
                    throws XPathExpressionException, LexerException, IOException, ProcessSubtreeException {
        processor.process(pid, level);
        boolean breakProcessing = processor.breakProcessing(pid, level);
        if (breakProcessing) {
            return breakProcessing;
        }
        if (relsExt == null) {
            return false;
        }
        XPathFactory factory = this.xPathFactory;
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        XPathExpression expr = xpath.compile("/rdf:RDF/rdf:Description/*");
        NodeList nodes = (NodeList) expr.evaluate(relsExt, XPathConstants.NODESET);

        if (pidStack.contains(pid)) {
            LOGGER.log(Level.WARNING, "Cyclic reference on " + pid);
            return breakProcessing;
        }
        pidStack.push(pid);
        changeStack(processor, pidStack);
        for (int i = 0, ll = nodes.getLength(); i < ll; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element iteratingElm = (Element) node;
                String namespaceURI = iteratingElm.getNamespaceURI();
                if (namespaceURI != null && (namespaceURI.equals(FedoraNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI)
                        || namespaceURI.equals(FedoraNamespaces.RDF_NAMESPACE_URI))) {
                    String attVal = iteratingElm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                    if (!attVal.trim().equals("")) {
                        PIDParser pidParser = new PIDParser(attVal);
                        pidParser.disseminationURI();
                        String objectId = pidParser.getObjectPid();
                        if (pidParser.getNamespaceId().equals("uuid")) {
                            if (!processor.skipBranch(objectId, level + 1)) {
                                Document iterationgRelsExt = null;

                                try {
                                    iterationgRelsExt = getRelsExt(objectId);
                                } catch (Exception ex) {
                                    LOGGER.warning("could not read RELS-EXT, skipping branch [" + (level + 1)
                                            + "] and pid (" + objectId + "):" + ex);
                                }
                                breakProcessing = processSubtreeInternal(pidParser.getObjectPid(), iterationgRelsExt,
                                        processor, level + 1, pidStack);

                                if (breakProcessing) {
                                    break;
                                }
                            } else {
                                LOGGER.fine("skipping branch [" + (level + 1) + "] and pid (" + objectId + ")");
                            }
                        }
                    }

                }
            }
        }
        pidStack.pop();
        changeStack(processor, pidStack);
        return breakProcessing;
    }

    @Override
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException {
        try {
            pid = makeSureObjectPid(pid);
            Document relsExt = null;
            try {
                // should be from
                if (isStreamAvailable(pid, FedoraUtils.RELS_EXT_STREAM)) {
                    relsExt = getRelsExt(pid);
                } else {
                    LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + ")");
                }
            } catch (Exception ex) {
                LOGGER.warning("could not read root RELS-EXT, skipping object  (" + pid + "):" + ex);
            }
            if (!processor.skipBranch(pid, 0)) {
                processSubtreeInternal(pid, relsExt, processor, 0, new Stack<String>());
            }
        } catch (LexerException e) {
            throw new ProcessSubtreeException(e);
        } catch (XPathExpressionException e) {
            throw new ProcessSubtreeException(e);
        }
    }

    @Override
    public Set<String> getPids(String pid) throws IOException {
        final Set<String> retval = new HashSet<String>();
        try {
            processSubtree(pid, new TreeNodeProcessor() {
                @Override
                public void process(String pid, int level) {
                    retval.add(pid);
                }
    
                @Override
                public boolean breakProcessing(String pid, int level) {
                    return false;
                }
    
                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        }
        return retval;
    }

    @Override
    public String findFirstViewablePid(String pid) throws IOException {
        final List<String> foundPids = new ArrayList<String>();
        try {
            processSubtree(makeSureObjectPid(pid), new TreeNodeProcessor() {
                boolean breakProcess = false;
                int previousLevel = 0;
    
                @Override
                public boolean breakProcessing(String pid, int level) {
                    return breakProcess;
                }
    
                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
                }
    
                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try {
                        if (previousLevel < level || level == 0) {
                            if (AbstractFedoraAccess.this.isImageFULLAvailable(pid)) {
                                foundPids.add(pid);
                                breakProcess = true;
                            }
                        } else if (previousLevel > level) {
                            breakProcess = true;
                        } else if ((previousLevel == level) && (level != 0)) {
                            breakProcess = true;
                        }
                        previousLevel = level;
                    } catch (Exception e) {
                        throw new ProcessSubtreeException(e);
                    }
                }
            });
        } catch (ProcessSubtreeException e) {
            throw new IOException(e);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    
        return foundPids.isEmpty() ? null : foundPids.get(0);
    }

    @Override
    public String getKrameriusModelName(Document relsExt) throws IOException {
        try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasModel", FedoraNamespaces.FEDORA_MODELS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                return pidParser.getObjectId();
            } else {
                throw new IllegalArgumentException("cannot find model of given document");
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getDonator(String pid) throws IOException {
        return getDonator(getRelsExt(pid));
    }

    @Override
    public String getKrameriusModelName(String pid) throws IOException {
        return getKrameriusModelName(getRelsExt(pid));
    }

    @Override
    public List<Element> getPages(String pid, boolean deep) throws IOException {
        Document relsExt = getRelsExt(pid);
        return getPages(pid, relsExt.getDocumentElement());
    }

    @Override
    public boolean isImageFULLAvailable(String pid) throws IOException {
        try {
            return isStreamAvailable(makeSureObjectPid(pid), FedoraUtils.IMG_FULL_STREAM);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public List<Element> getPages(String pid, Element rootElementOfRelsExt) throws IOException {
        try {
            ArrayList<Element> elms = new ArrayList<Element>();
            String xPathStr = "/RDF/Description/hasPage";
            XPath xpath = this.xPathFactory.newXPath();
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(rootElementOfRelsExt, XPathConstants.NODESET);
            for (int i = 0, lastIndex = nodes.getLength() - 1; i <= lastIndex; i++) {
                Element elm = (Element) nodes.item(i);
                elms.add(elm);
            }
            return elms;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    protected List<String> getTreePredicates() {
        return Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.treePredicates"));
    }

    @Override
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException {
        try {
            String pid = pids.get(pids.size() - 1);
            pid = makeSureObjectPid(pid);
            if (isImageFULLAvailable(pid)) {
                return true;
            }
            Document relsExt = getRelsExt(pid);
            Element descEl = XMLUtils.findElement(relsExt.getDocumentElement(), "Description",
                    FedoraNamespaces.RDF_NAMESPACE_URI);
            List<Element> els = XMLUtils.getElements(descEl);
            for (Element el : els) {
                if (getTreePredicates().contains(el.getLocalName())) {
                    if (el.hasAttribute("rdf:resource")) {
                        pid = el.getAttributes().getNamedItem("rdf:resource").getNodeValue();
                        pids.add(pid);
                        models.add(getKrameriusModelName(pid));
                        if (getFirstViewablePath(pids, models)) {
                            return true;
                        } else {
                            pids.remove(pids.size() - 1);
                            models.remove(pids.size() - 1);
                        }
                    }
                }
            }
            return false;
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

    @Override
    public List<String> getModelsOfRel(Document relsExt) {
        try {
            throw new UnsupportedOperationException("still unsupported");
            // Element foundElement =
            // XMLUtils.findElement(relsExt.getDocumentElement(), "hasModel",
            // FedoraNamespaces.FEDORA_MODELS_URI);
            // if (foundElement != null) {
            // String sform =
            // foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI,
            // "resource");
            // PIDParser pidParser = new PIDParser(sform);
            // pidParser.disseminationURI();
            // ArrayList<String> model =
            // RelsExtModelsMap.getModelsOfRelation(pidParser.getObjectId());
            // return model;
            // } else {
            // throw new IllegalArgumentException("cannot find model of ");
            // }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public List<String> getModelsOfRel(String pid) throws IOException {
        return getModelsOfRel(getRelsExt(pid));
    }


}
