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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.NotImplementedException;
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
import cz.incad.kramerius.security.EvaluatingResult;
import cz.incad.kramerius.security.RightCriteriumPriorityHint;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Stena, ktera pousti vsechny dokumenty ktere jsou po datumu uvedenem v konfiguraci
 * 
 * Trida vzdy porovnava pouze datum uvedem v metadatech objektu, ktery je s pravem svazan.  
 * Pokud je pravo uvedeno na objetku REPOSITORY, pak zkouma nejvyssi prvek v hierarchii 
 * 
 * (konkretni monografii, konkretni periodikum, atd..)
 */
public class MovingWall extends AbstractCriterium implements RightCriterium {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MovingWall.class.getName());
    
    @Override
    public EvaluatingResult evalute() throws RightCriteriumException {

        int wallFromConf = Integer.parseInt((String)getObjects()[0]);
        try {
            throw new NotImplementedException("not implemented !");
            /*
            String[] pathOfUUIDs = getEvaluateContext().getPathsToRoot();
            List<String> path = new ArrayList<String>(Arrays.asList(pathOfUUIDs));
            Collections.reverse(path);
            for (String uuid : path) {
                EvaluatingResult resolved = resolveInternal(wallFromConf, uuid);
                if (resolved != EvaluatingResult.NOT_APPLICABLE) return resolved;
            }
            */
//            /return EvaluatingResult.NOT_APPLICABLE;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            return EvaluatingResult.FALSE;
        }
    }

    public EvaluatingResult resolveInternal(int wallFromConf, String uuid) throws IOException {
        //AbstractUser user = this.evalContext.getUser();
        Document dc = getEvaluateContext().getFedoraAccess().getDC(uuid);
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
    }

    public EvaluatingResult createResult(Calendar calFromMetadata, Calendar calFromConf) {
        return calFromMetadata.before(calFromConf) ?  EvaluatingResult.TRUE:EvaluatingResult.FALSE;
    }

//    @Override
//    public boolean validate(Object[] objs) {
//        if ((objs != null) && (objs.length == 1)) {
//            String val = (String) objs[0];
//            try {
//                Integer.parseInt(val);
//                return true;
//            } catch (NumberFormatException e) {
//                LOGGER.log(Level.SEVERE, e.getMessage(), e);
//                return false;
//            }
//        } else return false;
//    }

    @Override
    public RightCriteriumPriorityHint getPriorityHint() {
        return RightCriteriumPriorityHint.NORMAL;
    }

    @Override
    public boolean isParamsNecessary() {
        return true;
    }

    @Override
    public SecuredActions[] getApplicableActions() {
        return new SecuredActions[] {SecuredActions.READ};
    }

    @Override
    public boolean validateParams(Object[] vals) {
        if (vals.length == 1) {
            try {
                Integer.parseInt((String) vals[0]);
                return true;
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
                return false;
            }
        } else return false;
    }
    
    
    
}
