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
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 */
public class MovingWallRightParam implements RightCriterium {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MovingWallRightParam.class.getName());
    
    private RightCriteriumContext evalContext;
    
    private Object[] objs;
    
    @Override
    public RightCriteriumContext getEvaluateContext() {
        return this.evalContext;
    }

    @Override
    public void setEvaluateContext(RightCriteriumContext ctx) {
        this.evalContext = ctx;
    }

    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        int wallFromConf = Integer.parseInt((String)getObjects()[0]);
        try {
            String uuid = this.evalContext.getAssociatedUUID();
            //AbstractUser user = this.evalContext.getUser();
            Document dc = this.evalContext.getFedoraAccess().getDC(uuid);
            Element dateElem = XMLUtils.findElement(dc.getDocumentElement(), "date", FedoraNamespaces.DC_NAMESPACE_URI);
            if (dateElem != null) {
                String dateString = dateElem.getTextContent();

                int yearFromMetadata = Integer.parseInt(dateString);
                Calendar calFromMetadata = Calendar.getInstance();
                calFromMetadata.set(Calendar.YEAR, yearFromMetadata);
                
                Calendar calFromConf = Calendar.getInstance();
                calFromConf.set(Calendar.YEAR, wallFromConf);
                
                return createResult(calFromMetadata, calFromConf);
            } else return EvaluatingResult.NOT_APPLICABLE;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResult.FALSE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResult.FALSE;
        }
    }

    public EvaluatingResult createResult(Calendar calFromMetadata, Calendar calFromConf) {
        return calFromMetadata.before(calFromConf) ?  EvaluatingResult.TRUE:EvaluatingResult.FALSE;
    }

    @Override
    public Object[] getObjects() {
        return this.objs;
    }

    @Override
    public void setObjects(Object[] objs) {
        this.objs = objs;
    }

    @Override
    public boolean validate(Object[] objs) {
        if ((objs != null) && (objs.length == 1)) {
            String val = (String) objs[0];
            try {
                Integer.parseInt(val);
                return true;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return false;
            }
        } else return false;
    }
}
