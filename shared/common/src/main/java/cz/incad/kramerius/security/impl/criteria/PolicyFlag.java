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
package cz.incad.kramerius.security.impl.criteria;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumException;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.SpecialObjects;

/**
 * Kontroluje priznak v metadatech RELS-EXT. 
 * Pokud 'kramerius:policy' ma hodnotu private, je dokument privatni a pristup je odpepren. 
 * V opacnem pripade je dokument verejny 
 * @author pavels
 *
 */
public class PolicyFlag extends AbstractCriterium {

    java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PolicyFlag.class.getName());
    
    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {
        try {
            FedoraAccess fa = getEvaluateContext().getFedoraAccess();
            String requestedPID = getEvaluateContext().getRequestedPid();
            if (!requestedPID.equals(SpecialObjects.REPOSITORY.getPid())) {
                Document relsExt = fa.getRelsExt(requestedPID);
                return checkPolicyElement(relsExt);
            } else return EvaluatingResult.TRUE;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return EvaluatingResult.TRUE;
        }
    }

//    @Override
//    public boolean validate(Object[] objs) {
//        return true;
//    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.NORMAL;
    }
    
    private EvaluatingResult checkPolicyElement(Document relsExt) throws IOException {
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath xpath = xpfactory.newXPath();
            xpath.setNamespaceContext(new FedoraNamespaceContext());
            XPathExpression expr = xpath.compile("//kramerius:policy/text()");
            Object policy = expr.evaluate(relsExt, XPathConstants.STRING);
            if ((policy != null) && (policy.toString().trim().equals("policy:private"))) {
                return EvaluatingResult.FALSE;
            } else {
                return EvaluatingResult.TRUE;
            }
                
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isParamsNecessary() {
        return false;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return  new SecuredActions[] {SecuredActions.READ};
    }
    
    
}
