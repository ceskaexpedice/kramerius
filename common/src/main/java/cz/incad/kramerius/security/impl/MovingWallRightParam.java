/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.security.impl;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.RightParamEvaluateContextException;
import cz.incad.kramerius.security.RightParam;
import cz.incad.kramerius.security.RightParamEvaluatingContext;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 */
public class MovingWallRightParam implements RightParam {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MovingWallRightParam.class.getName());
    
    private RightParamEvaluatingContext evalContext;
    
    @Override
    public RightParamEvaluatingContext getEvaluateContext() {
        return this.evalContext;
    }

    @Override
    public void setEvaluateContext(RightParamEvaluatingContext ctx) {
        this.evalContext = ctx;
    }

    @Override
    public boolean evalute() throws RightParamEvaluateContextException {
        try {
            String uuid = this.evalContext.getUUID();
            //AbstractUser user = this.evalContext.getUser();
            Document dc = this.evalContext.getFedoraAccess().getDC(uuid);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            xpath.setNamespaceContext(new FedoraNamespaceContext());
            //"dc:date"
            XPathExpression expr = xpath.compile("//dc:date/text()");
            NodeList nodes = (NodeList) expr.evaluate(dc, XPathConstants.NODESET);
            System.out.println(nodes.getLength());
            int length = nodes.getLength();
            for (int i = 0; i < length; i++) {
                Node item = nodes.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element elm = (Element) item;
                    System.out.println(elm.getLocalName());
                    System.out.println(elm.getNamespaceURI());
                    System.out.println(elm.getNodeName());
                }
                System.out.println(item);
            }
            
            if (nodes.getLength() >= 1) {
                
                Text txt = (Text) nodes.item(0);
                // jak to bude ?? 
                
                int yearFromMetadata = Integer.parseInt(txt.getData());
                int wallFromConf = KConfiguration.getInstance().getConfiguration().getInt("movingWallYear", 1910);

                Calendar calFromMetadata = Calendar.getInstance();
                calFromMetadata.set(Calendar.YEAR, yearFromMetadata);
                
                Calendar calFromConf = Calendar.getInstance();
                calFromConf.set(Calendar.YEAR, wallFromConf);
                
                return calFromMetadata.before(calFromConf);
            } else return false;

        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return true;
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return true;
        }
    }
}
