package cz.incad.Kramerius;

import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cz.incad.Kramerius.backend.guice.GuiceServlet;

public class AbstractSolrProcessServlet extends GuiceServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AbstractSolrProcessServlet.class.getName());
    
    protected transient XPathFactory fact;
    protected transient XPathExpression pidPathExpr;
    protected transient XPathExpression pidExpr;
    protected transient XPathExpression pathExpr;

    @Override
    public void init() throws ServletException {
    	super.init();
    	try {
    		fact = XPathFactory.newInstance();
    		pidPathExpr = pidPathExpr();
    		pidExpr = pidExpr();
    		pathExpr = pathExpr();
    	} catch (XPathExpressionException e) {
    		LOGGER.log(Level.SEVERE, e.getMessage(), e);
    		throw new RuntimeException(e.getMessage());
    	}
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	try {
    		fact = XPathFactory.newInstance();
    		pidPathExpr = pidPathExpr();
    	} catch (XPathExpressionException e) {
    		LOGGER.log(Level.SEVERE, e.getMessage(), e);
    		throw new RuntimeException(e.getMessage());
    	}
    }

    protected XPathExpression pidPathExpr() throws XPathExpressionException {
    	XPathExpression pidPathExpr = fact.newXPath().compile("//str[@name='pid_path']");
    	return pidPathExpr;
    }

    protected XPathExpression pidExpr() throws XPathExpressionException {
    	XPathExpression pidExpr = fact.newXPath().compile("//str[@name='PID']");
    	return pidExpr;
    }

    protected XPathExpression pathExpr() throws XPathExpressionException {
    	XPathExpression pathExpr = fact.newXPath().compile("//str[@name='path']");
    	return pathExpr;
    }

    protected String disectPidPath( Document parseDocument) throws XPathExpressionException {
        Node pidPathNode = (Node) pidPathExpr.evaluate(parseDocument, XPathConstants.NODE);
        if (pidPathNode != null) {
        	Element pidPathElm = (Element) pidPathNode;
        	return pidPathElm.getTextContent();
        }
        return null;
    }

    protected String disectPid(Document parseDocument) throws XPathExpressionException {
        Node pidNode = (Node) pidExpr.evaluate(parseDocument, XPathConstants.NODE);
        if (pidNode != null) {
        	Element pidElm = (Element) pidNode;
        	return pidElm.getTextContent();
        }
        return null;
    }

    protected String disectPath(Document parseDocument) throws XPathExpressionException {
        Node pathNode = (Node) pathExpr.evaluate(parseDocument, XPathConstants.NODE);
        if (pathNode != null) {
        	Element pathElm = (Element) pathNode;
        	return pathElm.getTextContent();
        }
        return null;
    }

}
