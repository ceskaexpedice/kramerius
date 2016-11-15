package cz.incad.kramerius.repo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.repo.MetaRepoInformations;
import cz.incad.kramerius.repo.MetaRepoInformationsException;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.resourceindex.ResourceIndexService;

public class MetaRepoInformationsImpl implements MetaRepoInformations {

    public static final Logger LOGGER = Logger.getLogger(MetaRepoInformationsImpl.class.getName());
    
    @Override
    public List<String> getFedoraObjectsFromModel(String model, int limit, int offset, String orderby, String orderDir)
            throws MetaRepoInformationsException {
        int pageSize = 100;
        try {
            List<String> retvals = new ArrayList<String>();
            IResourceIndex rindex = ResourceIndexService.getResourceIndexImpl();
            org.w3c.dom.Document doc = rindex.getFedoraObjectsFromModelExt(model, pageSize, offset, "date", "asc");
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            xpath.setNamespaceContext(new FedoraNamespaceContext());
            String xPathStr = "/sparql:sparql/sparql:results/sparql:result/sparql:object";
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            String pid;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                pid = childnode.getAttributes().getNamedItem("uri").getNodeValue();
                pid = pid.replaceAll("info:fedora/", "");
                retvals.add(pid);
            }
            return retvals;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MetaRepoInformationsException(e);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MetaRepoInformationsException(e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MetaRepoInformationsException(e);
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MetaRepoInformationsException(e);
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MetaRepoInformationsException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new MetaRepoInformationsException(e);
        }
    }
}
