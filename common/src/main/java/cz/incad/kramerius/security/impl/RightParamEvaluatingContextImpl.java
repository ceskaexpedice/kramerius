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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.solr.SolrUtils;

public class RightParamEvaluatingContextImpl implements RightCriteriumContext {

    private String requestedUUID;
    private String associatedUUID;
    private User user;
    private FedoraAccess fedoraAccess;

    
    public RightParamEvaluatingContextImpl(String reqUUID, User user, FedoraAccess fedoraAccess) {
        super();
        this.requestedUUID = reqUUID;
        this.user = user;
        this.fedoraAccess = fedoraAccess;
    }

    @Override
    public String getRequestedUUID() {
        return this.requestedUUID;
    }

    
    @Override
    public String getAssociatedUUID() {
        return this.associatedUUID;
    }

    @Override
    public AbstractUser getUser() {
        return this.user;
    }

    @Override
    public FedoraAccess getFedoraAccess() {
        return this.fedoraAccess;
    }

    
    
    @Override
    public void setAssociatedUUID(String uuid) {
        this.associatedUUID = uuid;
    }

    @Override
    public String[] getPathOfUUIDs() {
        try {
            Document solrData = SolrUtils.getSolrData(getRequestedUUID());
            return SolrUtils.disectPidPath(solrData).split("/");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }
}
